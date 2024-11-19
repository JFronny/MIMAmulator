//
// Created by Alex on 17.11.2024.

#include <ctype.h>
#include <errno.h>
#include <inttypes.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "hash.h"
#include "list.h"
#include "logger.h"

#define MAX_LINE_LENGTH 100
#define MIMA_MEM_SIZE 1048576


// for 1 Word = 3 Bytes per line
// hexdump -e '"%07.7_ax  " 3/1 "%02x " "\n"' examples/prog.mbf

typedef struct {
    bool used;
    uint32_t content;
} memory_cell;


/** Trim whitespaces from string. (Edits the string!)
 * 
 * @param s The string
 * @return The string, but without whitespaces
 */
char* strstrip(char *s)
{
    const size_t size = strlen(s);

    if (!size)
        return s;

    char *end = s + size - 1;
    while (end >= s && isspace(*end))
        end--;
    *(end + 1) = '\0';

    while (*s && isspace(*s))
        s++;

    return s;
}

/** Reads a file and does initial parsing (removing comments, empty lines, whitespace trimming, etc.)
 * 
 * @param filepath The filepath
 * @param lines The resulting list of strings
 * @return 0 on success, -1 on failure
 */
int readfile(const char* filepath, list* lines) {
    if (filepath == NULL || lines == NULL) {
        return -1;
    }

    FILE* fp = fopen(filepath, "r");
    if (fp == NULL)
        return -1;

    char buf[MAX_LINE_LENGTH];

    int buf_idx = 0;
    char c;
    while ((c = fgetc(fp)) != EOF) {
        // remove comments
        if (c == ';') {
            while ((c = fgetc(fp)) != EOF && c != '\n') {}
        }

        // remove tabs and carriage returns
        if (c == '\r' || c == '\t') {
            continue;
        }

        // split after a colon for labels
        if (c == ':') {
            buf[buf_idx] = c; // keep the colon
            buf[buf_idx+1] = '\0';
            push(lines, strstrip(buf));
            buf_idx = 0;
            continue;
        }

        // end line after newline
        if (c == '\n') {
            buf[buf_idx] = '\0';

            if (!strstrip(buf)[0])
                continue;

            push(lines, strstrip(buf));
            buf_idx = 0;
            continue;
        }

        buf[buf_idx] = c;
        buf_idx++;
        if(buf_idx + 1 >= MAX_LINE_LENGTH) {
            log_error("Line too long!");
            return -1;
        }
    }

    fclose(fp);
    return 0;
}

/** Checks if needle is substring of haystack.
 * 
 * @param haystack The string to be searched in
 * @param needle The substring to search for
 * @return true if substring was found
 */
bool is_substr(const char* haystack, const char *needle) {
    return strstr(haystack, needle) == NULL? false : true;
}

/** Inserts all opcodes into a hashtable
 * 
 * @param opcodes The hashtable for the result
 */
void make_opcode_table(hashtable* opcodes) {
    insert(opcodes, "LDC" , 0x000000);
    insert(opcodes, "LDV" , 0x100000);
    insert(opcodes, "STV" , 0x200000);
    insert(opcodes, "ADD" , 0x300000);
    insert(opcodes, "AND" , 0x400000);
    insert(opcodes, "OR"  , 0x500000);
    insert(opcodes, "XOR" , 0x600000);
    insert(opcodes, "EQL" , 0x700000);
    insert(opcodes, "JMP" , 0x800000);
    insert(opcodes, "JMN" , 0x900000);
    insert(opcodes, "HALT", 0xF00000);
    insert(opcodes, "NOT" , 0xF10000);
    insert(opcodes, "RAR" , 0xF20000);
}

/** Parses the document and extracts labels, variables and constants (and their memory locations) 
 * 
 * @param constants The hashtable to store the found labels, variables and constants
 * @param memory The memory in which to set the values for the constants
 * @param lines The list of lines to be parsed 
 * @return 0 on success, -1 on failure
 */
int extract_constants(hashtable* constants, memory_cell memory[], const list* lines) {
    int memory_location = 0;
    char buf[MAX_LINE_LENGTH];

    for (int i = 0; i < lines->used; ++i) {
        if (0 > memory_location || memory_location > MIMA_MEM_SIZE) {
            log_error("Writing outside of the MIMAs Memory!");
            return -1;
        }

        if (is_substr(lines->array[i], ":")) {
            // LABEL
            strcpy(buf, lines->array[i]);
            if (buf[0] == ':') {
                log_warning("Label with no name found!");
                continue;
            }
            buf[strlen(buf) - 1] = '\0'; // removes colon
            insert(constants, buf, memory_location);
        }
        else if (is_substr(lines->array[i], "* = ")) {
            // ORG
            strcpy(buf, lines->array[i]);

            const char* address_str = strstrip(buf + 4);
            if (*address_str == '\0') {
                log_warning("No address found after '* = '");
                return -1;
            }

            errno = 0;
            const int value = strtol(address_str, NULL, 0);
            if (errno == 0)
                memory_location = value;
            else {
                log_warning("Unable to parse Integer");
                return -1;
            }
        }
        else if (is_substr(lines->array[i], " = ")) {
            // VARIABLE
            strcpy(buf, lines->array[i]);

            char* start_of_substr = strstr(buf, " = "); // cannot be NULL

            if (*(start_of_substr + 3) == '\0') {
                log_warning(" No value found after ' = '");
                continue;
            }

            *start_of_substr = '\0';

            errno = 0;
            const int value = strtol(start_of_substr + 3, NULL, 0);
            if (errno == 0)
                insert(constants, buf, value);
            else {
                log_warning("Unable to parse Integer");
                return -1;
            }

        }
        else if (is_substr(lines->array[i], " DS ")) {
            // DATA STORE
            strcpy(buf, lines->array[i]);

            char* start_of_substr = strstr(buf, " DS "); // cannot be NULL
            if (*(start_of_substr + 4) == '\0') {
                log_warning("No value found after ' DS '");
                memory_location++;
                continue;
            }

            if (memory[memory_location].used) {
                log_warning("Overwriting already used memory!");
            }

            errno = 0;
            const int value = strtol(start_of_substr + 4, NULL, 0);
            if (errno == 0) {
                memory[memory_location].used = true;
                memory[memory_location].content = value;

                *start_of_substr = '\0';
                insert(constants, buf, memory_location);

                memory_location++;
            }
            else {
                log_warning("Unable to parse Integer");
                return -1;
            }
        }
        else {
            // OPERATION
            memory_location++;
        }
    }
    return 0;
}

/** Assembles the instruction into memory
 *
 * @param memory The resulting memory
 * @param lines The lines to be parsed
 * @param constants The constants to be used for replacing
 * @param opcodes The hashtable of instructions and their opcodes
 * @return 0 on success, -1 on failure
 */
int assemble_opcodes(memory_cell memory[], const list* lines, const hashtable* constants, const hashtable* opcodes) {
    int memory_location = 0;

    for (int i = 0; i < lines->used; ++i) {
        if (0 > memory_location || memory_location > MIMA_MEM_SIZE) {
            log_error("Writing outside of the MIMAs Memory!");
            return -1;
        }

        if (is_substr(lines->array[i], ":"))
            // skip Labels
            continue;

        if (is_substr(lines->array[i], "* = ")) {
            // ORG
            if (*(lines->array[i] + 4) == '\0') {
                log_warning(" No value found after '* = '");
                continue;
            }
            const char* address_str = strstrip(lines->array[i] + 4);

            errno = 0;
            const int value = strtol(address_str, NULL, 0);
            if (errno == 0)
                memory_location = value;
            else {
                log_warning("Unable to parse Integer");
                return -1;
            }
        }
        else if (is_substr(lines->array[i], " = "))
            // skip Variables
            continue;
        else if (is_substr(lines->array[i], " DS "))
            // DATA STORE
            memory_location++;
        else {
            // parse Operation
            char buf[MAX_LINE_LENGTH];
            strcpy(buf, lines->array[i]);

            if (memory[memory_location].used) {
                log_warning("Overwriting already used memory!");
            }

            int address = 0;
            char* opcode_end = strchr(buf, ' ');

            // if there is an argument:
            if (opcode_end != NULL) {
                const char* address_or_const = strstrip(opcode_end);
                if (find(constants, address_or_const, &address)) {
                    errno = 0;
                    address = strtol(address_or_const, NULL, 0);
                    if (errno != 0) {
                        log_warning("Unable to parse Integer");
                        return -1;
                    }
                }
                *opcode_end = '\0';
            }

            // if there is no argument
            const char* opcode_str = strstrip(buf);
            int opcode;
            if (find(opcodes, opcode_str, &opcode)) {
                log_warning("opcode %s not found!\n");
                continue;
            }
            memory[memory_location].used = true;
            memory[memory_location].content = opcode | (address & 0x0FFFFF);
            memory_location++;
        }
    }
    return 0;
}

/** Write memory to a file.
 *
 * @param memory The memory to be written.
 * @param filepath The file for the output
 * @return 0 on succes, -1 otherwise
 */
int write_memory(memory_cell memory[], const char* filepath) {
    if (filepath == NULL | memory == NULL) {
        return -1;
    }

    FILE* fp = fopen(filepath, "w");
    if (fp == NULL) {
        log_warning("Failed to open/create output file.");
        return -1;
    }

    if (memory == NULL) {
        return -1;
    }

    for (int i = 0; i < MIMA_MEM_SIZE; ++i) {
        const uint32_t a = memory[i].content;
        fputc(a >> 16, fp);
        fputc(a >> 8, fp);
        fputc(a, fp);
    }

    if (ferror(fp)) {
        log_warning("Failed to write to output file.");
        return -1;
    }

    fclose(fp);
    return 0;
}

/** Assemble a .masm (MIMA assembly) file to .mbf (MIMA binary file)
 *
 * @param in_file The input filepath
 * @param out_file The output filepath
 * @return 0 on success, -1 otherwise
 */
int assemble(const char* in_file, const char* out_file) {

    if(in_file == NULL || out_file == NULL) {
        return -1;
    }

    memory_cell *memory = malloc(MIMA_MEM_SIZE * sizeof(memory_cell));
    if (memory == NULL)
        return -1;

    list lines;
    init_list(&lines);

    if (readfile(in_file, &lines)) {
        free_list(&lines);
        return -1;
    }

    hashtable constants;
    if (init_hashtable(&constants, 100)) {
        free_list(&lines);
        return -1;
    }


    if (extract_constants(&constants, memory, &lines)) {
        free_list(&lines);
        free_hashtable(&constants);
    }

    hashtable opcodes;
    if (init_hashtable(&opcodes, 100)) {
        free_list(&lines);
        free_hashtable(&constants);
        return -1;
    }
    make_opcode_table(&opcodes);

    if (assemble_opcodes(memory, &lines, &constants, &opcodes)) {
        free_list(&lines);
        free_hashtable(&constants);
        free_hashtable(&opcodes);
        return -1;
    }

    if (write_memory(memory, out_file)) {
        free_list(&lines);
        free_hashtable(&constants);
        free_hashtable(&opcodes);
        return -1;
    }

    free_hashtable(&constants);
    free_hashtable(&opcodes);
    free(memory);
    free_list(&lines);
    return 0;
}

int main() {
    const char* in = "/mnt/c/dev/c/MIMAmulator/examples/prog.masm";
    const char* out = "/mnt/c/dev/c/MIMAmulator/examples/prog.mbf";

    if (assemble(in, out))
        return 1;

    return 0;
}


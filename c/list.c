//
// Created by Alex on 17.11.2024.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "list.h"

/** Initialise a dynamically growing list of strings.
 *
 * @return Pointer to the list.
 */
int init_list(list* l) {
    if (l == NULL)
        return 1;

    l->capacity = DEFAULT_SIZE;
    l->used=0;

    l->array = malloc(sizeof(char*) * l->capacity);
    if (l->array == NULL)
        return -1;
    return 0;
}

/** Frees the list and its content.
 *
 * @param l Pointer to the list.
 */
void free_list(list* l) {
    if (l == NULL)
        return;

    if (l->array != NULL) {
        // free all the deepcopies of strings
        for (int i = 0; i < l->used; ++i) {
            free(l->array[i]);
        }
        free(l->array);
        l->array = NULL;
    }
}

/** Adds new entry at the back of the list. The string will be copied.
 *
 * @param l Pointer to the list.
 * @param value Pointer to the char array to be added.
 * @return 0 on success,
 *        -1 on memory allocation failure.
 *        -2 if supplied argument was NULL
 */
int push(list* l, const char* value) {
    if (l == NULL || value == NULL) {
        return -2;
    }

    // realloc if there is no more slots left
    if (l->used >= l->capacity) {
        char** ptr = realloc(l->array, GROWTH_FACTOR * l->capacity * sizeof(char*));
        if (ptr == NULL)
            return -1;

        // on success redirect pointer
        l->array = ptr;
        l->capacity *= GROWTH_FACTOR;
    }

    // make a copy to avoid using stack based strings
    char* str = malloc(sizeof(char) * (strlen(value) + 1));
    if (str == NULL)
        return -1;

    strcpy(str, value);
    l->array[l->used] = str;
    l->used++;

    return 0;
}

/** Dirty, but handy way to quickly print the list in its entirety.
 *
 * @param l Pointer to the list.
 */
void pp_list(const list* l) {
    if (l == NULL) {
        printf("list was NULL, could not print");
        return;
    }

    printf("used %lu of %lu capacity.\n", l->used, l->capacity);
    for (size_t i = 0; i < l->used; ++i) {
        printf("l[%lu] = %s\n", i, l->array[i]);
    }
}
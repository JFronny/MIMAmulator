//
// Created by Alex on 15.11.2024.
//

#include <stdlib.h>
#include <string.h>
#include "mimamulator.h"

#include <stdio.h>

#include "logger.h"

// *---------------------
// MICROPROGRAMS
// *---------------------

word LOAD_CONST_microprogram[] = {
    WRITE_IR | READ_AKKU                    // IR -> Akku
};

Microprogram LOAD_CONST = {
    .program = LOAD_CONST_microprogram,
    .length = sizeof(LOAD_CONST_microprogram) / sizeof(LOAD_CONST_microprogram[0])
};

word LOAD_VALUE_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    READ_MEMORY,                           // R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_AKKU                  // SDR -> Akku
};

Microprogram LOAD_VALUE = {
    .program = LOAD_VALUE_microprogram,
    .length = sizeof(LOAD_VALUE_microprogram) / sizeof(LOAD_VALUE_microprogram[0])
};

word STORE_VALUE_microprogram[] = {
    WRITE_AKKU | READ_SDR,                 // Akku -> SDR
    WRITE_IR | READ_SAR | WRITE_MEMORY,    // IR -> SAR, W=1
    WRITE_MEMORY,                          // W=1
    WRITE_MEMORY,                          // W=1
};

Microprogram STORE_VALUE = {
    .program = STORE_VALUE_microprogram,
    .length = sizeof(STORE_VALUE_microprogram) / sizeof(STORE_VALUE_microprogram[0])
};

word ADD_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    WRITE_AKKU | READ_X | READ_MEMORY,     // Akku -> X, R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_Y,                    // SDR -> Y
    ALU_ADD,                               // ALU ADD
    WRITE_Z | READ_AKKU                    // Z -> Akku
};

Microprogram ADD = {
    .program = ADD_microprogram,
    .length = sizeof(ADD_microprogram) / sizeof(ADD_microprogram[0])
};

word AND_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    WRITE_AKKU | READ_X | READ_MEMORY,     // Akku -> X, R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_Y,                    // SDR -> Y
    ALU_AND,                               // ALU AND
    WRITE_Z | READ_AKKU                    // Z -> Akku
};

Microprogram AND = {
    .program = AND_microprogram,
    .length = sizeof(AND_microprogram) / sizeof(AND_microprogram[0])
};

word OR_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    WRITE_AKKU | READ_X | READ_MEMORY,     // Akku -> X, R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_Y,                    // SDR -> Y
    ALU_OR,                                // ALU OR
    WRITE_Z | READ_AKKU                    // Z -> Akku
};

Microprogram OR = {
    .program = OR_microprogram,
    .length = sizeof(OR_microprogram) / sizeof(OR_microprogram[0])
};

word XOR_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    WRITE_AKKU | READ_X | READ_MEMORY,     // Akku -> X, R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_Y,                    // SDR -> Y
    ALU_XOR,                               // ALU XOR
    WRITE_Z | READ_AKKU                    // Z -> Akku
};

Microprogram XOR = {
    .program = XOR_microprogram,
    .length = sizeof(XOR_microprogram) / sizeof(XOR_microprogram[0])
};

word EQL_microprogram[] = {
    WRITE_IR | READ_SAR | READ_MEMORY,     // IR -> SAR, R=1
    WRITE_AKKU | READ_X | READ_MEMORY,     // Akku -> X, R=1
    READ_MEMORY,                           // R=1
    WRITE_SDR | READ_Y,                    // SDR -> Y
    ALU_NEG_IF_EQL,                        // ALU EQL
    WRITE_Z | READ_AKKU                    // Z -> Akku
};

Microprogram EQL = {
    .program = EQL_microprogram,
    .length = sizeof(EQL_microprogram) / sizeof(EQL_microprogram[0])
};

word JMP_microprogram[] = {
    WRITE_IR | READ_IAR                     // IR -> IAR
};

Microprogram JMP = {
    .program = JMP_microprogram,
    .length = sizeof(JMP_microprogram) / sizeof(JMP_microprogram[0])
};

// TODO: this is part of the JMN-hack
word JMN_microprogram[] = {
    JMP_IF_NEG_HACK                         // IR -> IAR
};

Microprogram JMN = {
    .program = JMN_microprogram,
    .length = sizeof(JMN_microprogram) / sizeof(JMN_microprogram[0])
};

word HALT_microprogram[] = {};

Microprogram HALT = {
    .program = HALT_microprogram,
    .length = sizeof(HALT_microprogram) / sizeof(HALT_microprogram[0])
};

word NOT_microprogram[] = {
    WRITE_AKKU | READ_X,                    // Akku -> X
    ALU_NOT_OF_X,                           // ALU NOT
    WRITE_Z | READ_AKKU                     // Z -> Akku
};

Microprogram NOT = {
    .program = NOT_microprogram,
    .length = sizeof(NOT_microprogram) / sizeof(NOT_microprogram[0])
};

word RAR_microprogram[] = {
    WRITE_AKKU | READ_X,                    // Akku -> X
    ALU_ROR,                                // ALU NOT
    WRITE_Z | READ_AKKU                     // Z -> Akku
};

Microprogram RAR = {
    .program = RAR_microprogram,
    .length = sizeof(RAR_microprogram) / sizeof(RAR_microprogram[0])
};

Microprogram microprograms[31] = { 0 };


// *---------------------
// MIMA
// *---------------------

/** Initialize the MIMA
 *
 * @return the MIMA context
 */
MIMA* initMIMA() {

    MIMA* mima = malloc(sizeof(MIMA));
    if (mima == NULL) {
        return NULL;
    }

    *mima = (MIMA) {
        .Akku = 0,
        .Eins = 1,
        .IAR = 0,
        .IR = 0,
        .X=0,
        .Y = 0,
        .Z = 0,
        .SAR = 0,
        .SDR = 0,
        .read_mem_enabled_for = 0,
        .write_mem_enabled_for = 0,
        .simulated_instructions = 0,
        .memory = NULL
    };

    mima->memory = malloc(MIMA_MEM_SIZE * sizeof(word));
    if (mima->memory == NULL) {
        return NULL;
    }

    memset(mima->memory, 0, MIMA_MEM_SIZE * sizeof(word));

    initMicroprograms();

    return mima;
}

/** Initialize the Microprograms at the correct locations.
 *
 */
void initMicroprograms() {
    microprograms[0] = LOAD_CONST;
    microprograms[1] = LOAD_VALUE;
    microprograms[2] = STORE_VALUE;
    microprograms[3] = ADD;
    microprograms[4] = AND;
    microprograms[5] = OR;
    microprograms[6] = XOR;
    microprograms[7] = EQL;
    microprograms[8] = JMP;
    microprograms[9] = JMN;
    microprograms[HALT_LOCATION] = HALT;
    microprograms[16] = NOT;
    microprograms[17] = RAR;
}

/** Set the address at which the MIMA start executing
 *
 * @param mima The MIMA context
 * @param address The starting address
 */
void set_starting_address(MIMA* mima, word address) {
    mima->IAR = address & ADDRESS_MASK;
}

/** Frees up the memory used by the heap by the MIMA memory
 *
 * @param mima The MIMA context
 */
void freeMIMA(MIMA* mima) {
    if (mima->memory != NULL) {
        free(mima->memory);
        free(mima);
    }
}

/** Executes a single microop
 *
 * @param mima The MIMA context
 * @param microop The microop to be executed
 * @return -1 on failure
 *          0 on success
 *          1 on using JMN-Hack
 */
int executeMicroop(MIMA* mima, word microop) {

    // TODO: this is part of the JMN-Hack
    if (microop == JMP_IF_NEG_HACK) {
        if (mima->Akku & NEGATIVE_BIT_HACK_MASK) {
            mima->IAR = (mima->IR & ADDRESS_MASK);
            return 1;
        }
    }

    word BUS = 0;
    u32 BUS_USED = 0;

    // write to BUS (multiple creates conflict!)
    if (microop & WRITE_AKKU) {
        BUS = mima->Akku;
        BUS_USED++;
    }
    if (microop & WRITE_Z) {
        BUS = mima->Z;
        BUS_USED++;
    }
    if (microop & WRITE_EINS) {
        BUS = mima->Eins;
        BUS_USED++;
    }
    if (microop & WRITE_IAR) {
        BUS = mima->IAR;
        BUS_USED++;
    }
    if (microop & WRITE_IR) {
        BUS = mima->IR;
        BUS_USED++;
    }
    if (microop & WRITE_SDR) {
        BUS = mima->SDR;
        BUS_USED++;
    }

    if (BUS_USED > 1) {
        log_error("(Microcode exception) Conflict, multiple sources writing to the BUS");
        return -1;
    }

    // read from BUS (multiple allowed!)
    if (microop & READ_AKKU) {
        mima->Akku = BUS;
    }
    if (microop & READ_X) {
        mima->X = BUS;
    }
    if (microop & READ_Y) {
        mima->Y = BUS;
    }
    if (microop & READ_IAR) {
        mima->IAR = BUS & ADDRESS_MASK;
    }
    if (microop & READ_IR) {
        mima->IR = BUS;
    }
    if (microop & READ_SDR) {
        mima->SDR = BUS;
    }
    if (microop & READ_SAR) {
        mima->SAR = BUS & ADDRESS_MASK;
    }


    // ALU
    word ALU_OP = microop & ALU_MASK;

    if (ALU_OP == ALU_DO_NOTHING) {}
    else if (microop & (READ_X | READ_Y | WRITE_Z)) {
        log_warning("(Microcode warning) Don't use ALU and read/write to X, Y, Z in one microop");
        return -1;
    }
    if (ALU_OP == ALU_ADD) {
        mima->Z = (mima->X + mima->Y) & WORD_MASK;
    }
    if (ALU_OP == ALU_ROR) {
        const u32 n = 1; // TODO: Assuming ROR will alwasy rotate by one, not y.
        mima->Z = (mima->X >> n) | (mima->X << (24 - n));
    }
    if (ALU_OP == ALU_AND) {
        mima->Z = mima->X & mima->Y;
    }
    if (ALU_OP == ALU_OR) {
        mima->Z = mima->X | mima->Y;
    }
    if (ALU_OP == ALU_XOR) {
        mima->Z = mima->X ^ mima->Y;
    }
    if (ALU_OP == ALU_NOT_OF_X) {
        mima->Z = ~mima->X & WORD_MASK;
    }
    if (ALU_OP == ALU_NEG_IF_EQL) {
        mima->Z = (mima->X == mima->Y)? -1 : 0;
    }

    // MEMORY
    // READ
    if (microop & READ_MEMORY) {
        // Writing to the SAR register only allowed in the first cycle of reading memory!
        if ((microop & READ_SAR) && mima->read_mem_enabled_for > 0) {
            log_error("(Microcode exception) Written to SAR while trying to read memory.");
            return -1;
        }
        mima->read_mem_enabled_for++;
    } else {
        mima->read_mem_enabled_for = 0;
    }
    if (mima->read_mem_enabled_for >= 3) {
        // this shouldnt happen, but the memory is circular just in case
        mima->SDR = mima->memory[mima->SAR & (MIMA_MEM_SIZE - 1)] & WORD_MASK;
        mima->read_mem_enabled_for = 0;
    }

    // WRITE
    if (microop & WRITE_MEMORY) {
        // Writing to the SAR, SDR register only allowed in the first cycle of reading memory!
        if (microop & (READ_SAR | READ_SDR) && mima->write_mem_enabled_for > 0) {
            log_error("(Microcode exception) Written to SAR or SDR while trying to read memory.");
            return -1;
        }
        mima->write_mem_enabled_for++;
    } else {
        mima->write_mem_enabled_for = 0;
    }
    if (mima->write_mem_enabled_for >= 3) {
        // this shouldnt happen, but the memory is circular just in case
        mima->memory[mima->SAR & (MIMA_MEM_SIZE - 1)] = mima->SDR & WORD_MASK;
        mima->write_mem_enabled_for = 0;
    }

    return 0;
    // log_info("(Microcode) step executed!");
}

/** Executes a Microprogram
 *
 * @param mima The MIMA conext to execute
 * @param microprogram The Microprogram to execute
 * @return -2 on null pointer exception
 *         -1 on microcode failure
 *          0 on success
 */
int executeMicroprogram(MIMA* mima, Microprogram microprogram) {
    if (microprogram.program == NULL || mima == NULL) {
        return -2;
    }
    for (size_t i = 0; i < microprogram.length; i++) {
        if (executeMicroop(mima, microprogram.program[i]) == -1) {
            return -1;
        }
    }
    return 0;
}

/** The fetch phase of an Instruction.
 *
 * @param mima The MIMA context
 * @return -2 on null pointer exception
 *         -1 on failure
 *          0 on success
 */
int fetch(MIMA* mima) {
    word fetch_microprogram[] = {
        WRITE_IAR | READ_SAR | READ_X | READ_MEMORY, // IARSAR; IARX; R=1
        WRITE_EINS | READ_Y | READ_MEMORY,           // EinsY; R=1
        ALU_ADD | READ_MEMORY,                       // ALU add; R=1;
        WRITE_Z | READ_IAR,                          // ZIAR;
        WRITE_SDR | READ_IR                          // SDRIR;
    };

    Microprogram fetch = {
        .program = fetch_microprogram,
        .length = sizeof(fetch_microprogram) / sizeof(fetch_microprogram[0])
    };

    return executeMicroprogram(mima, fetch);
}

/** Decodes the OpCode and returns the location for the Microprogram table
 *
 * @param mima The MIMA context
 * @return The location of the Microprogram
 */
int decode(MIMA* mima) {
    // calculate the microprogram 'location'
    if ((mima->IR & HIGH_OP_CODE_MASK) == HIGH_OP_CODE_MASK) {
        // use full 8 bit Op code
        return ((mima->IR & OP_CODE_MASK & ~HIGH_OP_CODE_MASK) >> 16) + 15;
    }
    return (mima->IR & HIGH_OP_CODE_MASK) >> 20;
}

/** executes a single Instruction
 *
 * @param mima The MIMA context
 * @return -2 on null pointer exception
 *         -1 on failure
 *          0 on success
 *          1 on HALT
 */
int executeInstruction(MIMA* mima) {

    mima->simulated_instructions++;

    // CPU Phases
    // FETCH: execute the fetch microprogram
    int err = fetch(mima);
    if (err < 0) {
        return err;
    }

    // DECODE: opcode (first 8 bits of IR) is used to jump to the correct microprogram
    u32 microprogram_location = decode(mima);

    if (microprogram_location == HALT_LOCATION) {
        // catch HALT instruction
        return 1;
    }

    // EXECUTE: execute the microprogram
    return executeMicroprogram(mima, microprograms[microprogram_location]);
}
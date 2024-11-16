//
// Created by Alex on 15.11.2024.
//

#ifndef MIMAMULATOR_H
#define MIMAMULATOR_H

#include <stdint.h>

// 20 address bits => 2^20 = 1 048 576 words
#define MIMA_MEM_SIZE 1048576
#define WORD_SIZE 24
#define WORD_MASK 16777215 // = 2^24 - 1
#define ADDRESS_MASK 0x0FFFFF

// *---------------------
// MICROOPS:
// *---------------------

// Write means "Write to bus and into other registers"!
// Read means "Read from bus and put it into this registers"!
#define READ_AKKU (1 << 26)
#define WRITE_AKKU (1 << 25)

#define READ_X (1 << 24)
#define READ_Y (1 << 23)
#define WRITE_Z (1 << 22)

#define WRITE_EINS (1 << 21)

#define READ_IAR (1 << 20)
#define WRITE_IAR (1 << 19)

#define READ_IR (1 << 18)
#define WRITE_IR (1 << 17)

#define READ_SDR (1 << 16)
#define WRITE_SDR (1 << 15)

#define READ_SAR (1 << 14)

// ALU setting
#define ALU_DO_NOTHING 0
#define ALU_ADD (1 << 11)
#define ALU_ROR (2 << 11)
#define ALU_AND (3 << 11)
#define ALU_OR (4 << 11)
#define ALU_XOR (5 << 11)
#define ALU_NOT_OF_X (6 << 11)
#define ALU_NEG_IF_EQL (7 << 11)
#define ALU_MASK ALU_NEG_IF_EQL

#define READ_MEMORY (1 << 10)
#define WRITE_MEMORY (1 << 9)

// TODO: can this be done in a better way? This is part of the JMN-Hack
// this is a hack, we use an unused microcode bit to signal that we want to jump if Akku < 0 (bit 24 is 1 in Akku)
#define JMP_IF_NEG_HACK (1<<27)
#define NEGATIVE_BIT_HACK_MASK (1 << 23)

// *---------------------
// OPCODES
// *---------------------

#define OP_CODE_MASK 0xFF000
#define HIGH_OP_CODE_MASK 0xF00000

#define HALT_LOCATION 15 //0xF0

typedef uint64_t u64;
typedef uint32_t u32;
typedef u32 word;

typedef struct {
    word Akku;
    word Eins;
    word IAR;
    word IR;
    word X;
    word Y;
    word Z;
    word SDR;
    word SAR;
    u64 read_mem_enabled_for;
    u64 write_mem_enabled_for;
    u64 simulated_instructions;
    word* memory;
} MIMA;

typedef struct {
    size_t length;
    word* program;
} Microprogram;

MIMA* initMIMA();
void initMicroprograms();
void freeMIMA(MIMA* mima);
int executeMicroop(MIMA* mima, word microop);
int executeMicroprogram(MIMA* mima, Microprogram microprogram);
int fetch(MIMA* mima);
int decode(MIMA* mima);
int executeInstruction(MIMA* mima);

#endif //MIMAMULATOR_H

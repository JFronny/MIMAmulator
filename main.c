#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "logger.h"
#include "mimamulator.h"

void pp_mem(MIMA* mima, word start, word length) {
    start &= ADDRESS_MASK;
    length &= ADDRESS_MASK;
    word end = start + length;

    if (end & ~ADDRESS_MASK) {
        printf("too long, unable to print.\n");
        return; // TOO LONG
    }

    printf("\nMIMA MEMORY ADDRESS %d to %d\n", start, end);
    printf("---------------\n");
    printf("ADDRESS |   VALUE HEX         DECIMAL\n");
    for (int i = start; i < end; ++i) {
        printf("  %5X |      %6X      %10d\n", i, mima->memory[i], mima->memory[i]);
    }
}

/** Use all instructions to verify that they are working
 *
 */
void basic_test_all_instructions_program() {
    MIMA* mima = initMIMA();
    if (mima == NULL) {
        return;
    }

    mima->memory[0] = 0xB0F0;
    mima->memory[1] = 0xE;
    mima->memory[2] = ~0x10 & 0x00FFFFFF;
    mima->memory[3] = 0x1;
    mima->memory[4] = 0xE00;


    mima->memory[0x0A] = 0x100000; // LDV 0x0
    mima->memory[0x0B] = 0x300001; // ADD 0x1
    mima->memory[0x0C] = 0x400002; // AND 0x2
    mima->memory[0x0D] = 0x500003; // OR  0x3
    mima->memory[0x0E] = 0x600004; // XOR 0x4
    mima->memory[0x0F] = 0x200006; // STV 0x6

    mima->memory[0x10] = 0xF20000; // RAR
    mima->memory[0x11] = 0xF20000; // RAR
    mima->memory[0x12] = 0xF20000; // RAR
    mima->memory[0x13] = 0xF20000; // RAR
    mima->memory[0x14] = 0x200007; // STV 0x7

    mima->memory[0x15] = 0x000000; // LDC 0
    mima->memory[0x16] = 0xF10000; // NOT
    mima->memory[0x17] = 0x200008; // STV 0x8

    mima->memory[0x18] = 0x000050; // LDC 0x50
    mima->memory[0x19] = 0xF10000; // NOT
    mima->memory[0x1A] = 0x300003; // ADD 0x3
    mima->memory[0x1B] = 0x900030; // JMN 0x30
    mima->memory[0x1C] = 0x000DAD; // LDC 0xDAD
    mima->memory[0x1D] = 0x200009; // STV 0x9

    mima->memory[0x30] = 0x200009; // STV 0x9

    mima->memory[0x31] = 0xF00000; // HALT

    mima->IAR = 10;

    while (1) {
        int status = executeInstruction(mima);
        printf("akku: %0x, iar:%0x\n", mima->Akku, mima->IAR);
        if (status == 1) {
            printf("HALT. MIMA stopped. Ran for %" PRIu64 " instructions.\n", mima->simulated_instructions);
            break;
        }
        if (status < 0) {
            log_error("MIMA crashed.");
            break;
        }
    }
    pp_mem(mima, 0, 11);

    // memory should look like this:

//     MIMA MEMORY ADDRESS 0 to 11
//     ---------------
//     ADDRESS |   VALUE HEX         DECIMAL
//           0 |        B0F0           45296
//           1 |           E              14
//           2 |      FFFFEF        16777199
//           3 |           1               1
//           4 |         E00            3584
//           5 |           0               0
//           6 |        BEEF           48879
//           7 |      F00BEE        15731694
//           8 |      FFFFFF        16777215
//           9 |      FFFFB0        16777136
//           A |      100000         1048576


    freeMIMA(mima);
}

/** continously add 10 to 0x1
 *
 */
void continuos_add_program() {
    MIMA* mima = initMIMA();
    if (mima == NULL) {
        return;
    }

    mima->memory[0] = 10;
    mima->memory[0xB] = 0x300000; // ADD 0x0
    mima->memory[0xC] = 0x200001; // STV 0x1
    mima->memory[0xD] = 0x80000B; // JMP 0xA
    mima->memory[0xE] = 0xF00000; // HALT

    mima->IAR = 0xB;

    u64 i = 0;
    clock_t start = clock();
    while (i < 50000) {
        int status = executeInstruction(mima);
        if (status == 1) {
            printf("HALT. MIMA stopped. Ran for %" PRIu64 " instructions.\n", mima->simulated_instructions);
            break;
        }
        if (status < 0) {
            log_error("MIMA crashed.");
            break;
        }
        // printf("\033[H\033[J");
        // pp_mem(mima, 0, 5);
        i++;
    }
    float seconds = (float) (clock() - start) / CLOCKS_PER_SEC;

    printf("HALT. MIMA stopped. ");
    printf("Took %f seconds for %" PRIu64 " instructions at %f MHz.\n", seconds, mima->simulated_instructions, (float)mima->simulated_instructions / (seconds * 1000000));

    pp_mem(mima, 0, 20);

    freeMIMA(mima);
}



int main(void) {
    basic_test_all_instructions_program();
    // continuos_add_program();

    return 0;
}

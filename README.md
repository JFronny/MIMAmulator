# MIMAmulator

Suite of tools and emulators for the [KITs](https://kit.edu) MIMA (Minimalmaschine, a 'minimal processor').

## About the MIMA
- The MIMA is a 24 bit word machine
- The memory is addressed using 20 bits, so 1MiB of primary memory
- There is only one Register: Akku

### OpCodes
The MIMA uses either 4 bits or 8 bits for the OpCode. If the Instruction begins with 0xF the MIMA will interpret the first 8 bits as OpCpde.

| Format 1 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 20 | 19 to 0             |

| Format 2 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 16 | 15 to 0             |

The underscore in 0x0_ signifies that the second leftmost byte is used as part of the address.

| OpCode | Instruction | Action                           |
|--------|-------------|----------------------------------|
| 0x0_   | LDC c       | c → Akku (!! only lower 20 bits) |
| 0x1_   | LDV a       | *a → Akku                        |
| 0x2_   | STV a       | Akku → *a                        |
| 0x3_   | ADD a       | (Akku + *a) → Akku               |
| 0x4_   | AND a       | (Akku AND *a) → Akku             |
| 0x5_   | OR a        | (Akku OR *a) → Akku              |
| 0x6_   | XOR a       | (Akku XOR *a) → Akku             |
| 0x7_   | EQL a       | if (Akku == *a): -1 → Akku       |
|        |             | else: 0 → Akku                   |
| 0x8_   | JMP a       | a → IAR                          |
| 0x9_   | JMN a       | if (Akku < 0): a → IAR           |
| 0xA_   | -           | -                                |
| 0xB_   | -           | -                                |
| 0xC_   | -           | -                                |
| 0xD_   | -           | -                                |
| 0xE_   | -           | -                                |
| 0xF0   | HALT        | stop the MIMA                    |
| 0xF1   | NOT         | (NOT Akku) → Akku                |
| 0xF2   | RAR         | rotate Akku 1 to the right       |
| 0xF3   | -           | -                                |
| 0xF4   | -           | -                                |
| 0xF5   | -           | -                                |
| 0xF6   | -           | -                                |
| 0xF7   | -           | -                                |
| 0xF8   | -           | -                                |
| 0xF9   | -           | -                                |
| 0xFA   | -           | -                                |
| 0xFB   | -           | -                                |
| 0xFC   | -           | -                                |
| 0xFD   | -           | -                                |
| 0xFE   | -           | -                                |
| 0xFF   | -           | -                                |

## The Kotlin implementation
Initially supplied by JFronny, this implementation provides an assembler, a disassembler and an interpreter for the MIMA.

Use `gradle build` to build a jar and `java -jar build/libs/mima-kotlin.jar` to view a help message.

## The C implementation
Provides a simulation on the microcode level of a MIMA as well as an assembler.
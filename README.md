# MIMAmulator

Emulator for the [KITs](https://kit.edu) MIMA (Minimalmaschine, a 'minimal processor').

## General
- 24 Bits in a word
- 20 Address bits
  - 2^20 = 1048576 words of memory

### Registers
- Akku

## OpCodes
### Format
The MIMA uses either 4 bits or 8 bits for the OpCode. If the Instruction begins with 0xF the MIMA will interpret the first 8 bits as OpCpde.

| Format 1 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 20 | 19 to 0             |

| Format 2 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 16 | 15 to 0             |

### OpCode List

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

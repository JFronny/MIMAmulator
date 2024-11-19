# Architecure Overview
- 24 Bits in a word
- 20 Address bits
    - 2^20 = 1048576 words of memory
- Exactly one register: Akku

# Instruction Format
The MIMA uses either 4 bits or 8 bits for the OpCode. If the Instruction begins with 0xF the MIMA will interpret the first 8 bits as OpCpde.

| Format 1 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 20 | 19 to 0             |

| Format 2 |   OpCode | Address or constant |
|----------|---------:|---------------------|
| Bits     | 24 to 16 | 15 to 0             |

# Instructions
To view the documentation for an extension, find the documentation for the extension in [this](./extensions) directory.

| OpCode | Instruction | Action                           | Extension |
|--------|-------------|----------------------------------|-----------|
| 0x0_   | LDC c       | c → Akku (!! only lower 20 bits) | Base      |
| 0x1_   | LDV a       | *a → Akku                        | Base      |
| 0x2_   | STV a       | Akku → *a                        | Base      |
| 0x3_   | ADD a       | (Akku + *a) → Akku               | Base      |
| 0x4_   | AND a       | (Akku AND *a) → Akku             | Base      |
| 0x5_   | OR a        | (Akku OR *a) → Akku              | Base      |
| 0x6_   | XOR a       | (Akku XOR *a) → Akku             | Base      |
| 0x7_   | EQL a       | if (Akku == *a): -1 → Akku       | Base      |
|        |             | else: 0 → Akku                   |           |
| 0x8_   | JMP a       | a → IAR                          | Base      |
| 0x9_   | JMN a       | if (Akku < 0): a → IAR           | Base      |
| 0xA_   | -           | -                                |           |
| 0xB_   | -           | -                                |           |
| 0xC_   | -           | -                                |           |
| 0xD_   | -           | -                                |           |
| 0xE_   | -           | -                                |           |
| 0xF0   | HALT        | stop the MIMA                    | Base      |
| 0xF1   | NOT         | (NOT Akku) → Akku                | Base      |
| 0xF2   | RAR         | rotate Akku 1 to the right       | Base      |
| 0xF3   | IN a        | IO port *a → Akku                | IO        |
| 0xF4   | OUT a       | Akku → IO port *a                | IO        |
| 0xF5   | -           | -                                |           |
| 0xF6   | -           | -                                |           |
| 0xF7   | -           | -                                |           |
| 0xF8   | -           | -                                |           |
| 0xF9   | -           | -                                |           |
| 0xFA   | -           | -                                |           |
| 0xFB   | -           | -                                |           |
| 0xFC   | -           | -                                |           |
| 0xFD   | -           | -                                |           |
| 0xFE   | -           | -                                |           |
| 0xFF   | -           | -                                |           |
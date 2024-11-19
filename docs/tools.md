# Obtaining the tools
We use GitHub Actions to build the tools and provide them as artifacts.
If you don't want to build the tools yourself, you can download them there.
The Actions build should not need any dependencies you don't already have, so using them should be easy.

# mima-kotlin
Initially supplied by JFronny, this implementation provides an assembler, a disassembler and a high-performance (~30MHz) machine-code interpreter for the MIMA.

Use `gradle build` to build a jar and `java -jar build/libs/mima-kotlin.jar` to view a help message.

If you have downloaded a CI build, just `./mima-kotlin` should work.

# mima-c
Initially supplied by Alexander Klee, this implementation provides an assembler, a disassembler and a high-performance (~10MHz) microcode interpreter for the MIMA.

Use `cmake` to build the project.
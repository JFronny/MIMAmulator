cmake_minimum_required(VERSION 3.22)
project(c C)

set(CMAKE_C_STANDARD 11)

add_executable(
        Mimamulator main.c
        mimamulator.c
        mimamulator.h
        logger.c
        logger.h
        list.c
)

add_executable(
        assembler assembler.c
        list.c
        list.h
        hash.c
        hash.h
        logger.c
        logger.h
)
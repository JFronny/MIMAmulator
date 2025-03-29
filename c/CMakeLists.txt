cmake_minimum_required(VERSION 3.22)
project(c C)

set(CMAKE_C_STANDARD 11)

if (CMAKE_BUILD_TYPE MATCHES Release)
    SET(CMAKE_C_FLAGS  "${CMAKE_C_FLAGS} -O3")
endif()

add_executable(
        Mimamulator main.c
        mimamulator.c
        mimamulator.h
        logger.c
        logger.h
        list.c
)

add_executable(
        Assembler assembler.c
        list.c
        list.h
        hash.c
        hash.h
        logger.c
        logger.h
)
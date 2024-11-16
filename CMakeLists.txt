cmake_minimum_required(VERSION 3.22)
project(Mimamulator C)

set(CMAKE_C_STANDARD 11)

add_executable(Mimamulator main.c
        mimamulator.c
        mimamulator.h
        logger.c
        logger.h)

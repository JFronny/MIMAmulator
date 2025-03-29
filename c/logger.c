//
// Created by Alex on 15.11.2024.
//
#include "logger.h"

#include <stdio.h>
#include <stdlib.h>


void log_error(char* msg) {
    if (VERBOSE) {
        printf("ERROR: %s. EXITING.\n", msg);
        exit(EXIT_FAILURE);
    }
}

void log_warning(char* msg) {
    if (VERBOSE) {
        printf("WARNING: %s.\n", msg);
    }
}

void log_info(char* msg) {
    if (VERBOSE) {
        printf("INFO: %s.\n", msg);
    }
}
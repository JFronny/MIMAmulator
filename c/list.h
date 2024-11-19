//
// Created by Alex on 17.11.2024.
//

#ifndef LIST_H
#define LIST_H

#define DEFAULT_SIZE 10
#define GROWTH_FACTOR 2

typedef struct {
    size_t capacity;
    size_t used;
    char** array;
} list;

int init_list(list* l);
void free_list(list* l);
int push(list* l, const char* value);
void pp_list(const list* l);

#endif //LIST_H

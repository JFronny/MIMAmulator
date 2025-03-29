#ifndef HASH_H
#define HASH_H

#include <stddef.h>
#include <inttypes.h>

typedef struct {
	int valid;
	char *key;
	int value;
} element;

typedef struct {
	element *elements;
	size_t capacity;
	size_t used;
} hashtable;

uint64_t hashString(const char *c);
int insert(hashtable *table, char *key, int value);
int find(const hashtable *table, const char *key, int *value);
int init_hashtable(hashtable* ht, size_t size);
void free_hashtable(hashtable* ht);
void pp_hashtable(const hashtable* ht);

#endif

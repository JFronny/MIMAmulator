#include "hash.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/** Hashes a string
 *
 * @param c The string
 * @return The hash
 */
uint64_t hashString(const char *c)
{
	uint64_t hash = 5381;
	for (int i = 0; c[i]; i++) {
		hash = hash*33 + c[i];
	}
	return hash;
}

/** Inserts an element in the hashtable
 *
 * @param table The hashtable
 * @param key The key for the value
 * @param value The value
 * @return 0 on success, 1 if table is full, -1 on failure
 */
int insert(hashtable *table, char *key, const int value) {
	char* key_cpy = malloc((strlen(key) + 1) * sizeof(char));
	if (key_cpy == NULL)
		return -1;
	strcpy(key_cpy, key);

	const uint64_t hash = hashString(key_cpy);
	size_t offset = 0;

	while (table->elements[(hash + offset) % table->capacity].valid) {
		if (strcmp(table->elements[(hash + offset) % table->capacity].key, key_cpy) == 0)
			// element has the same hash
			break;
		if (offset >= table->capacity) {
			// hash table is full
			free(key_cpy);
			return 1;
		}

		offset++;
	}

	table->elements[(hash + offset) % table->capacity].value = value;
	table->elements[(hash + offset) % table->capacity].key = key_cpy;
	table->elements[(hash + offset) % table->capacity].valid = 1;
	table->used++;
	return 0;
}

/** Tries to find the key in the hashtable
 *
 * @param table The hashtable
 * @param key The key of the element
 * @param value Will store the value of the element
 * @return 0 if found, 1 otherwise
 */
int find(const hashtable *table, const char *key, int *value) {
	const uint64_t hash = hashString(key);
	size_t offset = 0;

	while (1) {
		if (!table->elements[(hash + offset) % table->capacity].valid)
			// not found in table
			return 1;
		if (strcmp(table->elements[(hash + offset) % table->capacity].key, key) == 0) {
			// element has the same key
			*value = table->elements[(hash + offset) % table->capacity].value;
			return 0;
		}
		if (offset >= table->capacity)
			// not found in table
			return 1;

		offset++;
	}

}

/** Initializes the hashtable.
 *
 * @param ht The hashtable to be initialized.
 * @param size The size for the new hashtable
 * @return 0 on succes, -1 on failure
 */
int init_hashtable(hashtable* ht, const size_t size) {
	element* array = malloc(size * sizeof(element));
	if (array == NULL)
		return -1;
	memset(array, 0, size * sizeof(element));

	ht->capacity = size;
	ht->used = 0;
	ht->elements = array;
	return 0;
}

/** Frees a hashtable structure
 *
 * @param ht The hashtable
 */
void free_hashtable(hashtable* ht) {
	if (ht->elements == NULL)
		return;

	for (int i = 0; i < ht->capacity; ++i) {
		if (ht->elements[i].key != NULL) {
			free(ht->elements[i].key);
			ht->elements[i].key = NULL;
		}
	}

	free(ht->elements);
	ht->elements = NULL;
}

/** Quick and dirty way to print the hashtable
 *
 * @param ht The hashtable
 */
void pp_hashtable(const hashtable* ht) {
	printf("HASHTABLE: filled %lu of %lu.\n", ht->used, ht->capacity);
	printf("hash   | value    | key\n");
	printf("---------------------------\n");
	element e;
	for (int i = 0; i < ht->capacity; ++i) {
		e = ht->elements[i];
		if (e.valid)
			printf("%6d | %8d | \'%s\'\n", i, e.value, e.key);
	}
}
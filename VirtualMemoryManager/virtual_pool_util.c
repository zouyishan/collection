//
// Created by bytedance on 2021/8/3.
//
#include "virtual_pool_util.h"

void* zys_malloc(size_t size) {
    void* data;
    data = malloc(size);
    if (data == NULL) {
        fprintf(stderr, "malloc error \n");
    }
    return data;
}

void* zys_mem_align(size_t alignment, size_t size) {
    void* data;
    if (posix_memalign(&data, alignment, size)) {
        fprintf(stderr, "posix_memalign error\n");
        data = NULL;
    }
    return data;
}
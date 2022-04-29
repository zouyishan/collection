//
// Created by bytedance on 2021/8/3.
//

#ifndef VIRTUALMEMORYMANAGER_VIRTUAL_POOL_UTIL_H
#define VIRTUALMEMORYMANAGER_VIRTUAL_POOL_UTIL_H
#include <stddef.h>
#include <stdio.h>
#include <mm_malloc.h>
#include <stdlib.h>
void* zys_malloc(size_t size);
void* zys_mem_align(size_t alignment, size_t size);
#endif //VIRTUALMEMORYMANAGER_VIRTUAL_POOL_UTIL_H

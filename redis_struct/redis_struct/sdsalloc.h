//
// Created by bytedance on 2021/6/25.
//

#ifndef UNTITLED1_SDSALLOC_H
#define UNTITLED1_SDSALLOC_H
#include "zmalloc.h"
//#define s_malloc zmalloc
//#define s_realloc zrealloc
//#define s_trymalloc ztrymalloc
//#define s_tryrealloc ztryrealloc
#define s_free zfree
#define s_malloc_usable zmalloc_usable
#define s_realloc_usable zrealloc_usable
#define s_trymalloc_usable ztrymalloc_usable
//#define s_tryrealloc_usable ztryrealloc_usable
//#define s_free_usable zfree_usable
#endif //UNTITLED1_SDSALLOC_H

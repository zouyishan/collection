//
// Created by bytedance on 2021/8/3.
//

#ifndef VIRTUALMEMORYMANAGER_VIRTUAL_POOL_H
#define VIRTUALMEMORYMANAGER_VIRTUAL_POOL_H

#include <stddef.h>

typedef void (*zys_virtualr_clean_handler)(void *data);

typedef struct zys_vritualr_pool_data_r zys_vritualr_pool_data;  // 内存结构体数据区域
typedef struct zys_virtualr_large_r zys_virtualr_large;          // 大内存分配
typedef struct zys_virtualr_clean_r zys_virtualr_clean;          // 析构函数，清理内存
typedef struct zys_virtualr_pool_r zys_virtualr_pool;            // 内存结构体头

#define zys_free free
#define ZYS_ALIGN 16

//TODO 这个有待考究
#define MAXSIZE   20
typedef unsigned char u_char;

#define zys_align_ptr(p, a) \
        (u_char *) (((uintptr_t) (p) + ((uintptr_t) a - 1)) & ~((uintptr_t) a - 1))
/**
 * 兼容ANSI标准，用unsigned char为标准
 */
struct zys_vritualr_pool_data_r {
    unsigned char *last;     // 上一次分配的结束地址
    unsigned char *end;      // 当前内存结束地址
    zys_virtualr_pool *next; // 下一个内存池
    int failed;              // 分配失败的次数
};

// 大块内存
struct zys_virtualr_large_r {
    zys_virtualr_large *next;
    void *data; // 大内存开始的首地址
};

// 析构函数
struct zys_virtualr_clean_r {
    zys_virtualr_clean_handler handler; // 函数指针
    void *data;               // 对应的内存
    zys_virtualr_clean *next; // 下一个析构函数
};

struct zys_virtualr_pool_r {
    zys_vritualr_pool_data *data; // 数据项
    zys_virtualr_pool *current;   // 当前内存池指针
    size_t max;                   // 判断为大内存的阀值
    zys_virtualr_large *large;    // 大内存的分配
    zys_virtualr_clean *clean;    // 析构函数, 销毁内存
};
#endif //VIRTUALMEMORYMANAGER_VIRTUAL_POOL_H

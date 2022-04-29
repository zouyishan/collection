//
// Created by bytedance on 2021/6/25.
//

#ifndef EASY_SDS_H
#define EASY_SDS_H

#define SDS_MAX_PREALLOC (1024*1024)
extern const char *SDS_NOINIT;

#include <sys/types.h>
#include <stdarg.h>
#include <stdint.h>

// 兼容c的字符串。
typedef char *sds;

/**
 * __attribute__ ((__packed__)) 编译时提醒紧凑分配内存，而不是字节对其.
 * 这里不会使用sdshdr5，只是用来凑数量的
 */
struct  __attribute__ ((__packed__)) sdshdr5 {
    unsigned char flags; /* 3位表示类型，五位不用*/
    char buf[];  /* 初始时不分配内存 */
};

struct __attribute__ ((__packed__)) sdshdr8 {
    uint8_t len; /* 用了的数量 */
    uint8_t alloc; /* 实际分配的内存 */
    unsigned char flags;
    char buf[];
};

struct __attribute__ ((__packed__)) sdshdr16 {
    uint16_t len; /* 用了的数量 */
    uint16_t alloc; /* 实际分配的内存 */
    unsigned char flags;
    char buf[];
};

struct __attribute__ ((__packed__)) sdshdr32 {
    uint32_t len; /* 用了的数量 */
    uint32_t alloc; /* 实际分配的内存 */
    unsigned char flags;
    char buf[];
};

struct __attribute__ ((__packed__)) sdshdr64 {
    uint64_t len; /* 用了的数量 */
    uint64_t alloc; /* 实际分配的内存 */
    unsigned char flags;
    char buf[];
};

// 定义不同的sds结构
#define SDS_TYPE_5  0
#define SDS_TYPE_8  1
#define SDS_TYPE_16 2
#define SDS_TYPE_32 3
#define SDS_TYPE_64 4
#define SDS_TYPE_MASK 7
#define SDS_TYPE_BITS 3
#define SDS_HDR_VAR(T, s) struct sdshdr##T *sh = (void*)((s) - (sizeof(struct sdshdr##T)))
#define SDS_HDR(T, s) ((struct sdshdr##T *)((s) - (sizeof(struct sdshdr##T))))
#define SDS_TYPE_5_LEN(f) ((f) >> SDS_TYPE_BITS)

// O(1) 获取字符串长度
static inline size_t sdsLen(const sds s) {
    unsigned char flags = s[-1];
    switch(flags & SDS_TYPE_MASK) {
        case SDS_TYPE_5:
            return SDS_TYPE_5_LEN(flags);
        case SDS_TYPE_8:
            return SDS_HDR(8, s) -> len;
        case SDS_TYPE_16:
            return SDS_HDR(16, s) -> len;
        case SDS_TYPE_32:
            return SDS_HDR(32, s) -> len;
        case SDS_TYPE_64:
            return SDS_HDR(64, s) -> len;
    }
    return 0;
}

static inline int sdsHdrSize(char type) {
    switch(type & SDS_TYPE_MASK) {
        case SDS_TYPE_5:
            return sizeof(struct sdshdr5);
        case SDS_TYPE_8:
            return sizeof(struct sdshdr8);
        case SDS_TYPE_16:
            return sizeof(struct sdshdr16);
        case SDS_TYPE_32:
            return sizeof(struct sdshdr32);
        case SDS_TYPE_64:
            return sizeof(struct sdshdr64);
    }
    return 0;
}

static inline void sdsSetLen(sds s, size_t newlen) {
    unsigned char flags = s[-1];
    switch(flags & SDS_TYPE_MASK) {
        case SDS_TYPE_5:
        {
            unsigned char *fp = ((unsigned char*)s) - 1;
            *fp = SDS_TYPE_5 | (newlen << SDS_TYPE_BITS);
        }
            break;
        case SDS_TYPE_8:
            SDS_HDR(8, s) -> len = newlen;
            break;
        case SDS_TYPE_16:
            SDS_HDR(16, s) -> len = newlen;
            break;
        case SDS_TYPE_32:
            SDS_HDR(32, s) -> len = newlen;
            break;
        case SDS_TYPE_64:
            SDS_HDR(64, s) -> len = newlen;
            break;
    }
}

static inline size_t sdsTypeMaxSize(char type) {
    if (type == SDS_TYPE_5)
        return (1<<5) - 1;
    if (type == SDS_TYPE_8)
        return (1<<8) - 1;
    if (type == SDS_TYPE_16)
        return (1<<16) - 1;
#if (LONG_MAX == LLONG_MAX)
    if (type == SDS_TYPE_32)
        return (1ll<<32) - 1;
#endif
    return -1; /* this is equivalent to the max SDS_TYPE_64 or SDS_TYPE_32 */
}

static inline size_t sdsavail(const sds s) {
    unsigned char flags = s[-1];
    switch(flags&SDS_TYPE_MASK) {
        case SDS_TYPE_5: {
            return 0;
        }
        case SDS_TYPE_8: {
            SDS_HDR_VAR(8,s);
            return sh->alloc - sh->len;
        }
        case SDS_TYPE_16: {
            SDS_HDR_VAR(16,s);
            return sh->alloc - sh->len;
        }
        case SDS_TYPE_32: {
            SDS_HDR_VAR(32,s);
            return sh->alloc - sh->len;
        }
        case SDS_TYPE_64: {
            SDS_HDR_VAR(64,s);
            return sh->alloc - sh->len;
        }
    }
    return 0;
}

static inline size_t sdsalloc(const sds s) {
    unsigned char flags = s[-1];
    switch(flags&SDS_TYPE_MASK) {
        case SDS_TYPE_5:
            return SDS_TYPE_5_LEN(flags);
        case SDS_TYPE_8:
            return SDS_HDR(8,s)->alloc;
        case SDS_TYPE_16:
            return SDS_HDR(16,s)->alloc;
        case SDS_TYPE_32:
            return SDS_HDR(32,s)->alloc;
        case SDS_TYPE_64:
            return SDS_HDR(64,s)->alloc;
    }
    return 0;
}

static inline char sdsReqType(size_t string_size) {
    if (string_size < 1 << 5) {
        return SDS_TYPE_5;
    } else if (string_size < 1<<8) {
        return SDS_TYPE_8;
    } else if (string_size < 1<<16) {
        return SDS_TYPE_16;
    }
    // 兼容32位机和64位机
#if (LONG_MAX == LLONG_MAX)
    if (string_size < 1ll<<32)
        return SDS_TYPE_32;
    return SDS_TYPE_64;
#else
    return SDS_TYPE_32;
#endif
}

static inline void sdsSetAlloc(sds s, size_t newlen) {
    unsigned char flags = s[-1];
    switch(flags&SDS_TYPE_MASK) {
        case SDS_TYPE_5:
            /* Nothing to do, this type has no total allocation info. */
            break;
        case SDS_TYPE_8:
            SDS_HDR(8,s)->alloc = newlen;
            break;
        case SDS_TYPE_16:
            SDS_HDR(16,s)->alloc = newlen;
            break;
        case SDS_TYPE_32:
            SDS_HDR(32,s)->alloc = newlen;
            break;
        case SDS_TYPE_64:
            SDS_HDR(64,s)->alloc = newlen;
            break;
    }
}

void *sds_malloc(size_t size);
void *sds_realloc(void *ptr, size_t size);
void sds_free(void *ptr);
#endif //EASY_SDS_H

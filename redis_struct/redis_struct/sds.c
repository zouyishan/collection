//
// Created by bytedance on 2021/6/25.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <assert.h>
#include <limits.h>
#include "sds.h"
#include "sdsalloc.h"

/**
 * 如果创建一个sds的时候，传入的init是这个，则不会将原有的内存的值清空
 */
const char *SDS_NOINIT = "SDS_NOINIT";

void sdsfree(sds s) {
    if (s == NULL) return;
    s_free((char*)s-sdsHdrSize(s[-1]));
}

sds _sdsNewLen(const void *init, size_t initlen, int trymalloc) {
    // 还有无类型指针!!!! 扩展性yyds
    void *sh;
    sds s;
    // 返回大小对应的类型
    char type = sdsReqType(initlen);
    // 除真正字符数组的长度
    int hdrlen = sdsHdrSize(type);

    unsigned char *fp;
    size_t usable;

    // +1表示字符串的结束符
    assert(initlen + hdrlen + 1 > initlen); // stack overflow

    sh = trymalloc ? s_trymalloc_usable(hdrlen + initlen + 1, &usable) : s_malloc_usable(hdrlen + initlen + 1, &usable);
    if (sh == NULL) return NULL;
    // 是否初始化这块内存
    if (init == SDS_NOINIT) {
        init = NULL;
    } else if(!init) {
        memset(sh, 0, hdrlen + initlen + 1);
    }

    s = (char *)sh + hdrlen;
    fp = ((unsigned char *)s) - 1; // flag
    usable = usable - hdrlen - 1;
    if (usable > sdsTypeMaxSize(type)) {
        usable = sdsTypeMaxSize(type);
    }

    switch (type) {
        case SDS_TYPE_5: {
            *fp = type | (initlen << SDS_TYPE_BITS);
            break;
        }
        case SDS_TYPE_8: {
            SDS_HDR_VAR(8, s);
            sh -> len = initlen;
            sh -> alloc = usable;
            *fp = type;
            break;
        }
        case SDS_TYPE_16: {
            SDS_HDR_VAR(16, s);
            sh -> len = initlen;
            sh -> alloc = usable;
            *fp = type;
            break;
        }
        case SDS_TYPE_32: {
            SDS_HDR_VAR(32, s);
            sh -> len = initlen;
            sh -> alloc = usable;
            *fp = type;
            break;
        }
        case SDS_TYPE_64: {
            SDS_HDR_VAR(64, s);
            sh -> len = initlen;
            sh -> alloc = usable;
            *fp = type;
            break;
        }
    }
    if (initlen && init) {
        memcpy(s, init, initlen);
    }
    s[initlen] = '\0';
    return s;
}

sds sdsNewLen(const void *init, size_t initlen) {
    return _sdsNewLen(init, initlen, 0);
}

sds sdsTryNewLen(const void *init, size_t initlen) {
    return _sdsNewLen(init, initlen, 1);
}

sds sdsNew(const char *init) {
    size_t initlen = (init == NULL) ? 0 : strlen(init);
    return sdsNewLen(init, initlen);
}

/**
 * 防止缓冲区溢出，如果新的长度小于SDS_MAX_PREALLOC(1024 * 1024 == 1MB)则翻倍，如果大于就多分配1MB。预分配
 *
 * @param s
 * @param addlen
 * @param greedy
 * @return
 */
sds _sdsMakeRoomFor(sds s, size_t addlen, int greedy) {
    void *sh, *newsh;
    size_t avail = sdsavail(s);
    size_t len, newlen;
    char type, oldtype = s[-1] & SDS_TYPE_MASK;
    int hdrlen;
    size_t usable;

    // 如果原来的空间足够大，直接返回
    if (avail >= addlen) return s;

    len = sdsLen(s);
    sh = (char*)s - sdsHdrSize(oldtype);
    newlen = (len + addlen);
    // 溢出
    assert(newlen > len);
    if (greedy == 1) {
        // 如果新的长度小于SDS_MAX_PREALLOC,则长度翻倍，不然就加上SDS_MAX_PREALLOC(1 MB)
        if (newlen < SDS_MAX_PREALLOC)
            newlen *= 2;
        else
            newlen += SDS_MAX_PREALLOC;
    }

    // 新的类型
    type = sdsReqType(newlen);

    /* Don't use type 5: the user is appending to the string and type 5 is
     * not able to remember empty space, so sdsMakeRoomFor() must be called
     * at every appending operation. */
    if (type == SDS_TYPE_5) type = SDS_TYPE_8;

    hdrlen = sdsHdrSize(type);
    assert(hdrlen + newlen + 1 > len);  /* Catch size_t overflow */

    if (oldtype == type) {
        newsh = s_realloc_usable(sh, hdrlen + newlen + 1, &usable);
        if (newsh == NULL) return NULL;
        s = (char*)newsh + hdrlen;
    } else {
        /* Since the header size changes, need to move the string forward,
         * and can't use realloc */
        newsh = s_malloc_usable(hdrlen + newlen + 1, &usable);
        if (newsh == NULL) return NULL;
        memcpy((char*)newsh + hdrlen, s, len + 1);
        s_free(sh);
        s = (char*)newsh + hdrlen;
        s[-1] = type;
        sdsSetLen(s, len);
    }
    usable = usable - hdrlen - 1;
    if (usable > sdsTypeMaxSize(type))
        usable = sdsTypeMaxSize(type);
    sdsSetAlloc(s, usable);
    return s;
}

sds sdsMakeRoomFor(sds s, size_t addlen) {
    return _sdsMakeRoomFor(s, addlen, 1);
}

sds sdscatlen(sds s, char *t, size_t len) {
    size_t curlen = sdsLen(s);

    s = sdsMakeRoomFor(s, len);
    if (s == NULL) return NULL;
    s = strcat(s, t);

    // memcpy(s+curlen, t, len);
    sdsSetLen(s, curlen + len);
    s[curlen + len] = '\0';
    printf("====%s", s);
    return s;
}

sds sdscat(sds s, const char *t) {
    printf("///// %s\n", t);
    return sdscatlen(s, t, strlen(t));
}

sds sdscatsds(sds s, const sds t) {
    return sdscatlen(s, t, sdsLen(t));
}

/**
 * 惰性空间，这里并没有释放内存，也就是并没有改变alloc的值，方便以后扩展
 * @param s
 * @param cset
 * @return
 */
sds sdstrim(sds s, const char *cset) {
    char *end, *sp, *ep;
    size_t len;

    sp = s;
    ep = end = s + sdsLen(s)-1;
    while(sp <= end && strchr(cset, *sp)) sp++;
    while(ep > sp && strchr(cset, *ep)) ep--;
    len = (sp > ep) ? 0 : ((ep-sp)+1);
    if (s != sp) memmove(s, sp, len);
    s[len] = '\0';
    sdsSetLen(s, len);
    return s;
}

int main(void) {
    sds sds1 = _sdsNewLen("chgfaocdfg45678901432hjd", 100, 1);
    printf("sds1 -> Len %ld\n", sdsLen(sds1));
    printf("sds1 -> Len %s\n", sds1);
    sds1 = sdscat(sds1, "hhhhhhhhhhhhhhhh");
    printf("sds1 -> Len %ld\n", sdsLen(sds1));
    printf("sds1 -> %s\n", sds1);

    sdsfree(sds1);
    printf("============================\n");
    size_t size = zmalloc_used_memory();
    printf("total : %ld\n", size);
}
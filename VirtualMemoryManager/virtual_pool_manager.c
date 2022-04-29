//
// Created by bytedance on 2021/8/3.
//

#include "virtual_pool.h"
#include "virtual_pool_util.h"

zys_virtualr_pool *create_zys_pool(size_t size) {
    if (size <= sizeof(zys_virtualr_pool)) {
        fprintf(stderr, "size is small, %zu", size);
        return NULL;
    }

    zys_virtualr_pool *data;
    data = zys_mem_align(ZYS_ALIGN, size);

    data->data->next = NULL;
    data->data->last = (unsigned char *) data + sizeof(zys_virtualr_pool);
    data->data->end = (unsigned char *) data + size;

    data->large = NULL;
    data->clean = NULL;
    data->current = data;
    data->max = MAXSIZE;

    return data;
}

void destory_zys_pool(zys_virtualr_pool *pool) {
    zys_virtualr_clean *c;
    zys_virtualr_large *l;
    zys_virtualr_pool *p, *n;
    // 清空data
    for (c = pool->clean; c; c = c->next) {
        if (c->handler) {
            c->handler(c->data);
        }
    }

    // 清空large内存
    for (l = pool->large; l; l = l->next) {
        if (l->data) {
            zys_free(l->data);
        }
    }

    // 清空内存头
    for (p = pool, n = p->data->next;; p = n, n = n->data->next) {
        zys_free(p);
        if (n == NULL) {
            break;
        }
    }
}

void *zys_palloc_block(zys_virtualr_pool *pool, size_t size) {
    unsigned char *m;
    size_t psize;
    zys_virtualr_pool *p, *new;

    // 现在占的大小
    psize = (size_t) (pool->data->end - (unsigned char *) pool);

    // 分配内存
    m = zys_mem_align(ZYS_ALIGN, psize);
    if (m == NULL) {
        return NULL;
    }

    new = (zys_virtualr_pool *) m;
    // 修改end的值
    new->data->end = m + psize;
    new->data->next = NULL;
    new->data->failed = 0;

    m += sizeof(zys_vritualr_pool_data);
    m = zys_align_ptr(m, ZYS_ALIGN);
    // 修改last的值
    new->data->last = m + size;

    for (p = pool->current; p->data->next; p = p->data->next) {
        if (p->data->failed++ > 4) {
            pool->current = p->data->next;
        }
    }

    p->data->next = new;

    return m;
}

void *zys_palloc_large(zys_virtualr_pool *pool, size_t size) {
    void *p;
    int n;
    zys_virtualr_large *large;

    p = zys_malloc(size);
    if (p == NULL) {
        return NULL;
    }

    n = 0;

    for (large = pool->large; large; large = large->next) {
        if (large->data == NULL) {
            large->data = p;
            return p;
        }

        if (n++ > 3) {
            break;
        }
    }

    //TODO
}

void *zys_palloc_small(zys_virtualr_pool *pool, size_t size, int align) {
    unsigned char *m;
    zys_virtualr_pool *p;

    p = pool->current;

    // 遍历所有的p，看是否有符合的内存大小。
    do {
        m = p->data->last;
        if ((size_t) (p->data->end - m) >= size) {
            p->data->last = m + size;
            return m;
        }

        p = p->data->next;
    } while (p);

    // 这里要分配的是这里需要的size
    return zys_palloc_block(pool, size);
}

// 大内存的申请
void *zys_palloc(zys_virtualr_pool *pool, size_t size) {
    if (size <= pool->max) {
        return zys_palloc_small(pool, size, 0);
    }
    return zys_palloc_large(pool, size);
}

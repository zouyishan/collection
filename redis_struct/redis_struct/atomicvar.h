#include <pthread.h>

#ifndef __ATOMIC_VAR_H
#define __ATOMIC_VAR_H

#define zysAtomic

#undef  zysAtomic
/**
 * 定义_Atomic使用原子类
 */
#define zysAtomic _Atomic

#ifdef __ATOMIC_VAR_FORCE_SYNC_MACROS
#include <valgrind/helgrind.h>
#else
#define ANNOTATE_HAPPENS_BEFORE(v) ((void) v)
#define ANNOTATE_HAPPENS_AFTER(v)  ((void) v)
#endif

#if !defined(__ATOMIC_VAR_FORCE_SYNC_MACROS) && defined(__STDC_VERSION__) && \
    (__STDC_VERSION__ >= 201112L) && !defined(__STDC_NO_ATOMICS__)
/* Use '_Atomic' keyword if the compiler supports. */
#undef  redisAtomic
#define redisAtomic _Atomic
/* Implementation using _Atomic in C11. */

#include <stdatomic.h>
#define atomicIncr(var,count) atomic_fetch_add_explicit(&var,(count),memory_order_relaxed)
#define atomicGetIncr(var,oldvalue_var,count) do { \
    oldvalue_var = atomic_fetch_add_explicit(&var,(count),memory_order_relaxed); \
} while(0)
#define atomicDecr(var,count) atomic_fetch_sub_explicit(&var,(count),memory_order_relaxed)
#define atomicGet(var,dstvar) do { \
    dstvar = atomic_load_explicit(&var,memory_order_relaxed); \
} while(0)
#define atomicSet(var,value) atomic_store_explicit(&var,value,memory_order_relaxed)
#define atomicGetWithSync(var,dstvar) do { \
    dstvar = atomic_load_explicit(&var,memory_order_seq_cst); \
} while(0)
#define atomicSetWithSync(var,value) \
    atomic_store_explicit(&var,value,memory_order_seq_cst)
#define REDIS_ATOMIC_API "c11-builtin"

#elif !defined(__ATOMIC_VAR_FORCE_SYNC_MACROS) && \
    (!defined(__clang__) || !defined(__APPLE__) || __apple_build_version__ > 4210057) && \
    defined(__ATOMIC_RELAXED) && defined(__ATOMIC_SEQ_CST)
/* Implementation using __atomic macros. */

#define atomicIncr(var, count) __atomic_add_fetch(&var,(count),__ATOMIC_RELAXED)
#define atomicGetIncr(var,oldvalue_var,count) do { \
    oldvalue_var = __atomic_fetch_add(&var,(count),__ATOMIC_RELAXED); \
} while(0)
#define atomicDecr(var,count) __atomic_sub_fetch(&var,(count),__ATOMIC_RELAXED)
#define atomicGet(var, dstvar) do { \
    dstvar = __atomic_load_n(&var, __ATOMIC_RELAXED); \
} while(0)
#define atomicSet(var,value) __atomic_store_n(&var,value,__ATOMIC_RELAXED)
#define atomicGetWithSync(var,dstvar) do { \
    dstvar = __atomic_load_n(&var,__ATOMIC_SEQ_CST); \
} while(0)
#define atomicSetWithSync(var,value) \
    __atomic_store_n(&var,value,__ATOMIC_SEQ_CST)
#define REDIS_ATOMIC_API "atomic-builtin"

#elif defined(HAVE_ATOMIC)
/* Implementation using __sync macros. */

#define atomicIncr(var,count) __sync_add_and_fetch(&var,(count))
#define atomicGetIncr(var,oldvalue_var,count) do { \
    oldvalue_var = __sync_fetch_and_add(&var,(count)); \
} while(0)
#define atomicDecr(var,count) __sync_sub_and_fetch(&var,(count))
#define atomicGet(var,dstvar) do { \
    dstvar = __sync_sub_and_fetch(&var,0); \
} while(0)
#define atomicSet(var,value) do { \
    while(!__sync_bool_compare_and_swap(&var,var,value)); \
} while(0)
/* Actually the builtin issues a full memory barrier by default. */
#define atomicGetWithSync(var,dstvar) { \
    dstvar = __sync_sub_and_fetch(&var,0,__sync_synchronize); \
    ANNOTATE_HAPPENS_AFTER(&var); \
} while(0)
#define atomicSetWithSync(var,value) do { \
    ANNOTATE_HAPPENS_BEFORE(&var);  \
    while(!__sync_bool_compare_and_swap(&var,var,value,__sync_synchronize)); \
} while(0)
#define REDIS_ATOMIC_API "sync-builtin"

#else
#error "Unable to determine atomic operations for your platform"

#endif
#endif /* __ATOMIC_VAR_H */

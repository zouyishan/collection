//
// Created by bytedance on 2021/6/27.
//

#include <pthread.h>
#include <stdio.h>
#include <unistd.h>
#include <stdatomic.h>
#include "../redis_struct/atomicvar.h"

#define THREAD_NUM 10

zysAtomic size_t atomic = 0;
size_t num = 0;
#define up_(__n) atomicIncr(atomic, (__n))

void *inc(int* count) {
    for (int i = 0; i < 5000; i++) {
        *count += 1;
    }
}

void *atomic_inc() {
    size_t z = 1;
    for (int i = 0; i < 5000; i++) {
        up_(z);
    }
}

int a[THREAD_NUM] = {1};
int b[THREAD_NUM] = {1};
int main(void) {
    pthread_t pthreadArray[THREAD_NUM];
    pthread_t pthreadAtomicArray[THREAD_NUM];
    for (int i = 0; i < THREAD_NUM; i++) {
        // 成功返回0，如果不为0表示创建失败。
        a[i] = pthread_create(&pthreadArray[i], NULL, (void *)inc, &num);
    }

    for (int i = 0; i < THREAD_NUM; i++) {
        b[i] = pthread_create(&pthreadAtomicArray[i], NULL, (void *)atomic_inc, NULL);
    }

    // 主线程睡眠，等待执行
    sleep(1);

    for (int i = 0; i < THREAD_NUM; ++i) {
        if (!a[i]) {
            fprintf(stdout, "线程%d,成功执行\n", i);
        }
        if (!b[i]) {
            fprintf(stdout, "atomic线程%d, 成功执行\n", i);
        }
    }

    fprintf(stdout, "线程不安全的结果是：%zu\n", num);
    fprintf(stdout, "atomic最后的结果是：%zu", atomic);
    return 0;
}
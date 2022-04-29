//
// Created by bytedance on 2021/7/1.
//

#include <unistd.h>
#include <stdio.h>
#include <stdbool.h>

// 开始地址，结束地址
void *startMemory;
void *endMemory;
// 是否初始化
bool isInit = false;

typedef struct mm {
    bool available;
    size_t size; // 这个size不包含这个结构体所占的内存
} zysMemoryManager;

void zysMallocInit() {
    startMemory = sbrk(0);
    endMemory = startMemory;
    isInit = true;
}

void *zysMalloc(size_t bytes) {
    // 初始化内存
    if (isInit == false) {
        zysMallocInit();
    }

    // 结果
    void *res;
    // 临时start
    void *tmpStart;
    tmpStart = startMemory;
    // 每次临时start额头结构
    zysMemoryManager *startLocation;

    res = 0;
    // 分配的字节前面加上结构体的内存
    bytes += sizeof (zysMemoryManager);

    // 每次内存分配我们都只是分配的刚刚好，所以可以这样遍历
    while (tmpStart != endMemory) {
        startLocation = (zysMemoryManager *) tmpStart;
        if (!startLocation -> available) {
            if (startLocation -> size > bytes) {
                startLocation -> available = true;
                res = startLocation + sizeof (zysMemoryManager);

                return res;
            }
        }
        tmpStart += startLocation -> size + sizeof (zysMemoryManager);
    }

    // 每次分配都分配刚刚好，free掉了这段内存是否就很容易生成碎片呢
    sbrk(bytes);
    startLocation = (zysMemoryManager *) endMemory;
    startLocation -> size = bytes - sizeof (zysMemoryManager);
    startLocation -> available = true;

    res = endMemory + sizeof (zysMemoryManager);
    endMemory += bytes;
    return res;
}

// 分配的是虚拟页，下面会调缺页中断再分配物理页
void zysFree(void *memory) {
    zysMemoryManager *location = memory - sizeof (zysMemoryManager);
    location -> available = false;
}

int main(void) {
    int *a = NULL;
    a = (int *)zysMalloc(sizeof(int));
    int *b = NULL;
    b = (int *)zysMalloc(sizeof(int));
    *b = 20;
    *a = 10;
    printf("a的值为：%d\n", *a);
    // endMemory - sizeof(int)就是b指针的地址
    int *c = endMemory - sizeof (int);

    if (c == b) {
        printf("内存分配yes\n");
    } else {
        printf("代码有问题\n");
    }
    printf("*b: %d, *c: %d", *b, *c);
    zysFree(a);
    zysFree(b);
    return 0;
}

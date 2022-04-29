//
// Created by bytedance on 2021/7/3.
//

#include <stdio.h>
#include <unistd.h>

char s;

int main(void) {
    printf("位置：%lld\n", (&s + 1));

    void *k = sbrk(0);
    printf("== %lld", k);
}
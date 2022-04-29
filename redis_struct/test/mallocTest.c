//
// Created by bytedance on 2021/6/25.
//

#include <unistd.h>
#include <stdio.h>

int has_initialized = 0;
void *managed_memory_start;
void *last_valid_address;

typedef struct {
    int is_avaliable;       //访问权限标志位(这里表示是否已经分配)
    int size;               //including the sizeof(mem_control_block)
} mem_control_block;

void malloc_init() {
    last_valid_address = sbrk(0);               //这里使用了一个技巧,传参0给sbrk,得到当前地址
    managed_memory_start = last_valid_address;
    has_initialized = 1;
}

void * malloc1(long nbytes) {
    void * current_location;
    mem_control_block * current_location_mcb;
    void * memory_location;

    if(!has_initialized)
        malloc_init();

    //这一步比较重要,因为我们分配的内存中是包含使用的这个数据结构大小的内存的.只是队用户隐藏.
    nbytes += sizeof(mem_control_block);

    memory_location = 0;

    current_location = managed_memory_start;

    while (current_location != last_valid_address) {
        current_location_mcb = (mem_control_block *)current_location;
        if (current_location_mcb -> is_avaliable) {
            if (current_location_mcb -> size >= nbytes) {
                /*
                  至此,就是最顺利的一种,也就是上面说的第一种情况
                 */
                current_location_mcb -> is_avaliable = 0;
                memory_location = current_location; //should delete the size of mem_control_block
                break;
            }
        }
        current_location += current_location_mcb -> size;
    }
    /*
      如果glibc帮助进程管理的内存地址空间不够用了,那就要扩展了,再批发点.
     */
    if (memory_location == 0) {
        //这里批发的正好是这次请求的,感觉实际系统中处于性能对的考虑会多批发点
        sbrk(nbytes);
        memory_location = last_valid_address;
        last_valid_address += nbytes;

        current_location_mcb = (mem_control_block*)memory_location;
        current_location_mcb -> is_avaliable = 0;
        current_location_mcb -> size = nbytes;
    }
    //记得向用户隐藏我们用于管理内存的这个数据结构的实例
    memory_location += sizeof(mem_control_block);
    return memory_location;
}

/*
  这个free函数真是简单
 */
void free1(void* first_byte)
{
    mem_control_block* mcb;
    mcb = (mem_control_block*) (first_byte - sizeof(mem_control_block));
    mcb->is_avaliable = 1;
    return;
}

int main(int argc, char const *argv[])
{
    int * a = NULL;
    a = (int *)malloc1(sizeof(int));
    *a = 10;
    printf("%d\n", *a);
    free1(a);
    return 0;
}
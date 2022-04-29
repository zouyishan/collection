//
// Created by bytedance on 2021/6/25.
//

#include <stdio.h>
#include "../redis_struct/sds.h"

int main(int argc, const char * argv[]) {
    // insert code here...
    printf("Hello, World!\n");
    /**
     * sds5  对应 1 << 5   32
     * sds8  对应 1 << 8   256
     * sds16 对应 1 << 16  65536
     * sds32 对应 1 << 32  2 ^ 32
     * sds64 对应 1 << 64  2 ^ 64
     */
    printf("SDS_TYPE_5 %d \n", sizeof(struct sdshdr5));
    printf("SDS_TYPE_8 %d \n", sizeof(struct sdshdr8));
    printf("SDS_TYPE_16 %d \n", sizeof(struct sdshdr16));
    printf("SDS_TYPE_32 %d \n", sizeof(struct sdshdr32));
    printf("SDS_TYPE_64 %d \n", sizeof(struct sdshdr64));
    return 0;
}
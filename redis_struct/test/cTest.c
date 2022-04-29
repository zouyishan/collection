//
// Created by bytedance on 2021/6/27.
//

#include <stdio.h>
#include <stdlib.h>

int main(void) {
    char *s = "hhhhh";

    printf("s:, %s\n", s);
    s = "ffsadfadsufihdiuhflasduhguibdsiguhdsuhguhdsughkudsahghdshgdsaga";
    printf("s now : %s\n%c\n", s, *s);
    printf("nc: %u", &s);

    s = "kkkkk\0fdsf";
    printf("s now : %s\n", s);
    printf("字符：%c", s[11]);
    return 0;
}
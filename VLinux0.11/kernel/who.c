#include <asm/segment.h>
#include <errno.h>
#include <linux/kernel.h>

#define SIZE 20

char sys_name[SIZE + 1];

static int NAME_LEN = 0;

int sys_iam(const char *name) {
    int i = 0;

    while (get_fs_byte(name + i) != '\0') {
        i++;
    }

    if (NAME_LEN > SIZE) {
        printk("this name is longer than kernel max size: %d\n", SIZE);
        return -(EINVAL);
    } else {
        NAME_LEN = i;
        i = 0;

        for (i = 0; i < NAME_LEN; i++) {
            sys_name[i] = get_fs_byte(name + i);
        }

        sys_name[i] = '\0';
        printk("hello master %s\n", sys_name);
    }
    return NAME_LEN;
}

int sys_whoiam(char *user_mode_name, int size) {
    if (NAME_LEN == 0) {
        printk("no kernel name\n");
        return -(EINVAL);
    }

    if (size > NAME_LEN) {
        printk("size is more than kernel name size\n");
        return -(EINVAL);
    } else {
        int i = 0;

        for (; i < NAME_LEN; i++) {
            put_fs_byte(sys_name[i], user_mode_name + i);
        }

        put_fs_byte('\0', user_mode_name + i);
    }
    return NAME_LEN;
}

#include "headers.h"

int cut(const char *src_path, const char *dst_path) {
    copy(src_path, dst_path);
    int fd_src = open(src_path, O_TRUNC);
    
    ckerror(fd_src, "truncate source file");

    return 0;
}
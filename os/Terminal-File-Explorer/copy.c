#include "headers.h"

void ckerror(int val, char *str) {
    if (val == -1) {
        perror(str);
        exit(1);
    }
}

int copy(const char *src_path, const char *dst_path) {
    int fd_src = open(src_path, O_RDONLY);
    int fd_dst = open(dst_path, O_WRONLY | O_TRUNC | O_APPEND);

    ckerror(fd_src, "source file opening");
    ckerror(fd_dst, "destination file opeing");

    const unsigned long long FILE_SIZE = lseek64(fd_src, 0, SEEK_END);
    lseek64(fd_src, -FILE_SIZE, SEEK_END);

    int buffer_size = 1048576;
    char *buff = (char *) malloc(buffer_size * sizeof(char));

    if (buff == NULL) {
        perror("buffer_size");
        return 1;
    }

    unsigned long long pos = 0;
    while (pos < FILE_SIZE) {
        int x = read(fd_src, buff, buffer_size);
        ckerror(x, "reading source file");

        pos += x;
        x = write(fd_dst, buff, x);

        ckerror(x, "writing destination file");
    }

    ckerror(close(fd_src), "source file closing");
    ckerror(close(fd_dst), "destination file closing");

    return 0;
}
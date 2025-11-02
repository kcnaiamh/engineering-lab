#include "headers.h"

int _delete(char *path) {
    int x = unlink(path);
    ckerror(x, "");

    return 0;
}
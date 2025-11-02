#include "headers.h"

int _rename(const char *old_path, const char *new_path) {
    int x = rename(old_path, new_path);
    ckerror(x, "");
    
    return 0;
}
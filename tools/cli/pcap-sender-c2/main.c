#include "own.h"
#include <unistd.h>

int main()
{
    int inum = get_adapter_list();
    while (TRUE)
    {
        capture_packet(inum);
        send_packet();
        sleep(0.05);
    }
}
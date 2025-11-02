#include "own.h"

FARPROC get_func_ptr(char *func_name)
{
    FARPROC func_ptr = GetProcAddress(dllHandle, func_name);
    if (func_ptr == NULL)
    {
        printf("Failed to get address of %s() function.\n", func_name);
        exit(1);
    }
    return func_ptr;
}

void set_dllHandle()
{
    dllHandle = LoadLibrary("wpcap.dll");
    if (dllHandle == NULL)
    {
        printf("Failed to load Npcap DLL.\n");
        exit(1);
    }
}

int get_adapter_list()
{
    pcap_if_t *alldevs;
    int inum;
    pcap_if_t *d;
    int i = 0;
    char errbuf[PCAP_ERRBUF_SIZE];

    dllHandle = LoadLibrary("wpcap.dll");
    if (dllHandle == NULL)
    {
        printf("Failed to load Npcap DLL.\n");
        exit(1);
    }

    if (get_func_ptr("pcap_findalldevs")(&alldevs, errbuf) == -1)
    {
        fprintf(stderr, "Error in pcap_findalldevs: %s\n", errbuf);
        exit(1);
    }

    if (get_func_ptr("pcap_findalldevs")(&alldevs, errbuf) == -1)
    {
        fprintf(stderr, "Error in pcap_findalldevs: %s\n", errbuf);
        exit(1);
    }

    // Print the list
    for (d = alldevs; d; d = d->next)
    {
        printf("%d. %s", ++i, d->name);
        if (d->description)
            printf(" (%s)\n", d->description);
        else
            printf(" (No description available)\n");
    }

    if (i == 0)
    {
        printf("\nNo interfaces found! Make sure Npcap is installed.\n");
        exit(-1);
    }

    printf("Enter the interface number (1-%d):", i);
    scanf("%d", &inum);

    if (inum < 1 || inum > i)
    {
        printf("\nInterface number out of range.\n");
        get_func_ptr("pcap_freealldevs")(alldevs);
        exit(-1);
    }

    return inum;
}
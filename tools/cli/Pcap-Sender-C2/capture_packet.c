#include "own.h"

const int NUMBER_OF_PACKET = 1000; // 0 for infinity

/* Callback function invoked by libpcap for every incoming packet */
void packet_handler(u_char *dumpfile, const struct pcap_pkthdr *header, const u_char *pkt_data)
{
    get_func_ptr("pcap_dump")(dumpfile, header, pkt_data); // save packet in dump file
}

int capture_packet(int inum)
{
    pcap_if_t *alldevs;
    pcap_if_t *d;
    int i = 0;
    pcap_t *adhandle;
    char errbuf[PCAP_ERRBUF_SIZE];
    pcap_dumper_t *dumpfile;

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

    // Jump to the selected adapter
    for (d = alldevs, i = 0; i < inum - 1; d = d->next, i++)
        ;

    // Open the adapter
    FARPROC pcap_open_live = get_func_ptr("pcap_open_live");
    if ((adhandle = (pcap_t *)pcap_open_live(d->name, // name of the device
                                             65536,   // portion of the packet to capture.
                                                      // 65536 grants that the whole packet will be captured on all the MACs.
                                             1,       // promiscuous mode (nonzero means promiscuous)
                                             1000,    // read timeout
                                             errbuf   // error buffer
                                             )) == NULL)
    {
        fprintf(stderr, "\nUnable to open the adapter. %s is not supported by Npcap\n", d->name);
        get_func_ptr("pcap_freealldevs")(alldevs);
        return -1;
    }

    // Open the dump file
    FARPROC pcap_dump_open = get_func_ptr("pcap_dump_open");
    dumpfile = (pcap_dumper_t *)pcap_dump_open(adhandle, "out.pcap");

    if (dumpfile == NULL)
    {
        fprintf(stderr, "\nError opening output file\n");
        return -1;
    }

    printf("listening on %s...\n", d->description);

    get_func_ptr("pcap_freealldevs")(alldevs);

    // start the capture
    FARPROC pcap_loop = get_func_ptr("pcap_loop");
    pcap_loop(adhandle, NUMBER_OF_PACKET, packet_handler, (unsigned char *)dumpfile);

    get_func_ptr("pcap_close")(adhandle);
    return 0;
}
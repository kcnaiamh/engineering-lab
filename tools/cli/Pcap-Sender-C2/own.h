#define _CRT_SECURE_NO_WARNINGS

#include <windows.h>
#include <pcap.h>
#include <stdio.h>
#include <time.h>

HMODULE dllHandle;

FARPROC get_func_ptr(char *func_name);
int get_adapter_list();
int capture_packet(int inum);
int send_packet();
void set_dllHandle();
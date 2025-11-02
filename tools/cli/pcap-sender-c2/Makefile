CC=gcc
CFLAGS = -Wall -Wextra -Werror
LDFLAGS = -lnpcap
all: run

run: main.c
	$(CC) .\main.c .\sender.c .\own.c .\capture_packet.c -o a -I"C:\\ProgramData\\npcap-sdk-1.13\\Include" -L"C:\\ProgramData\\npcap-sdk-1.13\\Lib\\x64" -lws2_32

clean:
	$(RM) *.o *.exe *.s a.out
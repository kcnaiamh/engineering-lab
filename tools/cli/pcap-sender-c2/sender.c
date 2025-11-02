#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include "own.h"

#pragma comment(lib, "ws2_32.lib") // Link the Winsock library

#define SERVER_IP "192.168.0.109"
// #define SERVER_IP "127.0.0.1"
#define SERVER_PORT 2222
#define BUFFER_SIZE 1024

#define FILE_NAME "out.pcap"

int send_packet()
{
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
    {
        perror("WSAStartup failed");
        return 1;
    }

    // Create socket
    SOCKET clientSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (clientSocket == INVALID_SOCKET)
    {
        perror("Socket creation failed");
        WSACleanup();
        return 1;
    }

    // Specify server address
    struct sockaddr_in serverAddr;
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(SERVER_PORT);
    serverAddr.sin_addr.s_addr = inet_addr(SERVER_IP);

    // Connect to the server
    if (connect(clientSocket, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR)
    {
        perror("Connection failed");
        closesocket(clientSocket);
        WSACleanup();
        return 1;
    }

    // Open the pcap file for reading
    FILE *file = fopen(FILE_NAME, "rb");
    if (file == NULL)
    {
        fprintf(stderr, "File not found.\n");
        closesocket(clientSocket);
        WSACleanup();
        return 1;
    }

    fseek(file, 0, SEEK_END);
    long fileSize = ftell(file);
    fseek(file, 0, SEEK_SET);

    char buffer[1024];
    size_t bytesRead;
    long totalBytesSent = 0;

    while (totalBytesSent < fileSize)
    {
        bytesRead = fread(buffer, 1, sizeof(buffer), file);
        if (bytesRead == 0)
        {
            fprintf(stderr, "File read error.\n");
            break;
        }
        int bytesSent = send(clientSocket, buffer, bytesRead, 0);
        if (bytesSent == SOCKET_ERROR)
        {
            fprintf(stderr, "Sending failed.\n");
            break;
        }
        totalBytesSent += bytesSent;
    }

    // Close the file and socket
    fclose(file);
    closesocket(clientSocket);
    WSACleanup();

    if (totalBytesSent == fileSize)
    {
        printf("File sent successfully.\n");
    }
    else
    {
        printf("File sent partially.\n");
    }

    FILE *hFile = fopen(FILE_NAME, "w");
    if (hFile == NULL)
    {
        perror("Error opening the file");
        return 1;
    }

    // Close the file, effectively truncating it to zero bytes
    fclose(hFile);

    return 0;
}

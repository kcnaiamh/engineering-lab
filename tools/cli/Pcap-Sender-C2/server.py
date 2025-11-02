import socket

HOST = '0.0.0.0'
PORT = 2222

cnt = 0
while True:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen(1)
        conn, addr = s.accept()
        with conn:
            print('Connected by', addr)
            while True:
                data = conn.recv(2048)
                if not data: break
                with open("server_dump{}.pcap".format(str(cnt).zfill(3)), "ab") as fp:
                    fp.write(data)
            conn.send(b"OK")
        cnt += 1

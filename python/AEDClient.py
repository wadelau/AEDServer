#!/usr/bin/python3 
# -v

# AEDServer in Python, client-side
# Xenxin@Ufqi
# 21:21 27 December 2017

import sys
import socket

# config
myhost = 'localhost'
myport = 8881
line_terminator = "\n\n" # finish an input and wait for feedback from server

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((myhost, myport))

print("Connected to server....")

data = sys.argv[1] 

sock.sendall((data + line_terminator).encode("utf8"))
#print("sent-line:[{}]".format(data))

response = sock.recv(8192)
#print ("Received:", response.decode("utf8"))
print (response.decode("utf8"))

sock.close(  )

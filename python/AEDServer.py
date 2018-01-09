#!/usr/bin/python3 
# -v

# AEDServer in Python, server-side
# Xenxin@Ufqi
# Xenxin@Ufqi, Sat Dec  9 18:59:01 CST 2017

import sys
import time
from datetime import date, datetime
import logging as logx

import asyncore, asynchat, socket
import numpy as np

# config
myhost = 'localhost'
myport = 8881

#
class MainServerSocket(asyncore.dispatcher):
	
    myservice = '';
	
    #
    def __init__(self, host, port):
        print(str(date.today())+': initing MSS....')
        asyncore.dispatcher.__init__(self)
        self.create_socket(socket.AF_INET, socket.SOCK_STREAM)
        self.bind((host, port))
        self.listen(5)

        logx.basicConfig(filename='./log/AEDServer.log', format='%(asctime)s\t%(levelname)s\t%(message)s', level=logx.DEBUG)
        logx.info('server in coming up....')
         
        #		 
        # main service init....
        #
	
    #
    def handle_accept(self):
        newSocket, address = self.accept(  )
        print("Connected from", address)
        try:
            SecondaryServerSocket(newSocket, self.myservice)
        except:
            print("error for communication:{}".format(newSocket))
            logx.warn('error for communication:%', newSocket)
            pass


#
class SecondaryServerSocket(asynchat.async_chat):

    myservice = ''
    
    #
    def __init__(self, mysock, myservice):
        print(str(datetime.now())+': initing SSS....{}, {}'.format(mysock, myservice))
        self.myservice = myservice
        #asynchat.async_chat.__init__(self, *args)
        asynchat.async_chat.__init__(self, mysock)
        #self.set_terminator('\n'.encode("utf8"))
        self.set_terminator('\n\n'.encode("utf8")) # finish an input and start to feedback
        self.data = []

    #
    def collect_incoming_data(self, data):
        #self.data.append(data)
        try:
            strdata = data.decode("utf8")
            for line in strdata.splitlines():
                if line != '':
                    rtndata = self.doSomething(line)
                else:
                    rtndata = 0.0
                self.data.append(line + "\t" + str(rtndata) + "\n")
                #print("recv-data:{}, pdata:{}".format(line, pdata))
	    # return over long connnection, bgn
	    self.data.append("\n") # line feed as a return end, keep same with client
	    logx.info("send-back:[%s]", self.data)
            self.push((''.join(self.data).encode("utf8")))
            self.data = []
	    # return over long connection, end
        except:
            print("recv-data error for [{}], trying to close...".format(data))
            #pass
            #logx.warn('recv-data error for [%s], trying to close...', data)
            self.found_terminator()

    #
    def found_terminator(self):
        #for line in self.data:
        #    print("recv-line:{}, knn:{}".format(line, self.myknn))
        print("send-back:{}".format(self.data)) 
        self.push((''.join(self.data).encode("utf8")))
        self.data = []
        # long connection?
        self.handle_close()

    #
    def doSomething(self, inputLine):
       	val = 0.0
        try:
            # val = self.myservice.doSomething(inputLine)
            val = 0.1
        except:
            print("error for doSomething:{}".format(inputLine))
            logx.warn('error for predict:[%s]', inputLine)
            pass
        return val

    #
    def handle_close(self):
        #print ("Disconnected from", self.getpeername(  ))
        self.close(  )


# run
MainServerSocket(myhost, myport)
asyncore.loop()

#!/usr/bin/env python
#coding:utf-8
# ip query by dat 
# power by ip138
import os
import socket
import mmap
import math
from struct import pack, unpack


class Ip138(object):

    def __init__(self, path):
        self.path = path
        self.db = None
        self.open()
        self.textOffset = 0
        self.total = 0
        self.init()


    def open(self):
        if not self.db:
            self.db = open(self.path, 'rb')
            self.db = mmap.mmap(self.db.fileno(), 0, access = 1)
        return self.db

    def init(self):
        self.db.seek(0)
        self.textOffset = unpack('I', self.db.read(4))[0]
        self.total = int((self.textOffset - 4 - 256*4) / 9)
        #print(self.textOffset)
        #print(self.total)

    def query(self, ip):
        try:
        	ip = unpack('!I', socket.inet_aton(ip))[0]
        except:
        	print('输入iP格式不对')
        	exit()

        left = 0
        right = 0
        first = ip>>24
        if first==0xff:
            self.db.seek(4+(first-1)*4)
            left = unpack('I', self.db.read(4))[0]+1
            right = self.total
        else:
            self.db.seek(4+first*4)
            left = unpack('I', self.db.read(4))[0]
            self.db.seek(4+(first+1)*4)
            right = unpack('I', self.db.read(4))[0]-1
            if right<1:
                right = self.total

        #print(first, left, right)
        i = 0
        while i<24:
            middle = int(math.floor((left+right)/2))
            if middle==left:
                ipOffset = int(4 + 256*4 + right*9)
                self.db.seek(ipOffset+4)
                textOffset = unpack('I', self.db.read(4))[0]
                self.db.seek(ipOffset+8)
                textLength = unpack('B', self.db.read(1))[0]
                #print(right, textOffset, textLength)

                self.db.seek(self.textOffset + textOffset)
                text = self.db.read(textLength)
                return text

            middleOffset = int(4 + 256*4 + middle*9)
            #print(left, right, middle)
            self.db.seek(middleOffset)
            middleIp = unpack('I', self.db.read(4))[0]
            if ip <= middleIp:
                right = middle
            else:
                left = middle

            i=i+1

        return "end"

    def __del__(self):
        if self.db:
            self.db.close()


def main():
    #ip = '61.139.2.69'
    #ip = '0.0.0.0'
    #ip = '255.255.255.255'
    #ip = '152.32.193.0'
    #ip = '1.0.2.1'
    ip = '110.81.112.0'
    dbpath = '../ip.dat'
    ipquery = Ip138(dbpath)
    a = ipquery.query(ip)
    print('%15s %s' % (ip, a))

if __name__ == '__main__':
    main()
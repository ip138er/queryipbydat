#!/usr/bin/env python
#coding:utf-8
# ip query by dat 
# power by ip138
import os
import socket
import mmap
from struct import pack, unpack


class Ip138(object):

    def __init__(self, path):
        self.path = path
        self.db = None
        self.open_db()
        self.textData = []
        self.idx_start= 0
        self.index = [0 for x in range(256)]
        # IP索引总数
        self.total = 0
        self._read_data()

    def open_db(self):
        if not self.db:
            self.db = open(self.path, 'rb')
            self.db = mmap.mmap(self.db.fileno(), 0, access = 1)
        return self.db

    def _read_data(self):
        '''读取数据库中IP索引起始和结束偏移值
        '''
        self.db.seek(0)
        self.idx_start = unpack('I', self.db.read(4))[0]
        self.db.seek(self.idx_start)
        self.total = (self.idx_start-4 -256*4) / 9
        self.textData = self.db[self.idx_start:]
        self.db.seek(0)
        for x in range(256):
        	off = 4+4*x
        	self.db.seek(off)
        	self.index[x] = unpack('I', self.db.read(4))[0]
        self.ipEndAddr = [0 for x in range(self.total)]
        self.textOffset = [0 for x in range(self.total)]
        self.textLen = [0 for x in range(self.total)]
        
        for x in range(self.total):
        	off = 4 + 1024 + x*9
        	self.db.seek(off)
        	self.ipEndAddr[x] = unpack('I', self.db.read(4))[0]
        	self.db.seek(off+4)
        	self.textOffset[x] = unpack('I', self.db.read(4))[0]
        	self.db.seek(off+4+4)
        	self.textLen[x] = unpack('B', self.db.read(1))[0]

        return self.index

    def find(self, ip, l, r):
        '''使用二分法查找网络字节编码的IP地址的索引记录
        '''
        if r <= l:
            return l

        m = (l + r) / 2
        new_ip = self.ipEndAddr[m]

        if ip < new_ip:
            return self.find(ip, l, m-1)
        else:
            return self.find(ip, m+1, r)

    def query(self, ip):
        '''查询IP信息
        '''
        # 使用网络字节编码IP地址
        ip = unpack('!I', socket.inet_aton(ip))[0]
        end = 0
        # 使用 self.find 函数查找ip的索引偏移
        if (ip>>24)!=0xff:
        	end = self.index[(ip>>24)+1]
        if end == 0:
        	end = len(self.ipEndAddr)
        i = self.find(ip, self.index[ip>>24], end)
        off = self.textOffset[i]
        # IP记录偏移值+4可以丢弃前4字节的IP地址信息。
        return self.textData[off:off+self.textLen[i]]

    def __del__(self):
        if self.db:
            self.db.close()


def main():
	ip = '182.56.66.93'
	dbpath = '../ip.dat'
	ipquery = Ip138(dbpath)
	a = ipquery.query(ip)
	print '%15s %s' % (ip, a)

if __name__ == '__main__':

    main()
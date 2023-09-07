package main

import (
	"log"
	"os"
	"net"
	"math"
	"bytes"
	"encoding/binary"
)

//ip数据
type IpLocation2 struct {
	TextOffset uint32 //数据区偏移量
	Total uint32 //ip数量
	File *os.File //打开的文件
}

//初始化ip归属地数据
func (this *IpLocation2) InitFromFile(file string) {
	filePtr, err :=os.OpenFile(file, os.O_RDONLY, 0)
	if err != nil {
		log.Fatalln(err)
	}
	this.File = filePtr

	this.File.Seek(0, os.SEEK_SET)
	buffer := make([]byte, 4)
	this.File.Read(buffer)

	this.TextOffset = binary.LittleEndian.Uint32(buffer) //获取文本偏移地址
	this.Total = (this.TextOffset - 4 - 256*4) / 9 //获取ip段数量
	//log.Println(this.Total)

	return
}

//获取ip归属地
func (this *IpLocation2) Query(ipstr string, dataType int, callback string) string {
	ip := net.ParseIP(ipstr)
	if ip == nil || ip.To4() == nil {
		return "error"
	}

	locationStr := this.Find(ip)

	return locationStr
}

func (this *IpLocation2) Find(ip net.IP) string {
	ipInt32 := binary.BigEndian.Uint32(ip[12:])

	first := int(ip[12])
	var left,right,middle uint32
	var buffer []byte
	if first == 255 {
		this.File.Seek(int64(4+(first-1)*4), os.SEEK_SET)
		buffer = make([]byte, 4)
		this.File.Read(buffer)
		left = binary.LittleEndian.Uint32(buffer) + 1
		right = this.Total
	}else{
		this.File.Seek(int64(4+first*4), os.SEEK_SET)
		buffer = make([]byte, 4)
		this.File.Read(buffer)
		left = binary.LittleEndian.Uint32(buffer)

		this.File.Seek(int64(4+(first+1)*4), os.SEEK_SET)
		buffer = make([]byte, 4)
		this.File.Read(buffer)
		right = binary.LittleEndian.Uint32(buffer)-1
		if(right<1){
			right = this.Total
		}
	}

	var i int = 0
	var middleOffset uint32
	for {
		middle = uint32(math.Floor(float64((left+right)/2)))
		if middle == left {
			//ip信息位置
			ipOffset := 4 + 256*4 + right*9;
			//数据位置
			this.File.Seek(int64(ipOffset+4), os.SEEK_SET)
			textOffsetbuffer := make([]byte, 4)
			this.File.Read(textOffsetbuffer)
			textOffset := binary.LittleEndian.Uint32(textOffsetbuffer)
			//数据长度
			this.File.Seek(int64(ipOffset+8), os.SEEK_SET)
			textLengthBuffer := make([]byte, 1)
			this.File.Read(textLengthBuffer)
			var textLength uint8
			bytesBuffer := bytes.NewBuffer(textLengthBuffer)
			binary.Read(bytesBuffer, binary.LittleEndian, &textLength)
			
			//ip信息数据
			this.File.Seek(int64(this.TextOffset + textOffset), os.SEEK_SET)
			buffer := make([]byte, textLength)
			this.File.Read(buffer)

			return string(buffer)
		}

		middleOffset = 4 + 256*4 + middle*9;
		this.File.Seek(int64(middleOffset), os.SEEK_SET)
		buffer := make([]byte, 4)
		this.File.Read(buffer)
		middleIp := net.IPv4(buffer[3], buffer[2], buffer[1], buffer[0])
		middleInt32 := binary.BigEndian.Uint32(middleIp[12:])
		//log.Println(left, right, middle, middleIp, middleInt32, ip, ipInt32)

		if ipInt32 <= middleInt32 {
			right = middle
		}else{
			left = middle
		}
		if i = i + 1; i>24 {
			break
		}
	}

	return "error"
}


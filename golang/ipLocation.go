package main

import (
	"encoding/binary"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"io/ioutil"
	"net"
	"strings"
)

//ip数据
type IpLocation struct {
	index      []uint32 //ip段索引 0-255
	ipEndArr   []uint32 //ip段数组
	textOffset []uint32 //ip归属地偏移量
	textlen    []byte   //ip归属地长度
	textData   []byte   //ip归属地
}

//初始化ip归属地数据
func (this *IpLocation) InitFromFile(file string) {
	data, _ := ioutil.ReadFile(file)
	textoffset := int(binary.LittleEndian.Uint32(data[:4])) //获取文本偏移地址
	this.textData = data[textoffset:]

	count := (textoffset - 4 - 256*4) / 9 //获取ip段数量
	this.index = make([]uint32, 256)
	//读取ip索引
	for i := 0; i < 256; i++ {
		off := 4 + i*4
		this.index[i] = binary.LittleEndian.Uint32(data[off : off+4])
	}

	this.ipEndArr = make([]uint32, count)
	this.textOffset = make([]uint32, count)
	this.textlen = make([]byte, count)

	//读取ip段数据
	for i := 0; i < count; i++ {
		off := 4 + 1024 + i*9
		this.ipEndArr[i] = binary.LittleEndian.Uint32(data[off : off+4])
		this.textOffset[i] = binary.LittleEndian.Uint32(data[off+4 : off+8])
		this.textlen[i] = data[off+8]
	}

	return
}

//查找归属地
func (this *IpLocation) FindByUint(ip uint32) string {
	var end uint32 = 0

	if ip>>24 != 0xff {
		end = this.index[(ip>>24)+1]
	}
	if end == 0 {
		end = uint32(len(this.ipEndArr))
	}
	idx := this.findIndexOffset(ip, this.index[ip>>24], end)
	off := this.textOffset[idx]
	return string(this.textData[off : off+uint32(this.textlen[idx])])
}

//二分查找
func (this *IpLocation) findIndexOffset(ip uint32, start, end uint32) uint32 {
	for start < end {
		mid := (start + end) / 2
		if ip > this.ipEndArr[mid] {
			start = mid + 1
		} else {
			end = mid
		}
	}

	if this.ipEndArr[end] >= ip {
		return end
	}

	return start
}

//获取ip归属地
func (this *IpLocation) Find(ipstr string, dataType int, callback string) string {
	ip := net.ParseIP(ipstr)
	if ip == nil || ip.To4() == nil {
		return "error"
	}

	var fields [7]string
	locationStr := this.FindByUint(binary.BigEndian.Uint32([]byte(ip.To4())))
	tmp := strings.Split(locationStr, "\t")
	for i := 0; i < len(tmp); i++ {
		fields[i] = tmp[i]
	}

	switch dataType {
	case 1:
		return ip.String() + "\t" + fields[0] + " " + fields[1] + " " + fields[2] + " " + fields[4] + " " + fields[5] + " " + fields[6]
	case 2:
		type locationInfo struct {
			Ret  string    `json:"ret"`
			Ip   string    `json:"ip"`
			Data [6]string `json:"data"`
		}
		var location locationInfo
		location.Ret = "ok"
		location.Data[0] = fields[0]
		location.Data[1] = fields[1]
		location.Data[2] = fields[2]
		location.Data[3] = fields[4]
		location.Data[4] = fields[5]
		location.Data[5] = fields[6]
		location.Ip = ip.String()
		result, err := json.Marshal(location)
		if err != nil {
			fmt.Println("error:", err)
			return "error"
		}
		if callback != "" {
			return callback + "(" + string(result) + ")"
		} else {
			return string(result)
		}
	case 3:
		type locationData struct {
			Country string `xml:"country"`
			Region  string `xml:"region"`
			City    string `xml:"city"`
			Isp     string `xml:"isp"`
			Zip     string `xml:"zip"`
			Zone    string `xml:"zone"`
		}
		type locationInfo struct {
			Ret  string       `xml:"ret"`
			Ip   string       `xml:"ip"`
			Data locationData `xml:"data"`
		}
		var location locationInfo
		location.Ret = "ok"
		location.Ip = ip.String()
		location.Data = locationData{
			Country: fields[0],
			Region:  fields[1],
			City:    fields[2],
			Isp:     fields[4],
			Zip:     fields[5],
			Zone:    fields[6],
		}

		result, err := xml.MarshalIndent(location, " ", " ")
		if err != nil {
			fmt.Println("error:", err)
			return "error"
		} else {
			return string(result)
		}
	}

	return "error"
}

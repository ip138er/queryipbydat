package main

import (
	"fmt"
)

//data type
const (
	DataTypeTxt   = 1
	DataTypeJsonp = 2
	DataTypeXml   = 3
)

func main() {
	location := new(IpLocation)
	location.InitFromFile("../ip.dat")
	result := location.Find("255.255.255.255", DataTypeJsonp, "find")
	fmt.Println(result)
}

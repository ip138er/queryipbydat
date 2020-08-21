#include <stdio.h>
#include "ipLocation.h"
#include "ipLocation2.h"

int main() {
	char result[128];

	//方法一载入内存查找
	initLocation("../ip.dat");
	findLocation("8.8.8.8", result);
	printf("%s\n", result); //utf-8

	//方法二文件查找
	initLocation2("../ip.dat");
	findLocation2("0.0.0.0", result);
	printf("%s\n", result);
	findLocation2("255.255.255.255", result);
	printf("%s\n", result);
	findLocation2("152.32.193.0", result);
	printf("%s\n", result);
	findLocation2("1.0.2.1", result);
	printf("%s\n", result);
	findLocation2("110.81.112.0", result);
	printf("%s\n", result);

	return 0;
}
#include <stdio.h>
#include "ipLocation.h"
//ÒýÓÃ
int main() {
	char result[128];
	initLocation("../ip.dat");
	findLocation("255.255.255.255", result);
	printf("%s", result); //ip.dat ip¹éÊôµØÊÇutf-8±àÂë
	return 0;
}
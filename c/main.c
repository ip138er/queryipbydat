#include <stdio.h>
#include "ipLocation.h"

int main() {
	char result[128];
	initLocation("../ip.dat");
	findLocation("255.255.255.255", result);
	printf("%s", result); //utf-8
	return 0;
}
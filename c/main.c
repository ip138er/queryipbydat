#include <stdio.h>
#include "ipLocation.h"

int main() {
	char result[128];
	initLocation("../ip.dat");
	findLocation("8.8.8.8", result);
	printf("%s", result); //utf-8
	return 0;
}
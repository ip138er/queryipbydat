#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

typedef unsigned char byte;
typedef unsigned int uint;

#if !defined(B2IL)
#define B2IL(b) (((b)[0] & 0xFF) | (((b)[1] << 8) & 0xFF00) | (((b)[2] << 16) & 0xFF0000) | (((b)[3] << 24) & 0xFF000000))
#endif

struct {
	FILE *file;
	uint textOffset; 
	uint total;     
} ipLocation2;

uint readIntData(uint offset){
	fseek(ipLocation2.file, (long)offset, SEEK_SET);
	byte byteBuffer[4];
	fread(&byteBuffer, sizeof(byte), sizeof(byteBuffer), ipLocation2.file);
	return B2IL(byteBuffer);
}

uint readByteData(uint offset){
	fseek(ipLocation2.file, (long)offset, SEEK_SET);
	byte byteBuffer[1];
	fread(&byteBuffer, sizeof(byte), sizeof(byteBuffer), ipLocation2.file);
	return byteBuffer[0]&0xFF;
}

uint ip2int2(char* ipstr) {
	int p1N, p2N, p3N, p4N;
	sscanf(ipstr, "%d.%d.%d.%d", &p1N, &p2N, &p3N, &p4N);
	uint ip = (p1N << 24) + (p2N << 16) + (p3N << 8) + p4N;
	return ip;
}

void initLocation2(const char *fileName) {
	ipLocation2.file = fopen(fileName, "rb");
	fseek(ipLocation2.file, 0, SEEK_END);
	long size = ftell(ipLocation2.file);

	ipLocation2.textOffset = readIntData(0);
	ipLocation2.total = (ipLocation2.textOffset - 4 - 256 * 4) / 9;

	//printf("%u\n", ipLocation2.total);
}

void findLocation2(char* ipstr, char * result) {
	uint ip = ip2int2(ipstr);
	uint left = 0;
	uint right = 0;
	uint first = (ip >> 24);
	if(first == 0xff) {
		left = readIntData(4+(first-1)*4)+1;
		right = ipLocation2.total;
	}else{
		left = readIntData(4+first*4);
		right = readIntData(4+(first+1)*4)-1;
		if(right < 1){
			right = ipLocation2.total;
		}
	}
	//printf("%u %u %u\n", first, left, right);


	int i=0;
	while(i<23){
		uint middle = (long)floor((left+right)/2);
		if(middle == left){
			uint ipOffset = 4 + 256*4 + right*9;
			uint textOffset = readIntData(ipOffset+4);
			uint textLength = readByteData(ipOffset+8);

			fseek(ipLocation2.file, (long)(ipLocation2.textOffset+textOffset), SEEK_SET);
			byte byteBuffer[textLength];
			fread(&byteBuffer, sizeof(byte), sizeof(byteBuffer), ipLocation2.file);

			memcpy(result, &byteBuffer, sizeof(byteBuffer));
			result[sizeof(byteBuffer)] = '\0';
			return;
		}

		//printf("%u %u %u\n", left, right, middle);
		uint middleOffset = 4 + 256*4 + middle*9;
		uint middleIp = readIntData(middleOffset);
		if(ip <= middleIp){
			right = middle;
		}else{
			left = middle;
		}
		i++;
	}
	return;
}

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef unsigned char byte;
typedef unsigned int uint;
#define B2IL(b) (((b)[0] & 0xFF) | (((b)[1] << 8) & 0xFF00) | (((b)[2] << 16) & 0xFF0000) | (((b)[3] << 24) & 0xFF000000))

struct {
	uint index[256]; 
	uint*ipEndArr;  
	uint*textOffset; 
	uint*textlen;   
	byte*textData;
	uint count;     
} ipLocation;

void initLocation(const char *fileName) {
	FILE *file = fopen(fileName, "rb");
	fseek(file, 0, SEEK_END);
	long size = ftell(file);
	fseek(file, 0, SEEK_SET);

	byte *data = (byte *)malloc(size * sizeof(byte));
	fread(data, sizeof(byte), (size_t)size, file);

	byte byteBuffer[4];
	memcpy(byteBuffer, data, 4);	
	int textoffset = B2IL(byteBuffer);

	ipLocation.textData = (byte *)malloc((size - textoffset) * sizeof(byte));
	memcpy(ipLocation.textData, data + textoffset, size - textoffset);
	int count = (textoffset - 4 - 256 * 4) / 9;
	ipLocation.count = count;

	for (int i = 0; i < 256; i++) {
		int offset = 4 + i * 4;
		memcpy(byteBuffer, data + offset, 4);
		ipLocation.index[i] = B2IL(byteBuffer);
	}

	ipLocation.ipEndArr = (uint*)malloc(count * sizeof(uint));
	ipLocation.textOffset = (uint*)malloc(count * sizeof(uint));
	ipLocation.textlen = (uint*)malloc(count * sizeof(uint));
	for (int i = 0; i < count; i++) {
		int offset = 4 + 1024 + i * 9;
		memcpy(byteBuffer, data + offset, 4);
		ipLocation.ipEndArr[i] = B2IL(byteBuffer);
		memcpy(byteBuffer, data + offset + 4, 4);
		ipLocation.textOffset[i] = B2IL(byteBuffer);
		ipLocation.textlen[i] = data[offset + 8];
	}
	return;
}

uint findIndexOffset(uint ip, uint start, uint end) {
	while (start < end) {
		int mid = (start + end) / 2;
		if (ip > ipLocation.ipEndArr[mid]) {
			start = mid + 1;
		}
		else {
			end = mid;
		}
	}
	if (ipLocation.ipEndArr[end] >= ip) {
		return end;
	}
	return start;
}

uint ip2int(char* ipstr) {
	int p1N, p2N, p3N, p4N;
	sscanf(ipstr, "%d.%d.%d.%d", &p1N, &p2N, &p3N, &p4N);
	uint ip = (p1N << 24) + (p2N << 16) + (p3N << 8) + p4N;
	return ip;
}

void findLocation(char* ipstr, char * result) {
	uint ip = ip2int(ipstr);
	uint end = 0;
	if (ip >> 24 != 0xff) {
		end = ipLocation.index[(ip >> 24) + 1];
	}
	if (end == 0) {
		end = ipLocation.count;
	}
	uint idx = findIndexOffset(ip, ipLocation.index[ip >> 24], end);
	memcpy(result, ipLocation.textData + ipLocation.textOffset[idx], ipLocation.textlen[idx]);
	result[ipLocation.textlen[idx]] = '\0';
	return;
}

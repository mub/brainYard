/*
 * Author: Michael Bergens, inspired by many good books
 */
#include <iostream>
#include <sstream>
#include <iomanip>

using namespace std;

#define COUNT_TABLE_SIZE 256
static unsigned char * buildCountTable() {
    unsigned char * result = new unsigned char[COUNT_TABLE_SIZE];
    result[0] = 0;
    for(int i = 1; i < COUNT_TABLE_SIZE; i++) {
        result[i] = (i & 1) + result[i >> 1];
    }
    return result;
}
static const unsigned char * bitCountsTable = buildCountTable();

int main(int argc, char * argv[]) {
    if(argc < 2) {
        cerr << "Must specify input value(s)" << endl;
        return 1;
    }
    stringstream hexConvert;
    hexConvert << hex << argv[1]; // better parse it as hex
    unsigned int source; // = atoi(argv[1]);
    hexConvert >> source;
    unsigned /* !! */ char * chars =  (unsigned char *) &source;
    int result = bitCountsTable[chars[0]]
        + bitCountsTable[chars[1]]
        + bitCountsTable[chars[2]]
        + bitCountsTable[chars[3]];
    cout << "Source: 0x" << hex << uppercase << source << dec << ", result: " << result << endl;
}


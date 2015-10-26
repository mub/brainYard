#include <iostream>
#include <sstream>
#include <iomanip>

using namespace std;

static const int POW_OF_2[] = {1, 2, 4, 8, 16};
static const int COUNT_MASKS[] = {0x55555555, 0x33333333, 0x0F0F0F0F, 0x00FF00FF, 0x0000FFFF};

unsigned int countByKernighanBrian(unsigned int src) {
  unsigned int result = 0;
  for(result = 0; src; result++) src &= src - 1;
  return result;
}

unsigned int countByCondensing(unsigned int src) {
    unsigned int result = src - ((src >> 1) & COUNT_MASKS[0]);
    result = ((result >> POW_OF_2[1]) & COUNT_MASKS[1]) + (result & COUNT_MASKS[1]);
    result = ((result >> POW_OF_2[2]) + result) & COUNT_MASKS[2];
    result = ((result >> POW_OF_2[3]) + result) & COUNT_MASKS[3];
    result = ((result >> POW_OF_2[4]) + result) & COUNT_MASKS[4];
    return result;
}

unsigned int reverse_bits(unsigned int input) {
    // complixity O(log [no.of.bits]) = O(1)
    // On 32 bit machines it takes 5 steps (logical)

    input = (input & 0x55555555) <<  1 | (input & 0xAAAAAAAA) >>  1;
    input = (input & 0x33333333) <<  2 | (input & 0xCCCCCCCC) >>  2;
    input = (input & 0x0F0F0F0F) <<  4 | (input & 0xF0F0F0F0) >>  4;
    input = (input & 0x00FF00FF) <<  8 | (input & 0xFF00FF00) >>  8;
    input = (input & 0x0000FFFF) << 16 | (input & 0xFFFF0000) >> 16;

    return input;
}

int main(int argc, char * argv[]) {
  // showbase <<   - this would render the base uppercase too

  if(argc < 2) {
    cerr << "Must specify input value(s)" << endl;
    return 1;
  }

  unsigned int vals[ argc - 1 ];

  for(int ix = 1; ix < argc; ix++) {
    stringstream source;
    source << hex << argv[ix];
    unsigned int arg;
    source >> arg;

    vals[ix - 1] = arg;
    printf("%08X -> %08X\n", arg, reverse_bits(arg));
  }
  argc--;

  cout << "** Bit Counts: " << endl;

  for(int ix = 0; ix < argc; ix++) {
    printf("%08X: Brian: %d, Condense: %d\n", vals[ix], countByKernighanBrian(vals[ix]), countByCondensing(vals[ix]));
  }

  // comparing printf to cout:
  cout << "First source: 0x" << hex << uppercase << cout.fill('0') << setw( 8 ) << vals[0] << endl;
} 
      

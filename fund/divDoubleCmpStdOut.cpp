#include <cstdio>
#include <limits>
#include <stdlib.h>
#include <math.h>
#include <clocale>

bool isEqual(double a, double b) {
  bool eps = std::numeric_limits<double>::epsilon() * 100;
  return fabs(a - b) < eps;
}

int main(int argc, char** argv) {
 // setlocale(LC_ALL, "en_US.utf8");
  std::setlocale(LC_ALL, "en_US.utf8");
  printf("Running %s; count: %d\n%s\n", argv[0], argc - 1, argc > 1 ? argv[1] : "<NONE>");

  double d = (100 + 1/3.0) - 100;

  printf("(100 + 1/3.0) - 100 :: %.12g\n", d);

  double a = 1/3.0;

  printf("a: %.12g; d: %.12g; diff: %.6g\n", a, d, fabs(a-d));

  bool isEq = a == d;
  printf("By the op:: a==d: %i<==%s\n", isEq, isEq ? "Equals" : "Different");
  isEq = isEqual(a, d);
  printf("By the fun:: a==d: %i<==%s\n", isEq, isEq ? "Equals" : "Different");

  if(0) printf("\n\nZero is EQ\n\n");
  if(!0) printf("Zero is NEQ\n\n");
  if(1) printf("One is EQ\n\n");
  if(!1) printf("One is NEQ\n");

  printf("\nMachine Epsilon: %18.12g\n", std::numeric_limits<double>::epsilon());
  printf("Signed:\n\tInt digits: %d; min=%'d; max=%'d;\n\tlong digits: %d; min=%'lld; max=%'lld;\n",
      std::numeric_limits<int>::digits10,
      std::numeric_limits<int>::min(),
      std::numeric_limits<int>::max(),
      std::numeric_limits<long long>::digits10,
      std::numeric_limits<long long>::min(),
      std::numeric_limits<long long>::max()
      );
 
  printf("Unsigned:\n\tInt digits: %d; min=%'u; max=%'u;\n\tlong digits: %d; min=%'llu; max=%'llu;\n",
      std::numeric_limits<unsigned int>::digits10,
      std::numeric_limits<unsigned int>::min(),
      std::numeric_limits<unsigned int>::max(),
      std::numeric_limits<unsigned long long>::digits10,
      std::numeric_limits<unsigned long long>::min(),
      std::numeric_limits<unsigned long long>::max()
      );
  return 0;
  /*
   * %d--> for int
   * %u--> for unsigned int
   * %ld--> for long int
   * %lu--> for unsigned long int
   * %lld--> for long long int
   * %llu--> for unsigned long long int
   */
}


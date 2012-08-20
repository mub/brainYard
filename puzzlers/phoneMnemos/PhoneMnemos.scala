import scala.collection.mutable.MultiMap

class PhoneMnemos(dictionary: Set[String]) {
   // Straight index from a digit to letters
   val keyMnemos = Map('2' -> "ABC", '3' -> "DEF", '4' -> "GHI", '5' -> "JKL",
     '6' -> "MNO", '7' -> "PQRS", '8' -> "TUV", '9' -> "WXYZ")

   // Inverted index from a char to the digit
  /*private */val charToNum: Map[Char, Char] = for ((digit, str) <- keyMnemos; ltr <- str) yield (ltr -> digit)

  def wordToNums(word: String): String = word map charToNum // iterates through all the characters, maps each to
  // the digit and assemples the return value.


  val numsToWords: Map[String, Set[String]] = dictionary groupBy wordToNums //Map digit sequence to matching words

  def toWords(nums: String): Set[List[String]] = if (nums.isEmpty) Set(List()) else {
    for {
      splitPoint <- 1 to nums.length // iterate from pos 1 to the end
      head = nums take splitPoint // get everything before split point
      if numsToWords.contains(head) // weed out combinations that are not words
      words <- numsToWords(head) // turn the digit sequence to the matching words set
      tail <- toWords(nums drop splitPoint) // recurse the tail past the split
    } yield words :: tail
  }.toSet

  def toPhrases(nums: String): Set[String] = toWords(nums) map (_ mkString " ")
}

object PhoneMnemosScala extends App {
  val gen = new PhoneMnemos(Set.newBuilder.++=(io.Source.fromFile(args(0)).getLines()).result())

  io.Source.fromFile(args(1)).getLines().foreach( line => {
    printf("%n%s -> %s", line, gen.toPhrases(line))
  })
}
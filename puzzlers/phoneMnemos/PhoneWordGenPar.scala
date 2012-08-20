import collection.parallel.immutable.ParVector

/**
 * @author mbergens Michael Bergens
 */
class PhoneWordGenPar(words: ParVector[String]) {
  /**
   * Straight index from a digit to letters
   */
   val mnemonics = Map(
     '2' -> "ABC", '3' -> "DEF", '4' -> "GHI", '5' -> "JKL",
     '6' -> "MNO", '7' -> "PQRS", '8' -> "TUV", '9' -> "WXYZ"
   )

  /**
   * Inverted index from a char to the digit
   */
  /*private */val charCode: Map[Char, Char] = for ((digit, str) <- mnemonics; ltr <- str) yield (ltr -> digit)

  private def wordCode(word: String): String = word.toUpperCase map charCode

  /**
   * Map digit sequence to matching words:
  val wordsForNum: Map[String, List[String]] = words groupBy wordCode

  def encode(number: String): Set[List[String]] = if (number.isEmpty) Set(List()) else {
    for {
      splitPoint <- (1 to number.length).par
      word <- wordsForNum(number take splitPoint)
      rest <- encode(number drop splitPoint)
    } yield word :: rest
  }.toSet

  def translate(number: String): Set[String] = encode(number) map (_ mkString " ")
   */
}

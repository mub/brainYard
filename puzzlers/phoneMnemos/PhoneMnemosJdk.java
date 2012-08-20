import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhoneMnemosJdk {

    private static final String PARTIAL_MATCHES_PROP = "phone.gen.partial.match.on";
    private final Map<String, List<String>> wordToNumber = new HashMap<>();

    private boolean isPartialMatchEnabled;

    public PhoneMnemosJdk(final String dictFileName) throws IOException {
        final String[] lettersForDigs = {"", "", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ"};
        isPartialMatchEnabled = System.getProperty(PARTIAL_MATCHES_PROP) != null;
        char[] digitForLetter = new char[26];
        final int digitLimit = 10;

        for(int digit = 0; digit < digitLimit; digit++) {
            String letters = lettersForDigs[digit];
            for(int i = 0; i < letters.length(); i++) digitForLetter[letters.charAt(i) - 'A'] = (char) (digit + '0');
        }

        String word = null; // process the dict into the map:
        try (BufferedReader br = new BufferedReader(new FileReader(dictFileName))) {
            while ((word = br.readLine()) != null) {
                word = word.toUpperCase();
                char[] digitSeq = word.toCharArray();
                for (int i = 0; i < digitSeq.length; i++) digitSeq[i] = digitForLetter[digitSeq[i] - 'A'];
                String number = new String(digitSeq);
                List<String> words = wordToNumber.get(number);
                if (words == null) wordToNumber.put(number, words = new ArrayList<>());
                words.add(word);
            }
        }
        catch (ArrayIndexOutOfBoundsException x) {
            throw new IllegalArgumentException("Invalid dictionary entry:" + word);
        }
    }

    private List<String> sentences(char[] digits, int start) {
        List<String> result = new ArrayList<>(words(digits, start, digits.length - start));
        for(int split = start + 1; split < digits.length; split++) {
            result.addAll(cartesianProduct(words(digits, start, split - start), sentences(digits, split)));
        }
        return result;
    }

    private List<String> words(char[] digits, int offset, int len) {
        List<String> result = wordToNumber.get(new String(digits, offset, len));
        return result == null ? Collections.<String>emptyList() : result;
    }

    public List<String> cartesianProduct(final List<String> heads, final List<String> tails) {
        final List<String> result = new ArrayList<>();
        if(heads.isEmpty() && tails.isEmpty()) return result;
        if(!isPartialMatchEnabled && heads.isEmpty()) return tails;
        if(tails.isEmpty()) return heads;
        for(final String prefix: heads)
            for(final String suffix: tails) result.add(prefix + " " + suffix);
        return result;
    }

    public List<String> genWords(final String number) {
        return sentences(number.toCharArray(), 0);
    }

    public static void main(String[] args) throws IOException {

        final PhoneMnemosJdk runner = new PhoneMnemosJdk(args[0]);

        try(final BufferedReader br = new BufferedReader(new FileReader(args[1]))) {
            String input;
            while((input = br.readLine()) != null) {
                System.out.printf("\"%s\" -> %s%n", input, runner.genWords(input));
            }
        }
    }
}
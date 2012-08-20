import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.transform;
import static com.google.common.io.Files.readLines;
import static java.nio.charset.Charset.defaultCharset;

public class PhoneMnemosGuava {
    final static Function<List<String>, String> LIST_STRING_CONCATTER = new Function<List<String>, String>() {
                        @Override public String apply(@Nullable List<String> input) {
                            return Joiner.on(' ').join(input).trim();
                        }
    };// this function is called from a method, better not inline it
    final ImmutableMultimap<String, String> numsToWords;

    Set<String> toPhrases(final String nums) {
        if(nums == null || nums.length() < 1) return Collections.emptySet();
        Set<String> combinations = new HashSet<>();
        final int lastPos = nums.length() - 1;
        for(int pos = 0; pos < nums.length(); pos++) {
            final String head = nums.substring(0, pos + 1);
            if(!numsToWords.containsKey(head)) continue;
            Set<String> words = copyOf(numsToWords.get(head));

            Set<String> tails = copyOf(
                pos == lastPos ? Collections.<String>emptyList() : toPhrases(nums.substring(pos + 1))
            );
            combinations.addAll(transform(ImmutableList.copyOf(Sets.cartesianProduct(
                words, tails.isEmpty() ? Collections.singleton("") : tails)), LIST_STRING_CONCATTER));
        }
        return combinations;
    }

    public PhoneMnemosGuava(final Set<String> dictionary) {
        final ImmutableMap<Character, String> keyMnemos = ImmutableMap.<Character, String>builder()
            .put('2', "ABC").put('3', "DEF").put('4', "GHI").put('5', "JKL").put('6', "MNO").put('7', "PQRS")
            .put('8', "TUV").put('9', "WXYZ").build();

        final Map<Character, Character> charToNum = Maps.newHashMap();
        for (Map.Entry<Character, String> entry : keyMnemos.entrySet()) {
            final String letters = entry.getValue();
            for (int pos = 0; pos < letters.length(); pos++) {
                charToNum.put(letters.charAt(pos), entry.getKey());
            }
        }

        numsToWords = Multimaps.index(dictionary, new Function<String, String>() {
            @Override public String apply(String word) {// function in a constructor - let it be GC'ed
                final char[] result = new char[word.length()];
                for (int pos = 0; pos < word.length(); pos++) result[pos] = charToNum.get(word.charAt(pos));
                return new String(result);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        final PhoneMnemosGuava runner = new PhoneMnemosGuava(copyOf(readLines(new File(args[0]), defaultCharset())));
        final List<String> nums = readLines(new File(args[1]), defaultCharset());
        for(final String num: nums) System.out.printf("%n%s -> %s", num, runner.toPhrases(num));
    }
    // here's one example when functional approach in Java falls flat: this FP sequence only gets the tuples iterable
    // and in the end there is no functional feature to convert it to the map, neither in Guava nor in
    // Apache commons collections.
    /*Iterable<Tuple2<Character, Character>> charCodeTuples = concat(transform(
mnemonics.entrySet(), new Function<Map.Entry<Character, String>, List<Tuple2<Character, Character>>>() {
@Override public List<Tuple2<Character, Character>> apply(final Map.Entry<Character, String> input) {

return ImmutableList.copyOf(transform(
   Chars.asList(input.getValue().toCharArray()), new Function<Character, Tuple2<Character, Character>>() {
       @Override public Tuple2<Character, Character> apply(@Nullable Character letter) {
           return new Tuple2<>(letter, input.getKey());
       }
}));
}
})); // the loop version above is short, clear and does the job */

    /*// another example when FP just does not work, computing wordCode:
Iterables.toArray(concat(transform(Chars.asList(word.toCharArray()), new Function<Character, Character>() {
@Override public Character apply(@Nullable Character letter) {
return charCode.get(letter);
}
})), Character.class);  - now good luck turning it to array of primitives to pass to a String constructor*/
}

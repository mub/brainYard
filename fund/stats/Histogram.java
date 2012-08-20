import java.util.Random;

/**
 * @author mbergens Michael Bergens
 */
public class Histogram {

    private static final int CHAR_BAR_LEN = 80;
    private static int emitIndex = 0;
    private static Random random = new Random();
    /**
     * Emit the bell curve with noise
     */
    private static double emit() {

        final double r = 10 * random.nextGaussian();
        return r;
    }

    public static int[] histogram(final double[] data, final int binCount) {

        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for(final double d: data) {
            if(d > max) max = d;
            if(d < min) min = d;
        }

        final double binSize = (max - min)/binCount;
        int[] result = new int[binCount];

        for(final double d: data) {
                result[ d == max ? binCount - 1 : (int)Math.floor((d - min)/binSize) ] ++;
        }
        return result;
    }

    private static void peekDoubleHex(final double what, final String comment) {
        System.out.printf("%n%s: val=%g, toHex=%s, bits=%X, raw=%X", comment, what, Double.toHexString(what)
            , Double.doubleToLongBits(what), Double.doubleToRawLongBits(what));
    }
    public static void main(String[] args) {

        peekDoubleHex(1.5, "one and a half");
        peekDoubleHex(0.5, "half");
        peekDoubleHex(0.3, "one third");
        peekDoubleHex(3, "Three");
        peekDoubleHex(Double.NaN, "NaN");
        peekDoubleHex(Double.MIN_NORMAL, "Min Normal");
        peekDoubleHex(Double.MIN_VALUE, "Min Val");
        peekDoubleHex(Double.MAX_VALUE, "Max Val");
        peekDoubleHex(Double.NEGATIVE_INFINITY, "Neg Infinity");
        peekDoubleHex(Double.POSITIVE_INFINITY, "Positive Infinity");

        System.exit(0);
        final int sampleSize = Integer.valueOf(args[0]);
        final int binCount = Integer.valueOf(args[1]);

        final double[] data = new double[sampleSize];
        for(int i = 0; i < sampleSize; i++) data[i] = emit();
        int[] sizes = histogram(data, binCount);

        for(int s: sizes) {
            System.out.printf("%n%s", s);
        }
        int sum = 0;
        for(final int count: sizes) sum += count;
        System.out.printf("%nTotal count: %,d", sum);

    }
/*
for (int j=0; j < generate.length; j++) {
    for(int i = 0; i < intervals; i++) {
        double imin = mins + i * intervalWidth;
        double imax = mins + (intervalWidth) * (i + 1);
        if(i == intervals - 1) imax = Double.POSITIVE_INFINITY;
        if(i == 0) imin = Double.NEGATIVE_INFINITY;

        if (generate[j] >= imin && generate[j] < imax) {
                intervalValue[i]++;
                break;
        }
    }
}
 */
}

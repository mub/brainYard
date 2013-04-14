import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

/**
 * Implementation of Tic-Tac-Toe using bitmasks in the model, handles 2x2 up to 5x5 boards,
 * includes illustrative harness running from a flat text file.
 * @author Michael Bergens
 */
public class TicTacToeBitMasks {

    // Since we work heavily with bitmasks, better use straight up constants vs enums.
    /**
     * Game controller: game status - game not started.
     */
    public static final long NOT_STARTED = 2L << 32;
    /**
     * Game controller: game status - game in progress.
     */
    public static final long IN_PROGRESS = NOT_STARTED << 1;
    /**
     * Game controller: game status - ended in tie.
     */
    public static final long TIE = IN_PROGRESS << 1;
    /**
     * Game controller: game status - ended with no winner.
     */
    public static final long NO_WINNER = TIE << 1;
    /**
     * Used to distinguish ones set bits vs zero set bits in the board status.
     */
    private static final int[] ONE_ZERO_XOR_MASKS = {0, -1};

    /**
     * Winning masks for either X player or O player, switched by using {@link #ONE_ZERO_XOR_MASKS}
     */
    private long[] winMasks;
    /**
     * All cells for the given board set - used in game status analysis.
     */
    private long allCellsStatusMask;

    /**
     * The constructor builds all needed for fast running state recognition: the {@link #winMasks}.
     * This is part of the Game Model.
     * and the {@link #allCellsStatusMask} for the given board size.
     */
    public TicTacToeBitMasks(int size) {
        // diagonal mask offsets: \ TopLeft-BottomRight and / TopRight - BottomLeft
        final int diagTlBrOff = size << 1, diagTrBlOff = diagTlBrOff + 1;
        winMasks = new long[diagTlBrOff + 2];
        Arrays.fill(winMasks, 0L);
        long allCellsStat = 0; // mutable local to assign to the immutable class member in the end
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) {
                winMasks[x] |= 1L << x * size + y; // horizontal mask
                winMasks[x + size] |= 1L << (y * size  + x); // vertical mask
            }
            winMasks[diagTlBrOff] |= 1L << x * size + x; // the \ diagonal
            final int xLeftOff = size - x - 1;
            winMasks[diagTrBlOff] |= 1L << x * size + xLeftOff; // the / diagonal
            allCellsStat |= winMasks[x];
        }

        allCellsStatusMask = allCellsStat;
    }

    /**
     * Game Model + Controller: determine the state, return binary 1s of all winning positions so the rendering engine
     * can cross the winning positions using this mask.
     * This method is designed with toppety-top performance in mind: all primitive types, no objects,
     * fastest operations on any CPU (no muls, no divs, no mods).
     * @return HI - game status, namely {@link #NOT_STARTED}, {@link #IN_PROGRESS}, {@link #TIE}, {@link #NO_WINNER} or the index
     * of the dominant party (0 or 1) with the won mask for either or both in case of {@link #TIE}
     * along with the winning cell information in the LO, so the rendering engine can cross the winning paths easily.
     */
    public long result(final long state) {
        if(state == 0) return NOT_STARTED;
        int cellStatuses = (int)(state >>> 32);
        int values = (int)(cellStatuses & state);
        int[] results = new int[2];
        for(int oneZeroIx = 0; oneZeroIx < 2; oneZeroIx++) { // loops through the Zero's and One's mask sets
            for(final long mask: winMasks) {
                if(( cellStatuses & (values ^ ONE_ZERO_XOR_MASKS[oneZeroIx]) & mask) == mask) results[oneZeroIx] |= mask;
            }
        }
        return  results[0] == 0 ? // the 1 (X) player not a winner?
            (
                results[1] == 0 ?
                    (// the 0 (O) player isn't winner either; no winners - is the game over yet?
                        cellStatuses == allCellsStatusMask ? NO_WINNER : IN_PROGRESS
                    )
                    : results[1] /* zeros (O) won solo */
            ): // the 1 (X) player won, but is it solo win or tie?
            (
                results[1] == 0 ?
                    ( // the 0 (O) player isn't winner, the 1 (X) is solo winner
                        1L << 32 | results[0]
                    ): // otherwise a tie, combine masks:
                    (TIE | results[0] | results[1])
            );
    }

    /**
     * Illustrative harness: one game state case.
     */
    static class GameState {
        final long status;
        final String source;

        private GameState(long status, String source) {
            this.status = status;
            this.source = source;
        }
        @Override public String toString() { return source + String.format(":%016X", status); }
    }

    /**
     * Illustrative harness: builds the case from textual matrix in the array of lines. For player 0,
     * the following symbols work the same: uppercase O, lowercase o or the digit zero.
     * For player 1 the following symbols work: X, x, and the digit one. Use dot '.' for unhit cell.
     * @param lines board state specs line-by-line using the symbols described above.
     * @param lineNumberInFile for diagnostics in error message if any.
     */
    private static GameState buildCase(final String[] lines, final int lineNumberInFile) {

        long accumulator = 0L;
        final int caseSize = lines.length;
        final StringBuilder caseSource = new StringBuilder((caseSize + 1) * caseSize);
        for(int lineIndex = 0; lineIndex < caseSize; lineIndex++) {
            final String source = lines[lineIndex];
            if(lineIndex > 0) caseSource.append(' ');
            caseSource.append(source);
            for(int pos = 0; pos < caseSize; pos++) {
                final char ch = source.charAt(pos);
                final int bitPos = lineIndex * caseSize + pos;
                switch(ch) {
                    case 'X': case 'x': case '1': // set status and the value bit
                        accumulator |=  1L<<(bitPos + 32) | 1L << bitPos;
                        break;
                    case 'O': case 'o': case '0': // set status only
                        accumulator |=  1L<<(bitPos + 32);
                        break;
                    case '.':case '-': // leave everything as is, status zero, value unimportant
                        break;
                    default:
                        throw new IllegalArgumentException("Line #" + lineNumberInFile
                                            + ": \"" + source + "\" - unsupported state specification");
                }
            }
        }
        return new GameState(accumulator, caseSource.toString());
    }

    /**
     * Illustrative harness: build textual presentation of the given mask for the given size,
     * used to show feedback for an illustrative case.
     */
    private static String getMaskTextual(long mask, int size) {
        final int status = (int)(mask >>> 32);
        final String statusText;
        switch (status) {
            case (int)(IN_PROGRESS >>> 32): return "in progress";
            case (int)(NOT_STARTED >>> 32): return "not started";
            case (int)(NO_WINNER >>> 32): return "no winner";
            case (int)(TIE >>> 32):
                statusText = "tie";
                break;
            default:
                statusText = ((mask >>> 32) == 0 ? "O" : "X") + " - won";
        }
        StringBuilder result = new StringBuilder((size + 1) * size + statusText.length() + 2);
        result.append(statusText).append(": ");
        for(int x = 0; x < size; x++) {
            for(int y = 0; y < size; y++) result.append((mask & ( 1 << x * size + y)) == 0 ? '`': '+');
            result.append(' ');
        }
        return result.toString();
    }

    /**
     * Illustrative game case encaplusation for a board size.
     */
    private final static class BoardHandler {
        int lineIndex = -1;
        final int caseSize;
        final int caseLastIndex;
        final String[] caseLines;
        int lineCount = 0; // for diagnostics
        final TicTacToeBitMasks caseRunner;
        String currentLine;

        BoardHandler(int caseSize) {
            this.caseSize = caseSize;
            caseLastIndex = caseSize - 1;
            caseLines = new String[caseSize];
            caseRunner = new TicTacToeBitMasks(caseSize);
        }

        private void process(final String line) {
            currentLine = line;
            lineCount++;
            if(line.isEmpty() || line.charAt(0) == '#') return;
            if(line.length() != caseSize) throw new IllegalArgumentException("Line #" + lineCount
                + ": \"" + line + "\" must be size of " + caseSize);
            //noinspection ConstantConditions
            caseLines[++lineIndex] = line;
            if(lineIndex == caseLastIndex) {
                final GameState state = buildCase(caseLines, lineCount);
                final long result = caseRunner.result(state.status);
                System.out.printf("%n%s -> %016X (%s)", state, result, getMaskTextual(result, caseSize));
                lineIndex = -1;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            BoardHandler boardHandler = null;
            String rawLine;
            while((rawLine = reader.readLine()) != null) {
                final String line = rawLine.trim();
                if(boardHandler == null) { // first meaningful line sets size for the rest in the file
                    boardHandler = new BoardHandler(line.length());
                }
                boardHandler.process(line);
            }
        }
    }
}

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.fill;

/**
 * Hard-coding 2-dim board. Todo: add network play
 *
 * @author mbergens Michael Bergens
 */
public class Battleship {

    /**
     * Game view - interface for a concrete rendering context if any.
     */
    static interface RenderContext{
        void shipSegment(int horiz, int vert, Ship ship, int segment);
        void hitCell(int horiz, int vert);
        void unhitCell(int horiz, int vert);
    }

    private static class Player {
        public final int id;
        private Player(int id) { this.id = id; }
        private Board board;

        public void allocate(int hSize, int vSize) {
            board = new Board(hSize, vSize);
        }

        public boolean hasBoard() { return board != null; }

        @Override public String toString() { return "Player{" + id + "}"; }

        public Board getBoard() { return board; }
    }

    private static enum GameStatus {
        /**
         * In INIT state, deployments and undeployments are allowed but shooting is not.
         */
        INIT,
        /**
         * In STARTED stage, no deployments or undeployments, just shooting.
         */
        STARTED
    }
    /**
     * Game controller: command for the board.
     */
    private static enum Command {
        ALLOCATE, BOARD, DEPLOY, UNDEPLOY, SHOOT, FLEET, HELP, PLAYER;

        public static Command getForInput(final char input) {
            final char firstChar = Character.toUpperCase(input);
            for (final Command cmd : Command.values()) {
                if (cmd.name().charAt(0) == firstChar) return cmd;
            }
            throw new IllegalArgumentException("Unsupported command char: " + input);
        }
    }

    private static final Pattern COMMA_SPLIT = Pattern.compile(",");
    /**
     * Board placement algoritm relies on the fact that the first character has the lowest integer value,
     * and the last one has the highest, keep it this way or revise the board placement algorithm.
     *
     * @see Fleet
     */
    private static final String VALID_SHIP_NAMES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * For efficient shipnames storage
     */
    private static final char SHIP_NAME_LOWEST = VALID_SHIP_NAMES.charAt(0);
    private static final char SHIP_NAME_HIGHEST = VALID_SHIP_NAMES.charAt(VALID_SHIP_NAMES.length() - 1);
    private static final char UNHIT_CELL = '.';
    private static final char MYSTERY_CELL = '`';
    private static final char HIT_CELL = '*';

    private static boolean isShipCell(final char cell) {
        return cell != UNHIT_CELL && cell != HIT_CELL;
    }
    /**
     * Game model for 2D board: ship orientation.
     */
    static enum ShipOrientation {
        VERTICAL, HORIZONTAL;

        static ShipOrientation deserialize(final char input) {
            final char firstChar = Character.toUpperCase(input);
            for (final ShipOrientation o : ShipOrientation.values()) {
                if (o.name().charAt(0) == firstChar) return o;
            }
            throw new IllegalArgumentException("Unsupported spec for Orientation: " + input);
        }
    }

    /**
     * Game controller: grid probe result type.
     */
    static enum ShootResultType {
        BLANK, NEW_HIT, DUPE_HIT, SUNK
    }

    /**
     * Game controller, ship deploy result: either successful or collision with another ship deployed in the position.
     */
    static enum DeployResultType {
        SUCCESS, OCCUPIED
    }

    /**
     * Game model - a ship on a 2D board;
     */
    static class Ship {
        private final char name;
        private final int left, top, size;
        private final ShipOrientation orientation;
        private final String toStr;
        private char[] segmentHealth;
        static final char HEALTHY_SEGMENT = '+';
        static final char BUSTED_SEGMENT = '!';

        Ship(final char name, final int left, final int top, int size, final ShipOrientation orientation) {
            if (size < 1) throw new IllegalArgumentException("Phantom ships are not allowed on this board");
            this.name = name;
            this.left = left;
            this.top = top;
            this.size = size;
            this.orientation = orientation;
            this.segmentHealth = new char[size];
            fill(segmentHealth, HEALTHY_SEGMENT);
            toStr = "Ship{" + name + '/' + size + ':' + left + ',' + top + '-' + orientation.name().charAt(0);
        }

        public char getName() {
            return name;
        }

        public int getLeft() {
            return left;
        }

        public int getTop() {
            return top;
        }

        public int getSize() {
            return size;
        }

        public boolean isSunk() {
            // return Arrays.binarySearch(segmentHealth, HEALTHY) < 0; // nah, not with 1 to 4 size arrays
            for (char ch : segmentHealth) if (ch == HEALTHY_SEGMENT) return false;
            return true;
        }

        public ShipOrientation getOrientation() {
            return orientation;
        }

        public boolean isHealthy(final int segment) {
            assert segment > 0 && segment < size : "Invalid segment index " + segment + " for ship " + this;
            return segmentHealth[segment] == HEALTHY_SEGMENT;
        }

        public void hit(final int segment) {
            assert isHealthy(segment) : "Repetitive hit on " + this + ", segment #" + segment;
            segmentHealth[segment] = BUSTED_SEGMENT;
        }

        @Override public String toString() {
            return toStr + '*' + new String(segmentHealth) + '}';
        }
    }

    /**
     * Game controller - action result: result type and the ship involved if any.
     *
     * @param <T> type for the action result.
     */
    static abstract class ActionResult<T extends Enum> {
        private final T type;
        private final Ship ship;

        ActionResult(T type, Ship ship) {
            this.type = type;
            this.ship = ship;
        }

        public T getType() {
            return type;
        }

        public Ship getShip() {
            return ship;
        }
    }

    static class ShootResult extends ActionResult<ShootResultType> {
        ShootResult(ShootResultType type, Ship ship) {
            super(type, ship);
        }
    }

    static class DeployResult extends ActionResult<DeployResultType> {
        DeployResult(DeployResultType type, Ship ship) {
            super(type, ship);
        }
    }

    /**
     * Game model: bunch of ships.
     */
    static class Fleet {
        private final Ship[] fleet = new Ship[SHIP_NAME_HIGHEST - SHIP_NAME_LOWEST + 1];

        private int index(final char name) {
            assert VALID_SHIP_NAMES.indexOf(name) >= 0 : "Invalid ship name: " + name;
            return name - SHIP_NAME_LOWEST;
        }

        private int index(final Ship ship) {
            return index(ship.getName());
        }

        public boolean hasShip(final char name) {
            return fleet[index(name)] != null;
        }

        public boolean hasShip(final Ship ship) {
            return fleet[index(ship.getName())] != null;
        }

        public void add(final Ship ship) {
            assert !hasShip(ship) : "Ship " + ship + " already in the set: " + fleet[index(ship)];
            fleet[index(ship)] = ship;
        }

        public Ship getShip(final char name) {
            return fleet[index(name)];
        }

        public List<Ship> getAllShips() {
            final List<Ship> result = new ArrayList<>(VALID_SHIP_NAMES.length());
            for (final Ship ship : fleet) {
                if (ship != null) result.add(ship);
            }
            return result;
        }

        public void remove(final Ship ship) {
            fleet[index(ship)] = null;
        }
    }

    static class Board {

        protected final int horizSize;
        protected final int vertSize;
        protected final char[] grid;
        protected final Fleet fleet = new Fleet();

        protected static interface IndexProvider {
            /**
             * Content grid index
             */
            int index(Ship ship, int segment);

            /**
             * Segment number for the given grid cell and given ship, caller responsible for verification of this cell
             * being occupied by this ship.
             */
            int segment(final Ship ship, int h, int v);
        }

        private final EnumMap<ShipOrientation, IndexProvider> indexProviders = new EnumMap<>(ShipOrientation.class);

        public boolean isGameOver() {
            final List<Ship> ships = getAllShips();
            if (ships.isEmpty()) return false;
            for (final Ship ship : ships) {
                if (!ship.isSunk()) return false;
            }
            return true;
        }

        public Board(final int horizSize, final int vertSize) {
            this.horizSize = horizSize;
            this.vertSize = vertSize;
            grid = new char[horizSize * vertSize];
            fill(grid, UNHIT_CELL);
            indexProviders.put(ShipOrientation.HORIZONTAL, new IndexProvider() {
                @Override public int index(final Ship ship, final int segment) {
                    if (ship.getLeft() + segment >= horizSize) throw new IllegalArgumentException(
                        "Attempt to access segment " + segment + " of the ship " + ship + ", horizontal size overflow");
                    return horizSize * ship.getTop() + ship.getLeft() + segment;
                }

                @Override public int segment(Ship ship, int h, int v) {
                    final int result = h - ship.getLeft();
                    assert result < ship.getSize() : "Invalid segment index " + result + " on " + ship + " on " + this;
                    return result;
                }
            });
            indexProviders.put(ShipOrientation.VERTICAL, new IndexProvider() {
                @Override public int index(final Ship ship, final int segment) {
                    if (ship.getTop() + segment >= vertSize) throw new IllegalArgumentException(
                        "Attempt to access segment " + segment + " of the ship " + ship + ", vertical size overflow");
                    return horizSize * (ship.getTop() + segment) + ship.getLeft();
                }

                @Override public int segment(final Ship ship, final int h, final int v) {
                    final int result = v - ship.getTop();
                    assert result < ship.getSize() : "Invalid segment index " + result + " on " + ship + " on " + this;
                    return result;
                }
            });
        }

        public List<Ship> getAllShips() {
            return fleet.getAllShips();
        }

        public int getHorizSize() {
            return horizSize;
        }

        public int getVertSize() {
            return vertSize;
        }

        public int getCellIndex(final Ship ship, final int segment) {
            return indexProviders.get(ship.getOrientation()).index(ship, segment);
        }

        public int getSegmentIndex(final Ship ship, final int h, final int v) {
            return indexProviders.get(ship.getOrientation()).segment(ship, h, v);
        }

        public DeployResult deploy(final Ship ship) {
            for (int segment = 0; segment < ship.getSize(); segment++) {
                final char deployedShipNameIfAny = grid[getCellIndex(ship, segment)];
                if (isShipCell(deployedShipNameIfAny)) {
                    return new DeployResult(DeployResultType.OCCUPIED, fleet.getShip(deployedShipNameIfAny));
                }
            }
            for (int segment = 0; segment < ship.getSize(); segment++) {
                grid[getCellIndex(ship, segment)] = ship.getName();
            }
            fleet.add(ship);
            return new DeployResult(DeployResultType.SUCCESS, ship);
        }

        public boolean undeploy(final char shipName) {
            if (!fleet.hasShip(shipName)) return false;
            final Ship ship = fleet.getShip(shipName);
            for (int segment = 0; segment < ship.getSize(); segment++) {
                final char deployedShipNameIfAny = grid[getCellIndex(ship, segment)];

                if (deployedShipNameIfAny != shipName) throw new IllegalStateException("Ship " + ship
                    + ": invalid board image: " + this);

                grid[getCellIndex(ship, segment)] = UNHIT_CELL;
            }
            fleet.remove(ship);
            return true;
        }

        public boolean hasShip(final char name) {
            return fleet.hasShip(name);
        }

        public Ship getShip(final char name) {
            return fleet.getShip(name);
        }

        public ShootResult shoot(final int h, final int v) {

            assert h >= 0 && h < horizSize && v >= 0 && v < vertSize :
                "Invalid board coordinates: h=" + h + ", v=" + v + ", board: " + this;
            final int gridIx = gridIndex(h, v);
            final char cellVal = grid[gridIx];

            if (!isShipCell(cellVal)) {
                grid[gridIx] = HIT_CELL;
                return new ShootResult(ShootResultType.BLANK, null);
            }

            final Ship ship = fleet.getShip(cellVal);
            final int segment = indexProviders.get(ship.getOrientation()).segment(ship, h, v);
            if (!ship.isHealthy(segment)) return new ShootResult(ShootResultType.DUPE_HIT, ship);
            ship.hit(segment);
            return ship.isSunk() ? new ShootResult(ShootResultType.SUNK, ship) : new ShootResult(ShootResultType.NEW_HIT, ship);
        }

        @Override public String toString() {
            final List<Ship> ships = fleet.getAllShips();
            final StringBuilder sb = new StringBuilder(200 + 200 * ships.size() + (horizSize + 1) * (vertSize + 1));
            final Formatter fmt = new Formatter(sb);
            fmt.format("Size: h%dxv%d, fleet: %s", horizSize, vertSize, ships);
            for (int vertPos = 0; vertPos < vertSize; vertPos++) {
                sb.append('\n');
                for (int horizPos = 0; horizPos < horizSize; horizPos++) {
                    sb.append(grid[gridIndex(horizPos, vertPos)]);
                }
            }
            return sb.toString();
        }

        public int gridIndex(final int h, final int v) {
            return v * horizSize + h;
        }

        private void renderCell(final RenderContext context, int horizPos, int vertPos) {
            final char ch = grid[gridIndex(horizPos, vertPos)];
            if (!isShipCell(ch)) {
                if (ch == HIT_CELL) context.hitCell(horizPos, vertPos);
                else context.unhitCell(horizPos, vertPos);
            }
            else {
                final Ship ship = fleet.getShip(ch);
                assert ship != null : "Missing ship " + ch + " at " + horizPos + ':' + vertPos + " on " + this;
                final int segment = getSegmentIndex(ship, horizPos, vertPos);
                context.shipSegment(horizPos, vertPos, ship, segment);
            }
        }

        public void render(final RenderContext context) {
            for (int vertPos = 0; vertPos < vertSize; vertPos++) {
                for (int horizPos = 0; horizPos < horizSize; horizPos++) {
                    renderCell(context, horizPos, vertPos);
                }
            }
        }
    }

    public static abstract class ConsoleRenderContextBase implements RenderContext {
        private final static String SPACE_BETWEEN_BOARDS = "  ";
        private final static int CONSOLE_BOARDS_GUTTER_SIZE = SPACE_BETWEEN_BOARDS.length();
        protected static char[][] buffer;
        protected static int horSize, vertSize, rightBoardStartIx;
        public static int getRightBoardStartIx() { return rightBoardStartIx; }

        private static void spill() {
            for(int v = 0; v < vertSize; v++) {
                System.out.println();
                for(int h = 0; h < horSize; h++) System.out.print(buffer[v][h]);
            }
        }

        private static void init(final Board board) {
            horSize = board.getHorizSize() * 2 + 4 + CONSOLE_BOARDS_GUTTER_SIZE;
            vertSize = board.getVertSize() + 2;
            rightBoardStartIx = CONSOLE_BOARDS_GUTTER_SIZE + board.getHorizSize() + 3;
            buffer = new char[vertSize][];
            for (int i = 0, bufferLength = buffer.length; i < bufferLength; i++) {
                buffer[i] = new char[horSize];
                fill(buffer[i], ' ');
            }
            // render headers and footers
            for (char h = 0; h < board.getHorizSize(); h++) {
                final char cellIx = (char)('0' + (char)(h % 10));
                final int leftBoardIx = 1 + h;
                final int rightBoardIx = rightBoardStartIx + h ;
                buffer[0][leftBoardIx] = cellIx;
                buffer[0][rightBoardIx] = cellIx;
                buffer[vertSize - 1][leftBoardIx] = cellIx;
                buffer[vertSize - 1][rightBoardIx] = cellIx;
            }
            for(char v = 0; v < board.getVertSize(); v++) {
                final char cellIx = (char)('0' + (char)(v % 10));
                final int topIx = 1 + v;
                buffer[topIx][0] = cellIx;
                buffer[topIx][rightBoardStartIx - CONSOLE_BOARDS_GUTTER_SIZE - 2] = cellIx;
                buffer[topIx][rightBoardStartIx - 1] = cellIx;
                buffer[topIx][horSize - 1] = cellIx;
            }
        }

        protected abstract int getLeftOffset();
        protected int getV(int boardVertIx) { return 1 + boardVertIx;}
        protected int getH(int boardHorizIx) { return getLeftOffset() + boardHorizIx; }
    }

    public class ConsoleEgoRenderContext extends ConsoleRenderContextBase {

        @Override public void shipSegment(int horiz, int vert, Ship ship, int segment) {
            final char ch = ship.getName();
            buffer[getV(vert)][getH(horiz)] = (ship.isHealthy(segment) ? ch : Character.toLowerCase(ch));
        }

        @Override public void hitCell(int horiz, int vert) {
            buffer[getV(vert)][getH(horiz)] = HIT_CELL;
        }

        @Override public void unhitCell(int horiz, int vert) {
            buffer[getV(vert)][getH(horiz)] = UNHIT_CELL;
        }

        @Override protected int getLeftOffset() { return 1; }
    }

    public class ConsoleEnemyRenderContext extends ConsoleRenderContextBase {

        @Override public void shipSegment(int horiz, int vert, Ship ship, int segment) {
            final char ch = ship.getName();
            buffer[getV(vert)][getH(horiz)] = (ship.isHealthy(segment) ? MYSTERY_CELL : Character.toLowerCase(ch));
        }

        @Override public void hitCell(int horiz, int vert) {
            buffer[getV(vert)][getH(horiz)] = HIT_CELL;
        }

        @Override public void unhitCell(int horiz, int vert) {
            buffer[getV(vert)][getH(horiz)] = MYSTERY_CELL;
        }

        @Override protected int getLeftOffset() { return getRightBoardStartIx(); }
    }

    abstract class CommandRunner {

        final public void assertGameOver() {
            for (final Player player: players) {
                if (player.getBoard().isGameOver()) System.out.printf("%n%s: *** GAME OVER ***%n", player);
            }
        }

        final public void ensureBoard() {
            if (!ego.hasBoard() || !enemy.hasBoard()) throw new IllegalStateException("Please allocate the boards");
        }

        public abstract void perform(String commandLine);

        public abstract String getHelp();
    }

    class AllocateCommandRunner extends CommandRunner {
        @Override public void perform(final String commandLine) {
            String[] fields = COMMA_SPLIT.split(commandLine.substring(1));
            if (fields.length != 2) throw new IllegalArgumentException(
                "Allocate command invalid, expected format H,V (like 10,10) received instead: " + commandLine);
            final int horizontal = Integer.valueOf(fields[0]);
            final int vertical = Integer.valueOf(fields[1]);
            if (horizontal < 2 || vertical < 2 || horizontal > 50 || vertical > 50) throw new IllegalArgumentException(
                "Allocate command invalid, nonsensical dimensions of "
                    + horizontal + 'x' + vertical + ", full command: " + commandLine);
            ego.allocate(horizontal, vertical);
            enemy.allocate(horizontal, vertical);
            ConsoleRenderContextBase.init(ego.getBoard());
            gameStatus = GameStatus.INIT;
        }

        @Override public String getHelp() {
            return "Allocate/reallocate boards: aH,V where H is horizontal size and V is vertical size, example: a10,10";
        }
    }

    class HelpCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            System.out.println("\nAvailable commands:");
            for (final CommandRunner cmdRunner : commands.values()) {
                System.out.printf("%n\t%s", cmdRunner.getHelp());
            }
        }

        @Override public String getHelp() {
            return "Show help: h";
        }
    }

    class BoardCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            ensureBoard();
            ego.getBoard().render(egoRenderContext); // either board should render both for console
            enemy.getBoard().render(enemyRenderContext);
            ConsoleRenderContextBase.spill();
            assertGameOver();
        }

        @Override public String getHelp() {
            return "Show current state of the boards: b";
        }
    }

    class FleetCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            ensureBoard();
            final List<Ship> ships = ego.getBoard().getAllShips();
            if (ships.size() < 1) System.out.printf("%nNo ships on the board");
            else for (final Ship ship : ego.getBoard().getAllShips()) {
                System.out.printf("%n\t%s", ship);
            }
            assertGameOver();
        }

        @Override public String getHelp() {
            return "Show all the deployed ships on my board, a.k.a. fleet: f";
        }
    }

    class DeployCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            assertStatusInit(commandLine);
            ensureBoard();
            final Board myBoard = ego.getBoard();
            final char name = Character.toUpperCase(commandLine.charAt(1));
            if (VALID_SHIP_NAMES.indexOf(name) < 0)
                throw new IllegalArgumentException("Illegal name for a ship: " + name);
            if (myBoard.hasShip(name))
                throw new IllegalArgumentException("Ship with the name " + name + " already deployed, see fleet");
            final String[] fields = COMMA_SPLIT.split(commandLine.substring(2));
            if (fields.length != 4) throw new IllegalArgumentException(
                "Deploy command invalid, run for help: " + commandLine);

            final int left = Integer.valueOf(fields[0]);
            if (left < 0 || left >= myBoard.getHorizSize()) throw new IllegalArgumentException(
                "Command line " + commandLine + ": invalid left spec on " + myBoard);

            final int top = Integer.valueOf(fields[1]);
            if (top < 0 || top >= myBoard.getVertSize()) throw new IllegalArgumentException(
                "Command line " + commandLine + ": invalid top spec on " + myBoard);

            final ShipOrientation orientation = ShipOrientation.deserialize(fields[2].charAt(0));

            final int size = Integer.valueOf(fields[3]);
            final Ship ship = new Ship(name, left, top, size, orientation);
            DeployResult result = myBoard.deploy(ship);
            switch (result.getType()) {
                case OCCUPIED:
                    System.out.printf("%nOverlay with the ship %s, deploy aborted", result.getShip());
                    break;
                case SUCCESS:
                    System.out.printf("%nDeployed %s as instructed", result.getShip());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid result type " + result.getType());
            }
        }

        @Override public String getHelp() {
            return "Deploy a ship on a board: dNL,T,O,S where N - name, L-left, T-top, O-orientation (V or H), S-size";
        }
    }

    class UndeployCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            assertStatusInit(commandLine);
            ensureBoard();
            final Board myBoard = ego.getBoard();
            final char name = Character.toUpperCase(commandLine.charAt(1));
            if (VALID_SHIP_NAMES.indexOf(name) < 0)
                throw new IllegalArgumentException("Illegal name for a ship: " + name);
            if (!myBoard.hasShip(name))
                throw new IllegalArgumentException("Ship with the name " + name + " is not deployed, see fleet");
            final Ship ship = myBoard.getShip(name);
            myBoard.undeploy(name);
            System.out.printf("Undeployed %s as instructed.", ship);
            assertGameOver();
        }

        @Override public String getHelp() {
            return "Undeploy a ship: uN, where N is the ship's name";
        }
    }

    class PlayerCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            final int playerId = Integer.valueOf(commandLine.substring(1));
            setPlayerId(playerId);
            System.out.printf("%nSwitched to player %d", playerId);
        }

        @Override public String getHelp() {
            return "Set current player index: pX, like p0 or p1";
        }
    }

    class ShootCommandRunner extends CommandRunner {

        @Override public void perform(String commandLine) {
            ensureBoard();
            final Board enemyBoard = enemy.getBoard();
            String[] fields = COMMA_SPLIT.split(commandLine.substring(1));
            if (fields.length != 2) throw new IllegalArgumentException(
                "Shoot command invalid, expected format HxV (like 10,10) received instead: " + commandLine);
            final int horizontal = Integer.valueOf(fields[0]);
            final int vertical = Integer.valueOf(fields[1]);
            if (horizontal < 0 || vertical < 0 || horizontal >= enemyBoard.getHorizSize() || vertical >= enemyBoard.getVertSize())
                throw new IllegalArgumentException(
                    "Coordinate " + horizontal + 'x' + vertical + ", is invalid; full command: " + commandLine);

            gameStatus = GameStatus.STARTED;
            ShootResult result = enemyBoard.shoot(horizontal, vertical);
            switch (result.getType()) { // todo apply the Visitor pattern instead
                case BLANK:
                    System.out.printf("%nNothing here: " + commandLine);
                    break;
                case DUPE_HIT:
                    System.out.printf("%nEnough beating the dead horse; command: " + commandLine);
                    break;
                case NEW_HIT:
                    System.out.printf("%nNew hit, command: " + commandLine);
                    break;
                case SUNK:
                    System.out.printf("%nSunk: " + result.getShip() + ", command: " + commandLine);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid result type " + result.getType());
            }
            assertGameOver();
        }

        @Override public String getHelp() {
            return "Shoot at enemy's board location: sH,V, where H is horizontal coord, V is vertical; example: s6,9";
        }
    }

    private GameStatus gameStatus = GameStatus.INIT;
    private Player[] players;
    private Player ego;
    private Player enemy;

    private RenderContext egoRenderContext = new ConsoleEgoRenderContext();
    private RenderContext enemyRenderContext = new ConsoleEnemyRenderContext();

    private final EnumMap<Command, CommandRunner> commands = new EnumMap<>(Command.class);

    public Battleship() {
        commands.put(Command.ALLOCATE, new AllocateCommandRunner());
        commands.put(Command.DEPLOY, new DeployCommandRunner());
        commands.put(Command.FLEET, new FleetCommandRunner());
        commands.put(Command.HELP, new HelpCommandRunner());
        commands.put(Command.SHOOT, new ShootCommandRunner());
        commands.put(Command.BOARD, new BoardCommandRunner());
        commands.put(Command.UNDEPLOY, new UndeployCommandRunner());
        commands.put(Command.PLAYER, new PlayerCommandRunner());
        players = new Player[] { new Player(0), new Player(1) };
        setPlayerId(0);
    }

    private void setPlayerId(final int userId) {
        if(userId < 0 || userId > 1) throw new IllegalArgumentException("Invalid user id: " + userId);
        ego = players[userId];
        enemy = players[1 - userId];
    }

    /**
     * Interactive console play.
     */
    public void consolePlay() {
        final Console console = System.console();
        while (true) {
            try {
                System.out.printf("%nPlayer [%d], Command (h for help, q to quit):", ego.id);
                final String line = console.readLine();
                if (line.length() == 0) continue;
                if (line.charAt(0) == 'q') break;
                commands.get(Command.getForInput(line.charAt(0))).perform(line);
            }
            catch (Exception x) {
                System.out.printf("%nERROR: %s", x.getLocalizedMessage());
            }
        }
    }

    private void assertStatusInit(final String commandLine) {
        if(gameStatus != GameStatus.INIT) throw new IllegalStateException(
            "Operation is only allowed before game start, command: " + commandLine);
    }

    /**
     * Scripted play.
     */
    public void scriptPlay(final String fileName) throws Exception {
        try (final BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) continue;
                if (line.charAt(0) == 'q') break;
                commands.get(Command.getForInput(line.charAt(0))).perform(line);
            }
        }
    }

    public static void main(String[] args) {
        final Battleship game = new Battleship();
        boolean scriptPlayCompleted = false;
        if(args.length > 0) try {
            game.scriptPlay(args[0]);
            scriptPlayCompleted = true;
        }
        catch(Exception x) {
            x.printStackTrace();
        }
        if(!scriptPlayCompleted) game.consolePlay();
    }
}

public class Positionv2 implements Cloneable{

    static final int WIDTH = 7;  // width of the board
    static final int HEIGHT = 6; // height of the board
    static final int MIN_SCORE = -(WIDTH*HEIGHT)/2 + 3;
    static final int MAX_SCORE = (WIDTH*HEIGHT+1)/2 - 3;

    private long current_position;
    private long mask;
    private int moves; // number of moves played since the beinning of the game.

    /**
     * Default constructor, build an empty position.
     */
    Positionv2(){
        current_position = 0;
        mask = 0;
        moves = 0;
    }

    @Override
    public Positionv2 clone() {
        try {
            Positionv2 pos = (Positionv2) super.clone();
            return pos;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * Indicates whether a column is playable.
     * @param col: 0-based index of column to play
     * @return true if the column is playable, false if the column is already full.
     */
    boolean canPlay(int col)
    {
        return (mask & top_mask(col)) == 0;
    }

    /**
     * Plays a playable column.
     * This function should not be called on a non-playable column or a column making an alignment.
     *
     * @param col: 0-based index of a playable column.
     */
    void play(int col)
    {
        current_position ^= mask;
        mask |= mask + bottom_mask(col);
        moves++;
    }

    /*
     * Plays a sequence of successive played columns, mainly used to initilize a board.
     * @param seq: a sequence of digits corresponding to the 1-based index of the column played.
     *
     * @return number of played moves. Processing will stop at first invalid move that can be:
     *           - invalid character (non digit, or digit >= WIDTH)
     *           - playing a colum the is already full
     *           - playing a column that makes an aligment (we only solve non).
     *         Caller can check if the move sequence was valid by comparing the number of
     *         processed moves to the length of the sequence.
     */
    long play(String seq)
    {
        for(int i = 0; i < seq.length(); i++) {
            int col = seq.charAt(i) - '1';
            if(col < 0 || col >= Position.WIDTH || !canPlay(col) || isWinningMove(col)) return i; // invalid move
            play(col);
        }
        return seq.length();
    }

    /**
     * Indicates whether the current player wins by playing a given column.
     * This function should never be called on a non-playable column.
     * @param col: 0-based index of a playable column.
     * @return true if current player makes an alignment by playing the corresponding column col.
     */
    boolean isWinningMove(int col)
    {
        long pos = current_position;
        pos |= (mask + bottom_mask(col)) & column_mask(col);
        return alignment(pos);
    }

    /**
     * @return number of moves played from the beginning of the game.
     */
    int nbMoves()
    {
        return moves;
    }

    /**
     * @return a compact representation of a position on WIDTH*(HEIGHT+1) bits.
     */
    long key()
    {
        return current_position + mask;
    }

    /**
     * Test an alignment for current player (identified by one in the bitboard pos)
     * @param pos : bitboard position of a player's cells.
     * @return true if the player has a 4-alignment.
     */
    static boolean alignment(long pos) {
        // horizontal
        long m = pos & (pos >> (HEIGHT+1));
        if((m & (m >> (2*(HEIGHT+1))))!=0) return true;

        // diagonal 1
        m = pos & (pos >> HEIGHT);
        if((m & (m >> (2*HEIGHT)))!=0) return true;

        // diagonal 2
        m = pos & (pos >> (HEIGHT+2));
        if((m & (m >> (2*(HEIGHT+2))))!=0) return true;

        // vertical;
        m = pos & (pos >> 1);
        if((m & (m >> 2))!=0) return true;

        return false;
    }

    // return a bitmask containg a single 1 corresponding to the top cel of a given column
    static long top_mask(int col) {
        return ((long) 1 << (HEIGHT - 1)) << col*(HEIGHT+1);
    }

    // return a bitmask containg a single 1 corresponding to the bottom cell of a given column
    static long bottom_mask(int col) {
        return (long) 1 << col*(HEIGHT+1);
    }

    // return a bitmask 1 on all the cells of a given column
    static long column_mask(int col) {
        return (((long) 1 << HEIGHT)-1) << col*(HEIGHT+1);
    }


}

public class Positionv2 implements Cloneable{

    static final int WIDTH = 7;  // width of the board
    static final int HEIGHT = 6; // height of the board
    static final int MIN_SCORE = -(WIDTH*HEIGHT)/2 + 3;
    static final int MAX_SCORE = (WIDTH*HEIGHT+1)/2 - 3;
    static final long bottom_mask = bottom(WIDTH, HEIGHT);
    static final long board_mask = bottom_mask * ((1L << HEIGHT)-1);

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
     * Plays a possible move given by its bitmap representation
     *
     * @param move: a possible move given by its bitmap representation
     *        only one bit of the bitmap should be set to 1
     *        the move should be a valid possible move for the current player
     */
    void play(long move)
    {
        current_position ^= mask;
        mask |= move;
        moves++;
    }

    /*
     * Plays a sequence of successive played columns, mainly used to initilize a board.
     * @param seq: a sequence of digits corresponding to the 1-based index of the column played.
     *
     * @return number of played moves. Processing will stop at first invalid move that can be:
     *           - invalid character (non digit, or digit >= WIDTH)
     *           - playing a colum the is already full
     *           - playing a column that makes an alignment (we only solve non).
     *         Caller can check if the move sequence was valid by comparing the number of
     *         processed moves to the length of the sequence.
     */
    long play(String seq)
    {
        for(int i = 0; i < seq.length(); i++) {
        int col = seq.charAt(i) - '1';
        if(col < 0 || col >= Positionv2.WIDTH || !canPlay(col) || isWinningMove(col)) return i; // invalid move
        playCol(col);
    }
        return seq.length();
    }

    /*
     * return true if current player can win next move
     */
    boolean canWinNext()
    {
        return (winning_position() & possible())!=0;
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

    /*
     * Return a bitmap of all the possible next moves the do not lose in one turn.
     * A losing move is a move leaving the possibility for the opponent to win directly.
     *
     * Warning this function is intended to test position where you cannot win in one turn
     * If you have a winning move, this function can miss it and prefer to prevent the opponent
     * to make an alignment.
     */
    long possibleNonLoosingMoves() {
        assert(!canWinNext());
        long possible_mask = possible();
        long opponent_win = opponent_winning_position();
        long forced_moves = possible_mask & opponent_win;
        if(forced_moves!=0) {
            if((forced_moves & (forced_moves - 1))!=0) // check if there is more than one forced move
                return 0;                           // the opponnent has two winning moves and you cannot stop him
            else possible_mask = forced_moves;    // enforce to play the single forced move
        }
        return possible_mask & ~(opponent_win >> 1);  // avoid to play below an opponent winning spot
    }

    /**
     * Indicates whether a column is playable.
     * @param col: 0-based index of column to play
     * @return true if the column is playable, false if the column is already full.
     */
    boolean canPlay(int col)
    {
        return (mask & top_mask_col(col)) == 0;
    }


    /**
     * Plays a playable column.
     * This function should not be called on a non-playable column or a column making an alignment.
     *
     * @param col: 0-based index of a playable column.
     */
    void playCol(int col)
    {
        play((mask + bottom_mask_col(col)) & column_mask(col));
    }

    /**
     * Score a possible move.
     *
     * @param move, a possible move given in a bitmap format.
     *
     * The score we are using is the number of winning spots
     * the current player has after playing the move.
     */
    int moveScore(long move) {
        return popcount(compute_winning_position(current_position | move, mask));
    }

    /**
     * Indicates whether the current player wins by playing a given column.
     * This function should never be called on a non-playable column.
     * @param col: 0-based index of a playable column.
     * @return true if current player makes an alignment by playing the corresponding column col.
     */
    boolean isWinningMove(int col)
    {
        return (winning_position() & possible() & column_mask(col))!=0;
    }

    /*
     * Return a bitmask of the possible winning positions for the current player
     */
    long winning_position() {
        return compute_winning_position(current_position, mask);
    }

    /*
     * Return a bitmask of the possible winning positions for the opponent
     */
    long opponent_winning_position() {
        return compute_winning_position(current_position ^ mask, mask);
    }

    /*
     * Bitmap of the next possible valid moves for the current player
     * Including losing moves.
     */
    long possible() {
        return (mask + bottom_mask) & board_mask;
    }

    /*
     * counts number of bit set to one in a 64bits integer
     */
    static int popcount(long m) {
        int c = 0;
        for (c = 0; m!=0; c++) m &= m - 1;
        return c;
    }

    /*
     * @parmam position, a bitmap of the player to evaluate the winning pos
     * @param mask, a mask of the already played spots
     *
     * @return a bitmap of all the winning free spots making an alignment
     */
    static long compute_winning_position(long position, long mask) {
        // vertical;
        long r = (position << 1) & (position << 2) & (position << 3);

        //horizontal
        long p = (position << (HEIGHT+1)) & (position << 2*(HEIGHT+1));
        r |= p & (position << 3*(HEIGHT+1));
        r |= p & (position >> (HEIGHT+1));
        p = (position >> (HEIGHT+1)) & (position >> 2*(HEIGHT+1));
        r |= p & (position << (HEIGHT+1));
        r |= p & (position >> 3*(HEIGHT+1));

        //diagonal 1
        p = (position << HEIGHT) & (position << 2*HEIGHT);
        r |= p & (position << 3*HEIGHT);
        r |= p & (position >> HEIGHT);
        p = (position >> HEIGHT) & (position >> 2*HEIGHT);
        r |= p & (position << HEIGHT);
        r |= p & (position >> 3*HEIGHT);

        //diagonal 2
        p = (position << (HEIGHT+2)) & (position << 2*(HEIGHT+2));
        r |= p & (position << 3*(HEIGHT+2));
        r |= p & (position >> (HEIGHT+2));
        p = (position >> (HEIGHT+2)) & (position >> 2*(HEIGHT+2));
        r |= p & (position << (HEIGHT+2));
        r |= p & (position >> 3*(HEIGHT+2));

        return r & (board_mask ^ mask);
    }

    // return a bitmask containg a single 1 corresponding to the top cel of a given column
    static long top_mask_col(int col) {
        return ((long) 1 << (HEIGHT - 1)) << col*(HEIGHT+1);
    }

    // return a bitmask containg a single 1 corresponding to the bottom cell of a given column
    static long bottom_mask_col(int col) {
        return (long) 1 << col*(HEIGHT+1);
    }

    // return a bitmask 1 on all the cells of a given column
    static long column_mask(int col) {
        return (((long) 1 << HEIGHT)-1) << col*(HEIGHT+1);
    }

    static long bottom(int width, int height) {
        return width == 0 ? 0 : bottom(width-1, height) | 1L << (width-1)*(height+1);
    }
}

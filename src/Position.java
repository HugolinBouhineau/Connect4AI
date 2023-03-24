public class Position implements Cloneable{

    static final int WIDTH = 7;
    static final int HEIGHT = 6;

    private int moves;      // number of moves played since the beginning of the game.
    private int[][] board;  // 0 if cell is empty, 1 for first player and 2 for second player.
    private int[] height;   // number of stones per column

    Position(){
        moves = 0;
        board = new int[WIDTH][HEIGHT];
        height = new int[WIDTH];
    }

    @Override
    public Position clone() {
        try {
            Position pos = (Position) super.clone();
            pos.board = new int[WIDTH][HEIGHT];
            for(int i=0; i<WIDTH; i++){
                pos.board[i] = board[i].clone();
            }
            pos.height = height.clone();
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
        return height[col] < HEIGHT;
    }

    /**
     * Plays a playable column.
     * This function should not be called on a non-playable column or a column making an alignment.
     *
     * @param col: 0-based index of a playable column.
     */
    void play(int col)
    {
        board[col][height[col]] = 1 + moves%2;
        height[col]++;
        moves++;
    }

    /**
     * Indicates whether the current player wins by playing a given column.
     * This function should never be called on a non-playable column.
     * @param col: 0-based index of a playable column.
     * @return true if current player makes an alignment by playing the corresponding column col.
     */
    boolean isWinningMove(int col)
    {
        int current_player = 1 + moves%2;
        // check for vertical alignments
        if(height[col] >= 3
                && board[col][height[col]-1] == current_player
                && board[col][height[col]-2] == current_player
                && board[col][height[col]-3] == current_player)
            return true;

        for(int dy = -1; dy <=1; dy++) {    // Iterate on horizontal (dy = 0) or two diagonal directions (dy = -1 or dy = 1).
            int nb = 0;                       // counter of the number of stones of current player surronding the played stone in tested direction.
            for(int dx = -1; dx <=1; dx += 2) // count continuous stones of current player on the left, then right of the played column.
                for(int x = col+dx, y = height[col]+dx*dy; x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && board[x][y] == current_player; nb++) {
                    x += dx;
                    y += dx*dy;
                }
            if(nb >= 3) return true; // there is an aligment if at least 3 other stones of the current user
            // are surronding the played stone in the tested direction.
        }
        return false;
    }

    /**
     * @return number of moves played from the beginning of the game.
     */
    int nbMoves()
    {
        return moves;
    }

    void print(){
        System.out.println("\nMoves : "+moves);
        System.out.print("Height : ");
        for (int i = 0; i < height.length; i++) {
            System.out.print(height[i]+" ");
        }
        System.out.println("\nPlateau : ");
        for (int row = HEIGHT-1; row >=0 ; row--) {
            for (int col = 0; col < WIDTH; col++) {
                System.out.print(board[col][row]+" ");
            }
            System.out.println();
        }
    }

}

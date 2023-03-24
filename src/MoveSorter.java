public class MoveSorter {

    // number of stored moves
    int size;

    // Contains size moves with their score ordered by score
    private Pair[] entries;

    MoveSorter(){
        size = 0;
        entries = new Pair[Positionv2.WIDTH];
    }

    /*
     * Add a move in the container with its score.
     * You cannot add more than Positionv2.WIDTH moves
     */
    void add(long move, int score)
    {
        int pos = size++;
        for(; pos!=0 && entries[pos-1].score > score; --pos){
            entries[pos] = entries[pos-1];
        }
        entries[pos] = new Pair(move,score);
//        entries[pos].move = move;
//        entries[pos].score = score;
    }

    /*
     * Get next move
     * @return next remaining move with max score and remove it from the container.
     * If no more move is available return 0
     */
    long getNext()
    {
        if(size!=0)
            return entries[--size].move;
        else
            return 0;
    }

    /*
     * reset (empty) the container
     */
    void reset()
    {
        size = 0;
    }

}

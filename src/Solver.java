import java.util.HashMap;
import java.util.Scanner;

public class Solver {

    private long nodeCount; // counter of explored nodes.

    HashMap<Long, Integer> map;

    int[] columnOrder;

    Solver(){
        nodeCount = 0;
        columnOrder = new int[Positionv2.WIDTH];
        map = new HashMap<>();
        // initialize the column exploration order, starting with center columns
        for(int i = 0; i < Positionv2.WIDTH; i++)
            columnOrder[i] = Positionv2.WIDTH/2 + (1-2*(i%2))*(i+1)/2;
    }

    void reset(){
        nodeCount = 0;
        map.clear();
    }

    /*
     * Recursively solve a connect 4 position using negamax variant of min-max algorithm.
     * @return the score of a position:
     *  - 0 for a draw game
     *  - positive score if you can win whatever your opponent is playing. Your score is
     *    the number of moves before the end you can win (the faster you win, the higher your score)
     *  - negative score if your opponent can force you to lose. Your score is the oposite of
     *    the number of moves before the end you will lose (the faster you lose, the lower your score).
     */
    int negamax(Positionv2 P, int alpha, int beta) {
        nodeCount++; // increment counter of explored nodes

        long next = P.possibleNonLoosingMoves();
        if(next == 0)     // if no possible non losing move, opponent wins next move
            return -(Positionv2.WIDTH*Positionv2.HEIGHT - P.nbMoves())/2;

        if(P.nbMoves() >= Positionv2.WIDTH*Positionv2.HEIGHT - 2) // check for draw game
            return 0;

        int min = -(Positionv2.WIDTH*Positionv2.HEIGHT-2 - P.nbMoves())/2;	// lower bound of score as opponent cannot win next move
        if(alpha < min) {
            alpha = min;                     // there is no need to keep beta above our max possible score.
            if(alpha >= beta) return alpha;  // prune the exploration if the [alpha;beta] window is empty.
        }

        int max = (Positionv2.WIDTH*Positionv2.HEIGHT-1 - P.nbMoves())/2;	// upper bound of our score as we cannot win immediately
        if(map.containsKey(P.key()))
            max = map.get(P.key()) + Positionv2.MIN_SCORE -1;
        if(beta > max) {
            beta = max;                     // there is no need to keep beta above our max possible score.
            if(alpha >= beta) return beta;  // prune the exploration if the [alpha;beta] window is empty.
        }

        MoveSorter moves = new MoveSorter();

        for(int i = Positionv2.WIDTH-1; i >=0 ; i--){
            long move = next & Positionv2.column_mask(columnOrder[i]);
            if(move!=0){
                moves.add(move, P.moveScore(move));
            }
        }

        next = moves.getNext();
        while(next!=0) {
            Positionv2 P2 = P.clone();
            P2.play(next);  // It's opponent turn in P2 position after current player plays x column.
            int score = -negamax(P2, -beta, -alpha); // explore opponent's score within [-beta;-alpha] windows:
            // no need to have good precision for score better than beta (opponent's score worse than -beta)
            // no need to check for score worse than alpha (opponent's score worse better than -alpha)

            if(score >= beta) return score;  // prune the exploration if we find a possible move better than what we were looking for.
            if(score > alpha) alpha = score; // reduce the [alpha;beta] window for next exploration, as we only
            // need to search for a position that is better than the best so far.
            next = moves.getNext();
        }

        map.put(P.key(), alpha - Positionv2.MIN_SCORE + 1);
        return alpha;
    }

    int solve(Positionv2 P, boolean weak)
    {
        if(P.canWinNext()) // check if win in one move as the Negamax function does not support this case.
            return (Positionv2.WIDTH*Positionv2.HEIGHT+1 - P.nbMoves())/2;
        int min = -(Positionv2.WIDTH*Positionv2.HEIGHT - P.nbMoves())/2;
        int max = (Positionv2.WIDTH*Positionv2.HEIGHT+1 - P.nbMoves())/2;
        if(weak) {
            min = -1;
            max = 1;
        }

        while(min < max) {  // iteratively narrow the min-max exploration window
            int med = min + (max - min)/2;
            if(med <= 0 && min/2 < med) med = min/2;
            else if(med >= 0 && max/2 > med) med = max/2;
            int r = negamax(P, med, med + 1);   // use a null depth window to know if the actual score is greater or smaller than med
            if(r <= med) max = r;
            else min = r;
        }
        return min;
    }

    long getNodeCount()
    {
        return nodeCount;
    }

    public void test () {

        int nbline = 0;
        long meantime = 0;

        Scanner scanner = new Scanner(this.getClass().getResourceAsStream("Test_L1_R2"));
        while (scanner.hasNextLine()) {
            nbline++;
            String line = scanner.nextLine().split(" ")[0];
            Positionv2 P = new Positionv2();
            if (P.play(line) != line.length()) {
                System.err.println("Line " + line + ": Invalid move " + (P.nbMoves() + 1) + " \"" + line + "\"");
            } else {
                reset();
                long start_time = System.currentTimeMillis();
                int score = solve(P, true);
                long end_time = System.currentTimeMillis();
                meantime += end_time - start_time;
                System.out.println(nbline + ": " + line + " " + score + " " + getNodeCount() + " " + (end_time - start_time));
            }
            System.out.println();
        }
        System.out.println("Mean time : "+ (meantime/nbline)/1000.0+"s");
        scanner.close();
    }

}

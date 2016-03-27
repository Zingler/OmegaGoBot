package bot.strategies;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.val;
import move.Move;
import util.MaxSizePriorityQueue;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

public class LibertySizeRatioAttackCandidateStrategy implements AttackGroupCandidateStrategy {
    double[][] scores = null;

    static double minValueRequired = .8;

    @Override
    public List<Group> getAttackCandidates( Field f ) {
        this.scores = groupAttackScore( f );

        val queue = new MaxSizePriorityQueue<Group>( 10, false );

        f.getGroups().getUniqueGroups().forEach( g -> {
            Move m = g.getAnyMove();
            double score = this.scores[m.getRow()][m.getCol()];
            if ( score > minValueRequired ) {
                queue.put( g, score );
            }
        } );

        return queue.getAllElements();
    }

    private double[][] groupAttackScore( Field f ) {
        val map = f.getGroups().getUniqueGroups().stream()
                .collect( Collectors.toMap( Function.identity(), this::groupAttackability ) );
        double[][] result = new double[f.getRows()][f.getColumns()];
        map.forEach( ( g, score ) -> g.getMoves().forEach( m -> {
            result[m.getRow()][m.getCol()] = f.getMyId() != g.getPlayerId() ? score : -score;
        } ) );
        return result;
    }

    private double groupAttackability( Group g ) {
        int peices = g.getMoves().size();
        int liberties = g.getLibertyCount();
        if ( liberties == 1 ) {
            return Double.MAX_VALUE;
        } else if ( peices > 15 && liberties > 4 ) {
            return 0;
        } else {
            return (double) peices / liberties;
        }
    }

    @Override
    public void newTurn() {
        this.scores = null;
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
        move.attackScores( this.scores );
    }
}

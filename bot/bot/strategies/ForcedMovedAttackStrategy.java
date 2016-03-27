package bot.strategies;

import java.util.ArrayList;
import java.util.List;

import move.Move;
import util.MaxSizePriorityQueue.Score;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

public class ForcedMovedAttackStrategy implements AttackingMoveStrategy {

    ForcedMoveEvaluator evaluator = new ForcedMoveEvaluator();

    List<Score<Move>> scores;

    @Override
    public void newTurn() {
        this.scores = null;
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
        // TODO add metrics
    }

    @Override
    public List<Move> getAttackingMoves( Field field, List<Group> attackableGroups ) {
        List<Move> moves = new ArrayList<>();
        for ( Group g : attackableGroups ) {
            if ( g.getLibertyCount() == 2 ) {
                for ( Move m : g.getLiberties() ) {
                    if ( this.evaluator.evaluateAttack( m, g.getAnyMove(), field ).isForcedCapture() ) {
                        moves.add( m );
                    }
                }
            }
        }
        return moves;
    }
}

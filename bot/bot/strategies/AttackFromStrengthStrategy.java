package bot.strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import move.Move;
import util.MaxSizePriorityQueue.Score;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

public class AttackFromStrengthStrategy implements AttackingMoveStrategy {

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
        List<Score<Move>> moves = new ArrayList<>();
        for ( Group g : attackableGroups ) {
            for ( Move m : g.getLiberties() ) {
                Field newField = field.simulateMyMove( m );
                int myLiberties = newField.getGroupAt( m.getRow(), m.getCol() ).map( Group::getLibertyCount ).orElse( 0 );
                double groupSize = newField.getLaplaceGroupSize( m, .3 );
                moves.add( new Score<Move>( m, -( myLiberties + ( groupSize / 3 ) ) ) );
            }
        }
        Collections.sort( moves );
        return moves.stream().map( Score::getElement ).collect( Collectors.toList() );
    }
}

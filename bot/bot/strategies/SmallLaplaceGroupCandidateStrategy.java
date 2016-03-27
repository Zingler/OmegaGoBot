package bot.strategies;

import java.util.List;
import java.util.stream.Collectors;

import util.MaxSizePriorityQueue.Score;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

public class SmallLaplaceGroupCandidateStrategy implements AttackGroupCandidateStrategy {

    @Override
    public List<Group> getAttackCandidates( Field f ) {
        List<Group> groups = f.getGroups().getUniqueGroups().stream()
                .filter( g -> g.getPlayerId() == f.getOpponentId() )
                .map( g -> new Score<Group>( g, ( f.getLaplaceGroupSize( g.getAnyMove(), .2 ) - g.getMoves().size() ) / g.getLibertyCount() ) )
                .filter( s -> s.getScore() < 2 )
                .sorted()
                .map( s -> s.getElement() )
                .collect( Collectors.toList() );

        return groups;
    }

    @Override
    public void newTurn() {
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
    }
}

package bot.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import move.Move;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

public class DoubleAtariCandidateStrategy implements AttackGroupCandidateStrategy {

    @Override
    public List<Group> getAttackCandidates( Field f ) {
        Map<Move, List<Group>> libertiesToGroupList = new HashMap<>();

        f.getGroups().getUniqueGroups()
                .stream()
                .filter( g -> g.getPlayerId() == f.getOpponentId() )
                .filter( g -> g.getLibertyCount() == 2 )
                .forEach( g -> {
                    for ( Move liberty : g.getLiberties() ) {
                        List<Group> groups = libertiesToGroupList.computeIfAbsent( liberty, m -> new ArrayList<>() );
                        groups.add( g );
                    }
                } );

        return libertiesToGroupList.entrySet().stream()
                .filter( e -> e.getValue().size() == 2 )
                .flatMap( e -> e.getValue().stream() )
                .collect( Collectors.toList() );
    }

    @Override
    public void newTurn() {
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
    }
}

package bot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import move.Move;
import bot.strategies.AttackingMoveStrategy;
import field.Field;
import field.Group;

public class DoubleAtariAttackStrategy implements AttackingMoveStrategy {

    @Override
    public List<Move> getAttackingMoves( Field field, List<Group> attackableGroups ) {
        Map<Move, List<Group>> libertiesToGroupList = new HashMap<>();

        attackableGroups
                .stream()
                .filter( g -> g.getLibertyCount() == 2 )
                .forEach( g -> {
                    for ( Move liberty : g.getLiberties() ) {
                        List<Group> groups = libertiesToGroupList.computeIfAbsent( liberty, m -> new ArrayList<>() );
                        groups.add( g );
                    }
                } );

        return libertiesToGroupList.entrySet().stream()
                .filter( e -> e.getValue().size() >= 2 )
                .map( Entry::getKey )
                .collect( Collectors.toList() );
    }

    @Override
    public void newTurn() {
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
    }

}

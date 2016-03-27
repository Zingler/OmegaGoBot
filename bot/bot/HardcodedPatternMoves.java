package bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import move.Move;
import field.Field;
import field.Group;

public class HardcodedPatternMoves {
    public List<Move> getMoves( GameState state, Field f ) {

        List<Move> moves = new ArrayList<>();

        Function<Group, Stream<Group>> groupToOpponentGroups = g ->
                g.getOpponentMoves().stream().map( opponent -> f.getGroupAt( opponent.getRow(), opponent.getCol() ).get() );

        if ( state.getRoundNumber() < 100 ) {

            Set<Move> attackedWeakStoneMoves = f
                    .getGroups()
                    .getUniqueGroups()
                    .stream()
                    .filter( g -> g.getPlayerId() == f.getMyId() )
                    .filter( g -> g.getMoves().size() == 1 )
                    .filter( g -> g.getLibertyCount() == 3 )
                    .filter( g -> f.getLaplaceGroupSize( g.getAnyMove(), .2 ) > 8 )
                    .flatMap( groupToOpponentGroups )
                    .filter( g -> g.getMoves().size() == 1 )
                    .flatMap( ( Group g ) -> g.getLiberties().stream() )
                    .collect( Collectors.toSet() );

            attackedWeakStoneMoves.stream()
                    .sorted( Comparator.comparing( ( Move m ) -> f.getLaplace( m ) ).reversed() ).forEach( moves::add );

            Set<Move> extendFromWeakStone = f.getGroups().getUniqueGroups().stream()
                    .filter( g -> g.getPlayerId() == f.getMyId() )
                    .filter( g -> g.getMoves().size() == 1 )
                    .filter( g -> g.getLibertyCount() == 2 )
                    .filter( g -> f.getLaplaceGroupSize( g.getAnyMove(), .001 ) > 8 )
                    .flatMap( g -> g.getLiberties().stream() ).collect( Collectors.toSet() );

            extendFromWeakStone.stream().sorted( Comparator.comparing( ( Move m ) -> f.getLaplace( m ) ).reversed() ).forEach( moves::add );
        }

        return moves;
    }
}

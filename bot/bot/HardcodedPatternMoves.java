package bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import move.Move;
import util.MaxSizePriorityQueue.Score;
import field.Field;
import field.Group;

public class HardcodedPatternMoves {
    public List<Move> getMoves( GameState state, Field f ) {

        List<Move> moves = new ArrayList<>();

        Function<Group, Stream<Group>> groupToOpponentGroups = g ->
                g.getOpponentMoves().stream().map( opponent -> f.getGroupAt( opponent.getRow(), opponent.getCol() ).get() );

        if ( state.getRoundNumber() >= 247 ) {
            Optional<Group> largestGroup = f.getGroups().getUniqueGroups().stream()
                    .filter( g -> g.getPlayerId() == f.getOpponentId() )
                    .sorted( Comparator.comparing( ( Group g ) -> f.getLaplaceGroup( g.getAnyMove(), .95 ).getMoveCount() ).reversed() ).findFirst();

            if ( largestGroup.isPresent() ) {
                Group laplace = f.getLaplaceGroup( largestGroup.get().getAnyMove(), .95 );
                List<Move> openSpaces = laplace.getMoves().stream()
                        .filter( m -> f.getPlayerAt( m.getRow(), m.getCol() ) == 0 )
                        .collect( Collectors.toList() );

                List<Move> movesWithMostLiberties = openSpaces.stream()
                        .map( ( Move m ) -> new Score<>( m, numberFreeSpotsAdjacent( m, f ) ) )
                        .filter( x -> x.getScore() >= 3 )
                        .sorted( Comparator.reverseOrder() )
                        .map( Score<Move>::getElement )
                        .collect( Collectors.toList() );

                moves.addAll( movesWithMostLiberties );
            }
        }

        if ( state.getRoundNumber() < 100 ) {

            Set<Move> attackedWeakStoneMoves = f
                    .getGroups()
                    .getUniqueGroups()
                    .stream()
                    .filter( g -> g.getPlayerId() == f.getMyId() )
                    .filter( g -> g.getMoves().size() == 1 )
                    .filter( g -> g.getLibertyCount() == 3 )
                    .filter( g -> f.getLaplaceGroup( g.getAnyMove(), .2 ).getMoveCount() > 8 )
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
                    .filter( g -> f.getLaplaceGroup( g.getAnyMove(), .001 ).getMoveCount() > 8 )
                    .flatMap( g -> g.getLiberties().stream() ).collect( Collectors.toSet() );

            extendFromWeakStone.stream().sorted( Comparator.comparing( ( Move m ) -> f.getLaplace( m ) ).reversed() ).forEach( moves::add );
        }

        return moves;
    }

    private double numberFreeSpotsAdjacent( Move move, Field field ) {
        return field.validAdjacentPoints( move ).stream()
                .mapToInt( m -> field.getPlayerAt( m.getRow(), m.getCol() ) == 0 ? 1 : 0 )
                .sum();
    }
}

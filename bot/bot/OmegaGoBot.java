// // Copyright 2016 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import move.Move;
import bot.strategies.AttackFromStrengthStrategy;
import bot.strategies.AttackGroupCandidateStrategy;
import bot.strategies.AttackingMoveStrategy;
import bot.strategies.ForcedMovedAttackStrategy;
import bot.strategies.LibertySizeRatioAttackCandidateStrategy;
import bot.strategies.SequentialAttackGroupCandidateStrategy;
import bot.strategies.SequentialAttackingMoveStrategy;
import bot.strategies.SmallLaplaceGroupCandidateStrategy;
import field.Field;
import field.Group;

/**
 * BotStarter class
 *
 * Magic happens here. You should edit this file, or more specifically the
 * makeTurn() method to make your bot do more than random moves.
 *
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class OmegaGoBot implements Bot {

    AttackGroupCandidateStrategy attackCandidateStrategy = SequentialAttackGroupCandidateStrategy.builder()
            .strat( new SmallLaplaceGroupCandidateStrategy() )
            .strat( new LibertySizeRatioAttackCandidateStrategy() ).build();
    private AttackingMoveStrategy attackStrategy = SequentialAttackingMoveStrategy.builder()
            .strat( new ForcedMovedAttackStrategy() )
            .strat( new AttackFromStrengthStrategy() ).build();

    OneLibertyLogic oneLiberyLogic = new OneLibertyLogic();
    HardcodedPatternMoves hardcodedPatternMoves = new HardcodedPatternMoves();

    List<DiagnosticRecorder> recorders = new ArrayList<>();
    {
        this.recorders.add( this.attackCandidateStrategy );
        this.recorders.add( new MoveTimer() );
        this.recorders.add( this.attackStrategy );
    }

    Random r = new Random();

    /**
     * Makes a turn. Edit this method to make your bot smarter. Currently
     * performs random, but legal moves.
     *
     * @return a Move object
     */
    @Override
    public Move getMove( GameState state, int timeout ) {
        return getMoveWithDiagnostics( state, timeout ).move();
    }

    @Override
    public MoveWithDiagnostics getMoveWithDiagnostics( GameState state, int timeout ) {
        this.recorders.forEach( DiagnosticRecorder::newTurn );
        MoveWithDiagnostics move = doLogic( state, timeout );
        this.recorders.forEach( r -> r.record( move ) );

        return move;
    }

    public MoveWithDiagnostics doLogic( GameState state, int timeout ) {
        state.setTimebank( timeout );

        MoveWithDiagnostics finalMove = new MoveWithDiagnostics();

        Field f = state.getField();

        double[][] heatMap = influenceHeatMap( f );
        finalMove.influenceHeatMap( heatMap );
        double[][] laplace = f.getLaplace();
        finalMove.laplace( laplace );

        List<Move> availableMoves = f.getAvailableMoves();
        Set<Move> notHoribleMoves = new HashSet<>();

        // Filters out moves that could immediately be captured. TODO support
        // snap back attacks if we start doing search.
        for ( Move m : availableMoves ) {
            Optional<Group> g = f.simulateMyMove( m ).getGroupAt( m.getRow(), m.getCol() );
            if ( g.isPresent() && g.get().getLibertyCount() > 1 ) {
                notHoribleMoves.add( m );
            }
        }

        List<Move> oneLibertyMoves = this.oneLiberyLogic.getOneLibertyMoves( f );
        List<Move> safeOneLibertyMoves = oneLibertyMoves.stream().filter( notHoribleMoves::contains ).collect( Collectors.toList() );

        if ( !safeOneLibertyMoves.isEmpty() ) {
            List<Move> moves = safeOneLibertyMoves.stream().limit( 5 ).collect( Collectors.toList() );
            setIfNonEmpty( finalMove, moves );
            finalMove.type( "Save or capture largest" );
            return finalMove;
        }

        List<Move> patternMoves = this.hardcodedPatternMoves.getMoves( state, f ).stream()
                .filter( notHoribleMoves::contains ).collect( Collectors.toList() );
        if ( !patternMoves.isEmpty() ) {
            setIfNonEmpty( finalMove, patternMoves );
            return finalMove.type( "Hardcoded pattern moves" );
        }

        if ( state.getRoundNumber() < 2 ) {
            List<Move> best = minBy( notHoribleMoves, ( Move m ) -> edgeCenterHeuristic( m, f ), 5 );
            setIfNonEmpty( finalMove, best );
            return finalMove.type( "Opening Corners" );
        } else if ( state.getRoundNumber() < 0 ) {
            List<Move> best = minBy( notHoribleMoves, ( Move m ) -> centerHeuristic( m, f ), 5 );
            setIfNonEmpty( finalMove, best );
            return finalMove.type( "Opening Circle" );
        } else {
            Predicate<Move> notHoribleFilter = notHoribleMoves::contains;

            List<Group> attackableGroups = this.attackCandidateStrategy.getAttackCandidates( f );
            List<Move> attackingMoves = this.attackStrategy.getAttackingMoves( f, attackableGroups );

            attackingMoves = attackingMoves.stream().filter( notHoribleFilter ).collect( Collectors.toList() );
            if ( !attackingMoves.isEmpty() ) {
                setIfNonEmpty( finalMove, attackingMoves );
                finalMove.type( "Attack" );
                return finalMove;
            }

            Set<Move> influenceMoves;

            if ( state.getRoundNumber() < 70 ) {
                final int rmod = f.getRows() - 1;
                final int cmod = f.getColumns() - 1;
                Predicate<Move> notFirstLine = m -> m.getRow() % rmod != 0 && m.getCol() % cmod != 0;
                influenceMoves = notHoribleMoves.stream().filter( notFirstLine ).collect( Collectors.toSet() );
            } else {
                influenceMoves = notHoribleMoves;
            }
            if ( state.getRoundNumber() % 4 != 0 ) {
                List<Move> best = minBy( influenceMoves, ( Move m ) -> {
                    double heat = laplace[m.getRow()][m.getCol()];
                    heat += ( this.r.nextDouble() * .05 - .025 );
                    return Math.abs( heat );
                }, 5 );
                setIfNonEmpty( finalMove, best );
                finalMove.type( "Laplace" );
                return finalMove;
            } else {
                List<Move> best = minBy( influenceMoves, ( Move m ) -> {
                    double heat = heatMap[m.getRow()][m.getCol()];
                    heat += ( this.r.nextDouble() * .05 );
                    return Math.abs( heat );
                }, 5 );
                setIfNonEmpty( finalMove, best );
                finalMove.type( "Exponential Weighing" );
                return finalMove;
            }
        }

        // Set<Move> availableRandomMoves = notHoribleMoves;
        //
        // int moveCount = availableRandomMoves.size();
        // if ( moveCount == 0 ) {
        // return null;
        // }
        // return finalMove.move( availableRandomMoves.get( this.r.nextInt(
        // moveCount ) ) ).type( "Random" );
    }

    private void setIfNonEmpty( MoveWithDiagnostics finalMove, List<Move> bestMoves ) {
        if ( !bestMoves.isEmpty() ) {
            finalMove.move( bestMoves.get( 0 ) ).otherTopMoves( rest( bestMoves ) );
        }
    }

    private List<Move> rest( List<Move> best ) {
        if ( !best.isEmpty() ) {
            return best.subList( 1, best.size() );
        } else {
            return best;
        }
    }

    private <T, R extends Comparable<R>> List<T> minBy( Collection<T> collection, Function<T, R> func, int max ) {
        Map<T, R> map = collection.stream().collect( Collectors.toMap( x -> x, func ) );

        return map.entrySet().stream().sorted( Comparator.comparing( Entry::getValue ) ).map( e -> e.getKey() ).limit( max ).collect( Collectors
                .toList() );
    }

    private Double centerHeuristic( Move m, Field f ) {
        double distanceFromCenter = distanceFromCenter( m, f );
        double centerError = Math.abs( 1.41 * ( f.getRows() - ( 4 * 2 ) ) /
                                       2.0 - distanceFromCenter );
        double value = centerError + this.r.nextDouble();
        return value;
    }

    private double[][] influenceHeatMap( Field f ) {
        double[][] influence = new double[f.getRows()][f.getColumns()];
        for ( int row = 0; row < f.getRows(); row++ ) {
            for ( int col = 0; col < f.getColumns(); col++ ) {
                int playerAt = f.getPlayerAt( row, col );
                if ( playerAt != 0 ) {
                    double playerFactor = playerAt == f.getMyId() ? 1 : -1;
                    for ( int i = 0; i < influence.length; i++ ) {
                        for ( int j = 0; j < influence[i].length; j++ ) {
                            double distance = Math.sqrt( Math.pow( row - i, 2 ) + Math.pow( col - j, 2 ) );
                            double value = Math.pow( 2, -distance + 1 );
                            influence[i][j] += playerFactor * value;
                        }
                    }
                }
            }
        }
        return influence;
    }

    private double edgeCenterHeuristic( Move m, Field f ) {
        // return m.getCol() + m.getRow();
        double distanceFromCorner = distanceFromCorner( m, f );
        double distanceFromCenter = distanceFromCenter( m, f );
        double edgeError = Math.abs( 1.41 * 4 - distanceFromCorner );
        double centerError = Math.abs( 1.41 * ( f.getRows() - ( 4 * 2 ) ) /
                                       2.0 - distanceFromCenter );
        double value = edgeError + centerError / 2;
        value = value + this.r.nextDouble() * 0.6;
        // System.out.println( m + " value = " + value );
        return value;
    }

    private double distanceFromCorner( Move m, Field f ) {
        double rd = Math.min( m.getRow(), f.getRows() - m.getRow() );
        double cd = Math.min( m.getCol(), f.getColumns() - m.getCol() );
        return Math.sqrt( rd * rd + cd * cd );
    }

    private double distanceFromCenter( Move m, Field f ) {
        double rd = Math.abs( f.getRows() / 2 - m.getRow() );
        double cd = Math.abs( f.getColumns() / 2 - m.getCol() );
        return Math.sqrt( rd * rd + cd * cd );
    }

}

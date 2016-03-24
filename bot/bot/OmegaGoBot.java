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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import move.Move;
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

    Random r = new Random();

    /**
     * Makes a turn. Edit this method to make your bot smarter. Currently
     * performs random, but legal moves.
     *
     * @return a Move object
     */
    @Override
    public Move getMove( GameState state, int timeout ) {
        return getMoveWithDiagnostics( state, timeout ).getMove();
    }

    @Override
    public MovesWithDiagnostics getMoveWithDiagnostics( GameState state, int timeout ) {
        state.setTimebank( timeout );

        MovesWithDiagnostics finalMove = new MovesWithDiagnostics();

        Field f = state.getField();

        double[][] heatMap = influenceHeatMap( f );
        finalMove.setInfluenceHeatMap( heatMap );
        double[][] laplace = laplace( f );
        finalMove.setLaplace( laplace );

        List<Move> availableMoves = f.getAvailableMoves();
        List<Move> notHoribleMoves = new ArrayList<>();

        // Filters out moves that could immediately be captured. TODO support
        // snap back attacks if we start doing search.
        for ( Move m : availableMoves ) {
            Optional<Group> g = f.simulateMyMove( m ).getGroupAt( m.getRow(), m.getCol() );
            if ( g.isPresent() && g.get().getLibertyCount() > 1 ) {
                notHoribleMoves.add( m );
            }
        }

        Set<Move> oneLibertySpots = new HashSet<>();
        for ( int i = 0; i < f.getRows(); i++ ) {
            for ( int j = 0; j < f.getColumns(); j++ ) {
                Optional<Group> g = f.getGroupAt( i, j );
                if ( g.isPresent() && g.get().getLibertyCount() == 1 ) {
                    oneLibertySpots.addAll( g.get().getLiberties() );
                }
            }
        }

        List<Move> capturesOrSaves = notHoribleMoves.stream().filter( oneLibertySpots::contains ).collect( Collectors.toList() );
        if ( !capturesOrSaves.isEmpty() ) {
            Collections.shuffle( capturesOrSaves );
            List<Move> moves = capturesOrSaves.stream().limit( 5 ).collect( Collectors.toList() );
            return finalMove.setMove( moves.get( 0 ) ).setOtherTopMoves( moves.subList( 1, moves.size() ) );
        }

        if ( state.getRoundNumber() < 3 ) {
            List<Move> best = minBy( notHoribleMoves, ( Move m ) -> edgeCenterHeuristic( m, f ), 5 );
            setIfNonEmpty( finalMove, best );
            return finalMove;
        } else if ( state.getRoundNumber() < 8 ) {
            List<Move> best = minBy( notHoribleMoves, ( Move m ) -> centerHeuristic( m, f ), 5 );
            setIfNonEmpty( finalMove, best );
            return finalMove;
        } else if ( state.getRoundNumber() < 250 ) {
            List<Move> best = minBy( notHoribleMoves, ( Move m ) -> {
                double heat = laplace[m.getRow()][m.getCol()];
                heat += ( this.r.nextDouble() * .05 - .025 );
                return Math.abs( heat );
            }, 5 );
            setIfNonEmpty( finalMove, best );
            return finalMove;
        }

        List<Move> availableRandomMoves = notHoribleMoves;

        int moveCount = availableRandomMoves.size();
        if ( moveCount == 0 ) {
            return null;
        }
        return finalMove.setMove( availableRandomMoves.get( this.r.nextInt( moveCount ) ) );
    }

    private void setIfNonEmpty( MovesWithDiagnostics finalMove, List<Move> bestMoves ) {
        if ( !bestMoves.isEmpty() ) {
            finalMove.setMove( bestMoves.get( 0 ) ).setOtherTopMoves( rest( bestMoves ) );
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

    private double[][] laplace( Field f ) {
        double[][] laplace = new double[f.getRows()][f.getColumns()];
        for ( int i = 0; i < f.getRows() * 2; i++ ) {
            for ( int row = 0; row < f.getRows(); row++ ) {
                for ( int col = 0; col < f.getColumns(); col++ ) {
                    int player = f.getPlayerAt( row, col );
                    if ( player == f.getMyId() ) {
                        laplace[row][col] = 1;
                    } else if ( player != 0 ) {
                        laplace[row][col] = -1;
                    } else {
                        List<Move> adjacent = f.validAdjacentPoints( row, col );
                        laplace[row][col] = adjacent.stream().mapToDouble( m -> laplace[m.getRow()][m.getCol()] ).average().getAsDouble();
                    }
                }
            }
        }
        return laplace;
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

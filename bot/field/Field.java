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

package field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import move.Move;

/**
 * Field class
 *
 * Handles everything that has to do with the field, such as storing the current
 * state and performing calculations on the field.
 *
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {
    private int myId;
    private int opponentId;
    private int rows;
    private int cols;
    private Field lastField;

    private int[][] field;

    public Field() {
    }

    /**
     * Initializes field
     *
     * @throws Exception
     */
    public Field initField() {
        this.field = new int[this.rows][this.cols];
        clearField();
        return this;
    }

    /**
     * Parse field from comma separated String
     *
     * @param String
     *            : input from engine
     */
    public Field updateFieldFromString( String s ) {
        s = s.replace( ";", "," );
        String[] split = s.split( "," );
        int counter = 0;
        if ( this.field == null ) {
            initField();
        }
        Field newField = this.copy();

        for ( int r = 0; r < this.rows; r++ ) {
            for ( int c = 0; c < this.cols; c++ ) {
                newField.field[r][c] = Integer.parseInt( split[counter] );
                counter++;
            }
        }

        newField.lastField = this;
        return newField;
    }

    /**
     * Sets the whole field to empty
     */
    public void clearField() {
        for ( int r = 0; r < this.rows; r++ ) {
            for ( int c = 0; c < this.cols; c++ ) {
                this.field[r][c] = 0;
            }
        }
    }

    /**
     * Returns a list of all available moves. i.e. empty cells that will not
     * result in a suicide move. This does *not* take the Ko rule in to account.
     * TODO: implement Ko rule and remove moves from available moves list that
     * violate this rule
     *
     * @return : a list of all available moves in this game state
     */
    public ArrayList<Move> getAvailableMoves() {
        ArrayList<Move> moves = new ArrayList<Move>();

        for ( int r = 0; r < this.rows; r++ ) {
            for ( int c = 0; c < this.cols; c++ ) {
                if ( isEmptyPoint( r, c ) && !isSuicideMove( r, c ) && !isKoMove( r, c ) ) {
                    moves.add( new Move( r, c ) );
                }
            }
        }

        return moves;
    }

    // TODO, this only checks if we match the immediately last board position
    // (not multiple Kos)
    private boolean isKoMove( int row, int col ) {
        Field f = simulateMyMove( new Move( row, col ) );
        if ( f.isSameBoard( this.lastField ) ) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isSameBoard( Field otherField ) {
        if ( otherField == null || otherField.field == null ) {
            return false;
        }
        for ( int i = 0; i < this.field.length; i++ ) {
            for ( int j = 0; j < this.field[i].length; j++ ) {
                if ( this.field[i][j] != otherField.field[i][j] ) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEmptyPoint( int row, int col ) {
        return this.field[row][col] == 0;
    }

    /**
     * Checks if move if a suicide move
     *
     * @param row
     *            : point x
     * @param col
     *            : point y
     * @return : True if move is a suicide move, false otherwise
     */
    public boolean isSuicideMove( int row, int col ) {

        Field f = simulateMyMove( new Move( row, col ) );
        // TODO this doesn't check if I captured something
        Optional<Group> g = f.getGroupAt( row, col );

        return !g.isPresent();
    }

    public Optional<Group> getGroupAt( int row, int col ) {
        int playerId = this.field[row][col];
        if ( playerId == 0 ) {
            return Optional.empty();
        }
        Group g = getGroupRec( new int[this.rows][this.cols], playerId, row, col );
        g.setPlayerId( playerId );
        return Optional.of( g );
    }

    private static class CountingState {
        public static final int NONE = 0;
        public static final int PART_OF_GROUP = 1;
        public static final int LIBERTY = 2;
    }

    private Group getGroupRec( final int[][] visitState, int playerId, int row, int col ) {
        if ( visitState[row][col] != CountingState.NONE ) {
            return new Group( Collections.EMPTY_LIST, Collections.EMPTY_LIST );
        }
        visitState[row][col] = CountingState.PART_OF_GROUP;

        List<Move> liberties = new ArrayList<>();
        List<Move> nextMoves = new ArrayList<>();
        for ( Move m : validAdjacentPoints( row, col ) ) {
            if ( visitState[m.getRow()][m.getCol()] != CountingState.NONE ) {
                continue;
            }

            int playerAtM = this.field[m.getRow()][m.getCol()];
            if ( playerAtM == 0 ) {
                liberties.add( m );
                visitState[m.getRow()][m.getCol()] = CountingState.LIBERTY;
            } else if ( playerAtM == playerId ) {
                nextMoves.add( m );
            }
        }
        Group g = new Group( new Move( row, col ), liberties );
        Group result = nextMoves.stream().map( move -> getGroupRec( visitState, playerId, move.getRow(), move.getCol() ) ).reduce( g, Group::merge );
        return result;
    }

    public Field simulateMyMove( Move move ) {
        Field newField = this.copy();
        newField.field[move.getRow()][move.getCol()] = this.myId;
        List<Move> validPoints = this.validAdjacentPoints( move.getRow(), move.getCol() );
        List<Group> surroundingGroups = validPoints.stream().map( m -> newField.getGroupAt( m.getRow(), m.getCol() ) ).filter( Optional::isPresent )
                .map( Optional::get ).collect( Collectors.toList() );
        for ( Group g : surroundingGroups ) {
            if ( g.getLibertyCount() == 0 ) {
                for ( Move m : g.getMoves() ) {
                    newField.field[m.getRow()][m.getCol()] = 0;
                }
            }
        }
        newField.setLastField( this );
        return newField;
    }

    public List<Move> validAdjacentPoints( int row, int col ) {
        final int[][] offsets = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        ArrayList<Move> moves = new ArrayList<>();
        for ( int[] offset : offsets ) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            if ( newRow >= 0 && newRow < this.cols && newCol >= 0 && newCol < this.rows ) {
                moves.add( new Move( newRow, newCol ) );
            }
        }
        return moves;
    }

    public Field copy() {
        return new Field().setMyId( this.myId ).setOpponentId( this.opponentId ).setRows( this.rows ).setColumns( this.cols ).setField( this.field );
    }

    @Override
    /**
     * Creates comma separated String that represents the field
     * @param args :
     * @return : Comma separated string of player Id's or 0 for empty.
     */
    public String toString() {
        String s = "";
        int counter = 0;
        for ( int y = 0; y < this.rows; y++ ) {
            for ( int x = 0; x < this.cols; x++ ) {
                if ( counter > 0 ) {
                    s += ",";
                }
                s += this.field[x][y];
                counter++;
            }
        }
        return s;
    }

    public Field setColumns( int value ) {
        this.cols = value;
        return this;
    }

    public int getColumns() {
        return this.cols;
    }

    public Field setRows( int value ) {
        this.rows = value;
        return this;
    }

    public int getRows() {
        return this.rows;
    }

    public Field setMyId( int id ) {
        this.myId = id;
        return this;
    }

    public Field setOpponentId( int id ) {
        this.opponentId = id;
        return this;
    }

    public Field setField( int[][] field ) {
        this.field = new int[field.length][field[0].length];
        for ( int i = 0; i < field.length; i++ ) {
            for ( int j = 0; j < field.length; j++ ) {
                this.field[i][j] = field[i][j];
            }
        }
        return this;
    }

    public void setLastField( Field field ) {
        this.lastField = field;
    }

    public int getMyId() {
        return this.myId;
    }

    public int getOpponentId() {
        return this.opponentId;
    }

    public String print() {
        StringBuilder builder = new StringBuilder();
        for ( int[] row : this.field ) {
            for ( int c : row ) {
                builder.append( c );
            }
            builder.append( "\n" );
        }
        return builder.toString();
    }

    public int getPlayerAt( int row, int col ) {
        return this.field[row][col];
    }

    public int[][] getField() {
        return this.field;
    }
}
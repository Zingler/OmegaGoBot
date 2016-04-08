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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import move.Move;
import util.ArrayStream;

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
    private double[][] laplace;

    private int[][] field;
    private GroupData groupData;

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
        if ( this.field[row][col] < 0 ) {
            return true;
        }
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

    public GroupData getGroups() {
        if ( this.groupData != null ) {
            return this.groupData;
        }

        List<List<Group>> groupTable = ArrayStream.mapToList( this.field, x -> null );

        boolean[][] visited = new boolean[this.rows][this.cols];
        for ( int i = 0; i < this.rows; i++ ) {
            for ( int j = 0; j < this.cols; j++ ) {
                int playerId = this.field[i][j];
                if ( !visited[i][j] && playerId != 0 ) {
                    Group g = new Group( playerId );
                    g.addMove( new Move( i, j ) );
                    groupVisit( visited, i, j, g );

                    for ( Move m : g.getMoves() ) {
                        groupTable.get( m.getRow() ).set( m.getCol(), g );
                    }
                }
            }
        }
        return new GroupData( groupTable );
    }

    private void groupVisit( boolean[][] visited, int row, int col, Group currentGroup ) {
        int playerId = this.field[row][col];
        if ( playerId == 0 ) {
            currentGroup.addLiberty( new Move( row, col ) );
            return;
        } else if ( playerId != currentGroup.getPlayerId() ) {
            currentGroup.addOpponent( new Move( row, col ) );
            return;
        }

        if ( visited[row][col] ) {
            return;
        } else {
            visited[row][col] = true;
            currentGroup.addMove( new Move( row, col ) );
            for ( Move m : validAdjacentPoints( row, col ) ) {
                groupVisit( visited, m.getRow(), m.getCol(), currentGroup );
            }
        }
    }

    public Optional<Group> getGroupAt( int row, int col ) {
        return Optional.ofNullable( this.getGroups().getGroupTable().get( row ).get( col ) );
    }

    public Field simulateMyMove( Move move ) {
        Field newField = this.copy();
        newField.setPlayer( move.getRow(), move.getCol(), this.myId );
        List<Move> validPoints = this.validAdjacentPoints( move.getRow(), move.getCol() );
        List<Group> surroundingGroups = validPoints.stream().map( m -> newField.getGroupAt( m.getRow(), m.getCol() ) ).filter( Optional::isPresent )
                .map( Optional::get ).collect( Collectors.toList() );
        for ( Group g : surroundingGroups ) {
            if ( g.getLibertyCount() == 0 ) {
                for ( Move m : g.getMoves() ) {
                    newField.setPlayer( m.getRow(), m.getCol(), 0 );
                }
            }
        }
        // Check for suicide for when myplayer plays in middle of 4 other
        // stones, and none of them died
        if ( newField.validAdjacentPoints( move ).stream().allMatch( m -> newField.getPlayerAt( m.getRow(), m.getCol() ) == this.opponentId ) )
        {
            newField.setPlayer( move.getRow(), move.getCol(), 0 );
        }

        newField.setLastField( this );
        return newField;
    }

    public Field simulateCurrentPlayerMoveAndSwitch( Move move ) {
        Field nf = this.simulateMyMove( move );
        int myOldId = nf.getMyId();
        nf.setMyId( nf.getOpponentId() );
        nf.setOpponentId( myOldId );
        return nf;
    }

    private void setPlayer( int row, int col, int playerId ) {
        this.field[row][col] = playerId;
        this.groupData = null;
        this.laplace = null;
    }

    public List<Move> validAdjacentPoints( Move m ) {
        return validAdjacentPoints( m.getRow(), m.getCol() );
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

    public List<Move> validAdjacentAndDiagonalPoints( int row, int col ) {
        ArrayList<Move> moves = new ArrayList<>();
        for ( int i = -1; i <= 1; i++ ) {
            for ( int j = -1; j <= 1; j++ ) {
                if ( i == 0 && j == 0 ) {
                    continue;
                }
                int newRow = row + i;
                int newCol = col + j;
                if ( newRow >= 0 && newRow < this.cols && newCol >= 0 && newCol < this.rows ) {
                    moves.add( new Move( newRow, newCol ) );
                }
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

    public Group getLaplaceGroup( Move m, double lowerBound ) {
        calculateLaplace();
        Function<Double, Boolean> membershipTest = this.laplace[m.getRow()][m.getCol()] > 0 ? x -> x > lowerBound : x -> x < -lowerBound;

        boolean[][] visited = new boolean[this.rows][this.cols];
        // TODO player is hardcoded to 1.
        Group g = new Group( 1 );
        g.addMove( m );
        getLaplaceGroupRecurse( visited, m.getRow(), m.getCol(), membershipTest, g );
        return g;
    }

    private void getLaplaceGroupRecurse( boolean[][] visited, int row, int col, Function<Double, Boolean> test, Group g ) {
        if ( visited[row][col] ) {
            return;
        }
        if ( test.apply( this.laplace[row][col] ) ) {
            visited[row][col] = true;
            g.addMove( new Move( row, col ) );
            for ( Move m : this.validAdjacentPoints( new Move( row, col ) ) ) {
                getLaplaceGroupRecurse( visited, m.getRow(), m.getCol(), test, g );
            }
        } else {
            return;
        }
    }

    public double getLaplace( Move m ) {
        calculateLaplace();
        return this.laplace[m.getRow()][m.getCol()];
    }

    public double[][] getLaplace() {
        calculateLaplace();
        return this.laplace;
    }

    private void calculateLaplace() {
        if ( this.laplace != null ) {
            return;
        }

        this.laplace = new double[this.getRows()][this.getColumns()];
        for ( int i = 0; i < this.getRows() * 2; i++ ) {
            for ( int row = 0; row < this.getRows(); row++ ) {
                for ( int col = 0; col < this.getColumns(); col++ ) {
                    int player = this.getPlayerAt( row, col );
                    if ( player == this.getMyId() ) {
                        this.laplace[row][col] = 1;
                    } else if ( player == this.getOpponentId() ) {
                        this.laplace[row][col] = -1;
                    } else {
                        List<Move> adjacent = this.validAdjacentPoints( row, col );
                        this.laplace[row][col] = adjacent.stream().mapToDouble( m -> this.laplace[m.getRow()][m.getCol()] ).average().getAsDouble();
                    }
                }
            }
        }
    }
}
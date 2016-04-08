package z;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Singular;
import move.Move;
import util.ArrayStream;

import com.google.common.collect.ImmutableMap;

@Data
@Builder
public class TurnView {
    public static final Map<Integer, String> playerSymbols = ImmutableMap.of( 0, " ", 1, "●", 2, "○" );

    public transient int[][] playerMap;
    @Singular
    public List<BoardView> boardViews = new ArrayList<>();

    public Move lastMove;
    public String lastMoveType;
    public long time;
    public int roundNumber;
    public List<Move> otherMovesConsidered;

    @Getter( lazy = true )
    private final List<List<Position>> board = ArrayStream.mapToList( this.playerMap, p -> new Position( playerSymbols.get( p ), false, false ) );

    public TurnView finish() {
        if ( this.lastMove != null ) {
            this.getBoard().get( this.lastMove.getRow() ).get( this.lastMove.getCol() ).setLastMove( true );
        }
        if ( this.otherMovesConsidered != null ) {
            this.otherMovesConsidered.forEach( m -> this.getBoard().get( m.getRow() )
                    .get( m.getCol() )
                    .setConsideredMove( true ) );
        }
        for ( BoardView v : this.boardViews ) {
            for ( int i = 0; i < v.getCells().size(); i++ ) {
                for ( int j = 0; j < v.getCells().get( i ).size(); j++ ) {
                    v.getCells().get( i ).get( j ).setSymbol( this.getBoard().get( i ).get( j ).getSymbol() );
                }
            }
        }

        return this;
    }

    public void addBoardView( BoardView view ) {
        this.boardViews.add( view );
    }

    @Data
    @AllArgsConstructor
    public static class Position {
        private String symbol;
        private boolean lastMove;
        private boolean consideredMove;
    }
}

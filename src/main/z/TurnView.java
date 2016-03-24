package z;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import move.Move;
import util.ArrayStream;

import com.google.common.collect.ImmutableMap;

@Data
@Builder
public class TurnView {
    public static final Map<Integer, String> playerSymbols = ImmutableMap.of( 0, " ", 1, "●", 2, "○" );

    public transient int[][] playerMap;
    public List<List<String>> influenceColors;
    public List<List<String>> laplaceColors;
    public Move lastMove;
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
        return this;
    }

    @Data
    @AllArgsConstructor
    public static class Position {
        private String symbol;
        private boolean lastMove;
        private boolean consideredMove;
    }
}

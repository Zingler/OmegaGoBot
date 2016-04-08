package field;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import move.Move;

@Data
public class Group {
    Set<Move> moves = new HashSet<>();
    Set<Move> opponentMoves = new HashSet<>();
    Set<Move> liberties = new HashSet<>();
    private final int playerId;

    public int getLibertyCount() {
        return this.liberties.size();
    }

    public int getMoveCount() {
        return this.moves.size();
    }

    public void addMove( Move move ) {
        this.moves.add( move );
    }

    public void addLiberty( Move move ) {
        this.liberties.add( move );
    }

    public void addOpponent( Move move ) {
        this.opponentMoves.add( move );
    }

    public Move getAnyMove() {
        return this.moves.iterator().next();
    }
}
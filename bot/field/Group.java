package field;

import java.util.ArrayList;
import java.util.List;

import move.Move;

public class Group {
    List<Move> moves;
    List<Move> liberties;
    private int playerId;

    public Group( Move m, List<Move> liberties ) {
        this.moves = new ArrayList<>();
        this.moves.add( m );
        this.liberties = liberties;
    }

    public Group( List<Move> moves, List<Move> liberties ) {
        this.moves = moves;
        this.liberties = liberties;
    }

    public static Group merge( Group one, Group other ) {
        one.moves.addAll( other.getMoves() );
        one.liberties.addAll( other.liberties );
        return one;
    }

    public List<Move> getMoves() {
        return this.moves;
    }

    public void setPlayerId( int playerId ) {
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getLibertyCount() {
        return this.liberties.size();
    }

    public List<Move> getLiberties() {
        return this.liberties;
    }
}
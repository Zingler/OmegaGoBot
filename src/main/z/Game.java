package z;

import java.util.ArrayList;

import lombok.Getter;
import move.Move;
import bot.Bot;
import bot.GameState;
import bot.MoveWithDiagnostics;
import bot.OmegaGoBot;
import field.Field;

@Getter
public class Game {
    private int player;
    private int turn;
    private ArrayList<Bot> players;
    private Field currentField;
    private GameState currentState;
    private MoveWithDiagnostics lastMoveDiagnostics;

    public Game() {
        this.currentField = new Field().setColumns( 19 ).setRows( 19 ).setMyId( 1 ).setOpponentId( 2 ).initField();
        this.players = new ArrayList<>();

        this.currentState = new GameState();
        this.currentState.setField( this.currentField );

        this.players.add( new OmegaGoBot() );
        this.players.add( new OmegaGoBot() );
        this.player = 1;
        this.turn = 1;
    }

    public void step() {
        MoveWithDiagnostics move = this.players.get( this.player - 1 ).getMoveWithDiagnostics( this.currentState, 1000 );
        this.lastMoveDiagnostics = move;
        play( move.move() );
    }

    private void play( Move move ) {
        this.currentField = this.currentState.getField();
        Field newField = this.currentField.simulateMyMove( move );
        int oldMyId = this.currentField.getMyId();
        newField.setMyId( this.currentField.getOpponentId() );
        newField.setOpponentId( oldMyId );
        this.currentField = newField;
        this.currentState.setField( newField );

        this.turn++;
        this.player = 3 - this.player;
        if ( this.player == 1 ) {
            this.currentState.setRoundNumber( this.currentState.getRoundNumber() + 1 );
        }
    }

    public void manualPlay( int row, int col ) {
        this.lastMoveDiagnostics = null;
        play( new Move( row, col ) );
    }

    public int getRoundNumber() {
        return this.currentState.getRoundNumber();
    }
}

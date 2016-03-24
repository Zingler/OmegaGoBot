package z;
import java.util.ArrayList;
import java.util.List;

import move.Move;
import bot.Bot;
import bot.GameState;
import bot.OmegaGoBot;
import field.Field;

public class PlayBots {
    public static void main( String[] args ) {
        long start = System.currentTimeMillis();
        Field currentField = new Field().setColumns( 19 ).setRows( 19 ).setMyId( 1 ).setOpponentId( 2 ).initField();
        List<Bot> players = new ArrayList<>();

        GameState currentState = new GameState();
        currentState.setField( currentField );

        players.add( new OmegaGoBot() );
        players.add( new OmegaGoBot() );

        int player = 1;
        int turn = 0;
        while ( turn < 200 ) {
            Move m = players.get( player - 1 ).getMove( currentState, 1000 );

            currentField = currentState.getField();
            Field newField = currentField.simulateMyMove( m );
            int oldMyId = currentField.getMyId();
            newField.setMyId( currentField.getOpponentId() );
            newField.setOpponentId( oldMyId );
            currentState.setField( newField );

            newField.print();

            turn++;
            player = 3 - player;
            if ( player == 1 ) {
                currentState.setRoundNumber( currentState.getRoundNumber() + 1 );
            }
        }
        System.out.println( "Done" );
        System.out.println( "Time taken = " + ( System.currentTimeMillis() - start ) + " millis" );
    }
}

package bot;

import move.Move;

public interface Bot {
    public Move getMove( GameState state, int timeout );

    public MovesWithDiagnostics getMoveWithDiagnostics( GameState currentState, int i );
}

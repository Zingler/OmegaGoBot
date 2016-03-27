package bot;

import move.Move;

public interface Bot {
    public Move getMove( GameState state, int timeout );

    public MoveWithDiagnostics getMoveWithDiagnostics( GameState currentState, int i );
}

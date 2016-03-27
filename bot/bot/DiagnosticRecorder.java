package bot;

public interface DiagnosticRecorder {
    public void newTurn();

    public void record( MoveWithDiagnostics move );
}

package bot;

public class MoveTimer implements DiagnosticRecorder {

    long start = 0;

    @Override
    public void newTurn() {
        this.start = System.currentTimeMillis();
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
        move.millis( System.currentTimeMillis() - this.start );
    }
}

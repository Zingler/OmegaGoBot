package bot;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;
import move.Move;

@Data
@Accessors( fluent = true )
public class MoveWithDiagnostics {
    Move move;
    double[][] influenceHeatMap;
    double[][] laplace;
    double[][] attackScores;
    List<Move> otherTopMoves;
    String type;
    long millis;
}

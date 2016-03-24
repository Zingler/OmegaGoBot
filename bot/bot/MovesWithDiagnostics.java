package bot;

import java.util.List;

import move.Move;

public class MovesWithDiagnostics {
    Move move;
    double[][] influenceHeatMap;
    double[][] laplace;
    List<Move> otherTopMoves;

    public MovesWithDiagnostics setMove( Move move ) {
        this.move = move;
        return this;
    }

    public Move getMove() {
        return this.move;
    }

    public MovesWithDiagnostics setInfluenceHeatMap( double[][] heatMap ) {
        this.influenceHeatMap = heatMap;
        return this;
    }

    public double[][] getInfluenceHeatMap() {
        return this.influenceHeatMap;
    }

    public MovesWithDiagnostics setLaplace( double[][] laplace ) {
        this.laplace = laplace;
        return this;
    }

    public double[][] getLaplace() {
        return this.laplace;
    }

    public MovesWithDiagnostics setOtherTopMoves( List<Move> otherMoves ) {
        this.otherTopMoves = otherMoves;
        return this;
    }

    public List<Move> getOtherTopMoves() {
        return this.otherTopMoves;
    }
}

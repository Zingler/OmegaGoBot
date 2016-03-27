package bot.strategies;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Singular;
import move.Move;
import bot.DiagnosticRecorder;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

@Builder
public class SequentialAttackingMoveStrategy implements AttackingMoveStrategy, DiagnosticRecorder {

    @Singular
    List<AttackingMoveStrategy> strats;

    @Override
    public void newTurn() {
        this.strats.forEach( DiagnosticRecorder::newTurn );
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
        this.strats.forEach( s -> s.record( move ) );
    }

    @Override
    public List<Move> getAttackingMoves( Field field, List<Group> attackableGroups ) {
        return this.strats.stream().flatMap( strat -> strat.getAttackingMoves( field, attackableGroups )
                .stream() ).collect( Collectors.toList() );
    }
}

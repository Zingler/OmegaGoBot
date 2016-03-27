package bot.strategies;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Singular;
import bot.DiagnosticRecorder;
import bot.MoveWithDiagnostics;
import field.Field;
import field.Group;

@Builder
public class SequentialAttackGroupCandidateStrategy implements AttackGroupCandidateStrategy, DiagnosticRecorder {

    @Singular
    List<AttackGroupCandidateStrategy> strats;

    @Override
    public void newTurn() {
        this.strats.forEach( DiagnosticRecorder::newTurn );
    }

    @Override
    public void record( MoveWithDiagnostics move ) {
        this.strats.forEach( s -> s.record( move ) );
    }

    @Override
    public List<Group> getAttackCandidates( Field f ) {
        return this.strats.stream().flatMap( strat -> strat.getAttackCandidates( f ).stream() ).collect( Collectors.toList() );
    }
}

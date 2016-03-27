package bot.strategies;

import java.util.List;

import bot.DiagnosticRecorder;
import field.Field;
import field.Group;

public interface AttackGroupCandidateStrategy extends DiagnosticRecorder {
    public List<Group> getAttackCandidates( Field f );
}

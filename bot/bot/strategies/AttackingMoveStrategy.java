package bot.strategies;

import java.util.List;

import move.Move;
import bot.DiagnosticRecorder;
import field.Field;
import field.Group;

public interface AttackingMoveStrategy extends DiagnosticRecorder {
    List<Move> getAttackingMoves( Field field, List<Group> attackableGroups );
}

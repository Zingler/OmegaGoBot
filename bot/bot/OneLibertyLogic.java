package bot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Data;
import move.Move;
import bot.strategies.ForcedMoveEvaluator;
import field.Field;
import field.Group;

public class OneLibertyLogic {
    ForcedMoveEvaluator forcedMoveEvaluator = new ForcedMoveEvaluator();

    public List<Move> getOneLibertyMoves( Field f ) {

        List<MoveGroup> oneLibertySpots = new ArrayList<>();
        for ( Group g : f.getGroups().getUniqueGroups() ) {
            if ( g.getLibertyCount() == 1 ) {
                oneLibertySpots.add( new MoveGroup( g.getLiberties().iterator().next(), g ) );
            }
        }

        List<MoveGroup> moveGroups = oneLibertySpots.stream().filter( this.isMyGroupAndForcedCapture( f ).negate() )
                .sorted( Comparator.comparing( mg -> -mg.getGroup().getMoves().size() ) ).collect( Collectors.toList() );

        return moveGroups.stream().map( MoveGroup::getMove ).collect( Collectors.toList() );
    }

    private Predicate<? super MoveGroup> isMyGroupAndForcedCapture( Field f ) {
        return mg -> mg.getGroup().getPlayerId() == f.getMyId()
                     && this.forcedMoveEvaluator.evaluateDefend( mg.getMove(), mg.getGroup().getAnyMove(), f ).isForcedCapture();
    }

    @Data
    static class MoveGroup {
        final Move move;
        final Group group;
    }
}

package bot.strategies;

import java.util.Optional;
import java.util.Set;

import lombok.val;
import move.Move;
import field.Field;
import field.Group;

public class ForcedMoveEvaluator {

    /**
     * Function to check for ladder type scenarios. Returns true if an attack
     * works. Probably breaks in a bunch of cases where a ladder makes a forcing
     * move for the other player.
     */
    public ForcedResult evaluateAttack( Move move, Move moveUnderAttack, Field field ) {
        Move nextMove = move;
        Field currentField = field;
        boolean forcedCapture = false;
        int forcedMovesCount = 0;
        while ( true ) {
            currentField = currentField.simulateCurrentPlayerMoveAndSwitch( nextMove );
            forcedMovesCount++;
            Optional<Group> g = currentField.getGroupAt( moveUnderAttack.getRow(), moveUnderAttack.getCol() );
            if ( !g.isPresent() ) {
                forcedCapture = true;
                break;
            }
            Set<Move> liberties = g.get().getLiberties();
            if ( liberties.size() >= 2 ) {
                break;
            }

            if ( liberties.size() == 1 ) {
                Move forcedMove = liberties.iterator().next();
                currentField = currentField.simulateCurrentPlayerMoveAndSwitch( forcedMove );
                forcedMovesCount++;
                Optional<Group> forcedGroup = currentField.getGroupAt( forcedMove.getRow(), forcedMove.getCol() );
                if ( !forcedGroup.isPresent() ) {
                    forcedCapture = true;
                    break;
                }
                Set<Move> forcedGroupLiberties = forcedGroup.get().getLiberties();
                val onextMove = selectMostForcingMove( forcedGroupLiberties, moveUnderAttack, currentField );
                if ( !onextMove.isPresent() ) {
                    break;
                }
                nextMove = onextMove.get();
            }
        }

        return new ForcedResult( forcedCapture, currentField, forcedMovesCount );
    }

    public ForcedResult evaluateDefend( Move potentialMove, Move moveUnderAttack, Field field ) {
        Field newField = field.simulateCurrentPlayerMoveAndSwitch( potentialMove );
        Optional<Group> group = newField.getGroupAt( moveUnderAttack.getRow(), moveUnderAttack.getCol() );
        if ( !group.isPresent() ) {
            return new ForcedResult( true, newField, 0 );
        }

        Optional<Move> m = selectMostForcingMove( group.get().getLiberties(), moveUnderAttack, newField );
        if ( !m.isPresent() ) {
            return new ForcedResult( false, newField, 0 );
        }
        return evaluateAttack( m.get(), moveUnderAttack, newField );
    }

    private Optional<Move> selectMostForcingMove( Set<Move> forcedGroupLiberties, Move moveUnderAttack, Field currentField ) {
        int row = moveUnderAttack.getRow();
        int col = moveUnderAttack.getCol();
        for ( Move m : forcedGroupLiberties ) {
            Field newField = currentField.simulateCurrentPlayerMoveAndSwitch( m );
            Optional<Group> forcedGroup = newField.getGroupAt( row, col );
            if ( !forcedGroup.isPresent() ) {
                return Optional.of( m );
            }
            Set<Move> liberties = forcedGroup.get().getLiberties();
            if ( liberties.size() != 1 ) {
                // Shouldn't occur
                continue;
            }
            newField = newField.simulateCurrentPlayerMoveAndSwitch( liberties.iterator().next() );
            int moveValue = newField.getGroupAt( row, col ).map( Group::getLibertyCount ).orElse( 0 );
            if ( moveValue <= 2 ) {
                return Optional.of( m );
            }
        }
        return Optional.empty();
    }
}

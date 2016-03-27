import lombok.val;
import move.Move;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import bot.strategies.ForcedMoveEvaluator;
import field.Field;

public class ForcedMoveTest {
    ForcedMoveEvaluator evaluator;
    Field field;

    @Before
    public void setup() {
        this.field = new Field().setMyId( 1 ).setOpponentId( 2 ).setRows( 5 ).setColumns( 5 );
        this.evaluator = new ForcedMoveEvaluator();
    }

    @Test
    public void ladder_dead() {
        this.field.setField( new int[][] { { 0, 0, 0, 0, 0 },
                                          { 0, 0, 0, 0, 0 },
                                          { 1, 0, 0, 0, 0 },
                                          { 1, 2, 0, 0, 0 },
                                          { 0, 1, 0, 0, 0 } } );
        val result = this.evaluator.evaluateAttack( new Move( 3, 2 ), new Move( 3, 1 ), this.field );
        Assert.assertThat( result.isForcedCapture(), Is.is( true ) );
    }

    @Test
    public void ladder_isAlive() {
        this.field.setField( new int[][] { { 0, 0, 2, 0, 0 },
                                          { 0, 0, 0, 0, 0 },
                                          { 1, 0, 0, 0, 0 },
                                          { 1, 2, 0, 0, 0 },
                                          { 0, 1, 0, 0, 0 } } );
        val result = this.evaluator.evaluateAttack( new Move( 3, 2 ), new Move( 3, 1 ), this.field );
        Assert.assertThat( result.isForcedCapture(), Is.is( false ) );
    }
}

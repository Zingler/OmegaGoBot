import java.util.List;

import move.Move;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsNot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import field.Field;
import field.Group;

public class FieldTest {

    Field field;

    @Before
    public void setup() {
        this.field = new Field().setMyId( 1 ).setOpponentId( 2 ).setRows( 3 ).setColumns( 3 );
    }

    @Test
    public void suicideMove_stoneIsDead() {
        this.field.setField( new int[][] { { 2, 2, 2 }, { 2, 0, 2 }, { 2, 2, 2 } } );

        List<Move> moves = this.field.getAvailableMoves();
        Assert.assertThat( moves.size(), Is.is( 1 ) );
    }

    @Test
    public void ko_cantPlayThere() {
        this.field.setField( new int[][] { { 0, 0, 0 }, { 1, 2, 0 }, { 2, 0, 2 } } );
        this.field.setLastField( this.field.copy().setField( new int[][] { { 0, 0, 0 }, { 1, 2, 0 }, { 0, 1, 2 } } ) );

        List<Move> moves = this.field.getAvailableMoves();
        Assert.assertThat( moves.size(), Is.is( 4 ) );
        Assert.assertThat( moves, IsNot.not( IsCollectionContaining.hasItem( new Move( 2, 1 ) ) ) );
    }

    @Test
    public void simpleGetGroupInfo() {
        this.field.setField( new int[][] { { 0, 2, 0 }, { 0, 2, 0 }, { 0, 0, 0 } } );

        Group g = this.field.getGroupAt( 1, 1 ).get();
        Assert.assertThat( g.getMoves().size(), Is.is( 2 ) );
        Assert.assertThat( g.getLibertyCount(), Is.is( 5 ) );
    }

    @Test
    public void biggerGetGroupInfo() {
        this.field.setField( new int[][] { { 0, 2, 0 }, { 0, 2, 0 }, { 2, 2, 2 } } );

        Group g = this.field.getGroupAt( 2, 1 ).get();
        Assert.assertThat( g.getMoves().size(), Is.is( 5 ) );
        Assert.assertThat( g.getLibertyCount(), Is.is( 4 ) );
    }

    @Test
    public void groupWithHoleGetGroupInfo() {
        this.field.setField( new int[][] { { 2, 2, 2 }, { 2, 0, 2 }, { 2, 2, 2 } } );

        Group g = this.field.getGroupAt( 0, 0 ).get();
        Assert.assertThat( g.getMoves().size(), Is.is( 8 ) );
        Assert.assertThat( g.getLibertyCount(), Is.is( 1 ) );
    }

    @Test
    public void twoPlayerGroups() {
        this.field.setField( new int[][] { { 2, 1, 1 }, { 2, 0, 0 }, { 2, 2, 2 } } );

        Group g = this.field.getGroupAt( 0, 0 ).get();
        Assert.assertThat( g.getMoves().size(), Is.is( 5 ) );
        Assert.assertThat( g.getLibertyCount(), Is.is( 2 ) );
    }

    @Test
    public void nestedGroupsNoLiberties() {
        this.field.setField( new int[][] { { 2, 2, 2 }, { 2, 1, 2 }, { 2, 2, 2 } } );

        Group g = this.field.getGroupAt( 1, 0 ).get();
        Assert.assertThat( g.getMoves().size(), Is.is( 8 ) );
        Assert.assertThat( g.getLibertyCount(), Is.is( 0 ) );
    }

}

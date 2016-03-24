import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;

import bot.OmegaGoBot;

public class OmegaGoBotTest {
    public OmegaGoBot bot = new OmegaGoBot();

    @Test
    public void test() {
        List<Double> list = new ArrayList<>();
        list.add( 2.3 );
        list.add( 1.2 );
        list.add( 4.5 );
        double min = list.stream().min( Comparator.comparingDouble( n -> a( n ) ) ).get();
        Assert.assertThat( min, Is.is( 1.2 ) );
    }

    public double a( double n ) {
        System.out.println( "run compare" );
        return n;
    }
}

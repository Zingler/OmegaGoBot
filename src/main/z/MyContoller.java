package z;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import util.ArrayStream;
import bot.MovesWithDiagnostics;

@Controller
public class MyContoller {

    Game game;

    @RequestMapping( "/" )
    String home() {
        this.game = new Game();
        return "board";
    }

    @RequestMapping( "/newgame" )
    @ResponseBody
    TurnView newGame() {
        this.game = new Game();
        return TurnView.builder().playerMap( this.game.getCurrentField().getField() ).build().finish();
    }

    @RequestMapping( "/step" )
    @ResponseBody
    TurnView step( @RequestParam( defaultValue = "1", required = false ) int steps ) {
        while ( steps-- > 0 ) {
            this.game.step();
        }
        MovesWithDiagnostics lastMoveDiagnostics = this.game.getLastMoveDiagnostics();
        return TurnView.builder()
                .playerMap( this.game.getCurrentField().getField() )
                .influenceColors( heatMapColors( lastMoveDiagnostics.getInfluenceHeatMap() ) )
                .laplaceColors( heatMapColors( lastMoveDiagnostics.getLaplace() ) )
                .lastMove( lastMoveDiagnostics.getMove() )
                .otherMovesConsidered( lastMoveDiagnostics.getOtherTopMoves() ).build().finish();
    }

    @RequestMapping( "/play" )
    @ResponseBody
    TurnView play( @RequestParam int row,
                   @RequestParam int col,
                   @RequestParam( defaultValue = "false" ) boolean botPlayAfterHuman ) {
        this.game.manualPlay( row, col );
        if ( botPlayAfterHuman ) {
            return step( 1 );
        } else {
            return TurnView.builder()
                    .playerMap( this.game.getCurrentField().getField() )
                    .build().finish();
        }
    }

    public List<List<String>> heatMapColors( double[][] heatMap ) {
        Double min = ArrayStream.reduce( heatMap, Double::min, Double.MAX_VALUE );
        Double max = ArrayStream.reduce( heatMap, Double::max, Double.MIN_VALUE );
        Function<Double, Double> scaler = x -> x < 0 ? -x / min : x / max;
        List<List<String>> result = ArrayStream.mapToList( heatMap, heat -> {
            double scale = scaler.apply( heat );
            int red = 0;
            int green = 0;
            if ( scale < 0 ) {
                red = 255;
                green = (int) ( ( 1 + scale ) * 255 );
            } else {
                green = 255;
                red = (int) ( ( 1 - scale ) * 255 );
            }
            int blue = 0;
            int color = ( red << 16 ) + ( green << 8 ) + blue;
            return "#" + String.format( "%06X", color );
        } );
        return result;
    }
}

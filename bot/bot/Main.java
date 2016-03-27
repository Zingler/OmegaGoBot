package bot;


public class Main {

    public static void main( String[] args ) {
        BotParser parser = new BotParser( new OmegaGoBot() );
        parser.run();
    }
}

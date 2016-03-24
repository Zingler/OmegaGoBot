package player;

public class Player {
    
    private String name;
    private int points;

    public Player(String name) {
        this.name = name;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    
    public int getPoints() {
        return this.points;
    }
    
    public String getName() {
        return this.name;
    }
}

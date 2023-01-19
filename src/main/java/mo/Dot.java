package mo;

public class Dot {
    private int x;
    private int y;

    public Dot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("X axis must be positive");
        }
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("X axis must be positive");
        }
        this.y = y;
    }

    public double distanceTo(Dot anotherDot){
        return Math.sqrt(Math.pow(x, anotherDot.getX()) + Math.pow(y, anotherDot.getY()));
    }
}

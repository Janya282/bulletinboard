public class Note {

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final String colour;
    private final String content;

    public Note(int x, int y, int width, int height, String colour, String content) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.colour = colour;
        this.content = content;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getColour() {
        return colour;
    }

    public String getContent() {
        return content;
    }

    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    public String toProtocolString(boolean pinned) {
        return "NOTE " + x + " " + y + " " + colour + " " + content + " PINNED=" + pinned;
    }
}










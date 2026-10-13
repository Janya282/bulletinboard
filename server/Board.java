import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

    private final int width;
    private final int height;
    private final int noteWidth;
    private final int noteHeight;
    private final List<String> colours; 

    private final List<Note> notes = new ArrayList<>();
    private final Set<String> pinSet = new HashSet<>();
    private final List<Pin> pins = new ArrayList<>();

    public Board(int width, int height, int noteWidth, int noteHeight, List<String> colours) {
        this.width = width;
        this.height = height;
        this.noteWidth = noteWidth;
        this.noteHeight = noteHeight;
        this.colours = new ArrayList<>(colours);
    }

    // ---------- GETTERS ----------
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getNoteWidth() {
        return noteWidth;
    }

    public int getNoteHeight() {
        return noteHeight;
    }

    public List<String> getColours() {
        return Collections.unmodifiableList(colours);
    }

    // ---------- NOTES ----------
    public synchronized void addNote(int x, int y, String colour, String content) {
        notes.add(new Note(x, y, noteWidth, noteHeight, colour, content));
    }

    public synchronized List<Note> getNotes() {
        return new ArrayList<>(notes);
    }

    // ---------- PINS ----------
    public synchronized List<Pin> getPins() {
        return new ArrayList<>(pins);
    }

    public synchronized void addPin(int x, int y) {
        String key = x + "," + y;
        if (pinSet.add(key)) {
            pins.add(new Pin(x, y));
        }
    }

    public synchronized boolean removePin(int x, int y) {
        String key = x + "," + y;
        if (!pinSet.remove(key)) {
            return false;
        }
        pins.removeIf(p -> p.getX() == x && p.getY() == y);
        return true;
    }

    // ---------- ATOMIC OPERATIONS ----------
    public synchronized void shake() {
        notes.removeIf(n -> !isNotePinned(n));
    }

    public synchronized void clear() {
        notes.clear();
        pins.clear();
        pinSet.clear();
    }

    // ---------- HELPERS ----------
    public synchronized boolean isNotePinned(Note n) {
        for (Pin p : pins) {
            if (n.contains(p.getX(), p.getY())) {
                return true;
            }
        }
        return false;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isNoteFullyInBounds(int x, int y) {
        return x >= 0 && y >= 0 && x + noteWidth <= width && y + noteHeight <= height;
    }

    public boolean isSupportedColour(String colour) {
        for (String c : colours) {
            if (c.equalsIgnoreCase(colour)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean hasCompleteOverlapAt(int x, int y) {
        for (Note n : notes) {
            if (n.getX() == x && n.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean anyNoteContains(int x, int y) {
        for (Note n : notes) {
            if (n.contains(x, y)) {
                return true;
            }
        }
        return false;
    }
}












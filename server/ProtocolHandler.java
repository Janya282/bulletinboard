import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ProtocolHandler implements Runnable {

    private final Socket socket;
    private final Board board;

    public ProtocolHandler(Socket socket, Board board) {
        this.socket = socket;
        this.board = board;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {

            out.println("BOARD " + board.getWidth() + " " + board.getHeight());
            out.println("NOTE " + board.getNoteWidth() + " " + board.getNoteHeight());
            out.println("COLOURS " + String.join(" ", board.getColours()));
            out.println("READY");

            String line;
            while ((line = in.readLine()) != null) {
                handle(line.trim(), out);
            }

        } catch (Exception e) {
        }
    }

    private void handle(String line, PrintWriter out) {
        if (line == null || line.isEmpty()) {
            return;
        }

        String[] split = line.split(" ", 2);
        String cmd = split[0].toUpperCase();
        String rest = (split.length == 2) ? split[1] : "";

        try {
            switch (cmd) {
                case "POST":
                    handlePost(rest, out);
                    break;
                case "GET":
                    handleGet(rest, out);
                    break;
                case "PIN":
                    handlePin(rest, out);
                    break;
                case "UNPIN":
                    handleUnpin(rest, out);
                    break;
                case "SHAKE":
                    board.shake();
                    out.println("OK SHAKE_COMPLETE");
                    break;
                case "CLEAR":
                    board.clear();
                    out.println("OK BOARD_CLEARED");
                    break;
                case "DISCONNECT":
                    out.println("OK DISCONNECTING");
                    try {
                        socket.close();
                    } catch (Exception ignored) {
                    }
                    break;
                default:
                    out.println("ERROR INVALID_FORMAT Unknown command");
            }
        } catch (NumberFormatException e) {
            out.println("ERROR INVALID_FORMAT Invalid number format");
        } catch (Exception e) {
            // Fallback: keep connection alive and report error
            String msg = (e.getMessage() == null) ? "" : e.getMessage();
            out.println("ERROR INVALID_FORMAT " + msg);
        }
    }

    // ----------------- POST -----------------
    private void handlePost(String rest, PrintWriter out) {
        // POST <x> <y> <color> <message>
        String[] args = rest.split(" ", 4);
        if (args.length < 4) {
            out.println("ERROR INVALID_FORMAT POST requires coordinates, color and message");
            return;
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);
        String colour = args[2];
        String content = args[3];

        if (!board.isNoteFullyInBounds(x, y)) {
            out.println("ERROR OUT_OF_BOUNDS Note exceeds the board boundaries");
            return;
        }

        if (!board.isSupportedColour(colour)) {
            out.println("ERROR COLOR_NOT_SUPPORTED " + colour + " is not a valid color");
            return;
        }

        if (board.hasCompleteOverlapAt(x, y)) {
            out.println("ERROR COMPLETE_OVERLAP Note overlaps an existing note entirely");
            return;
        }

        board.addNote(x, y, colour, content);
        out.println("OK NOTE_POSTED");
    }

    // ----------------- GET -----------------
    private void handleGet(String rest, PrintWriter out) {
        String trimmed = rest.trim();

        // GET PINS
        if (!trimmed.isEmpty() && trimmed.equalsIgnoreCase("PINS")) {
            List<Pin> pins = board.getPins();
            out.println("OK " + pins.size());
            for (Pin p : pins) {
                out.println("PIN " + p.getX() + " " + p.getY());
            }
            return;
        }

        // General GET form (criteria are optional, missing => ALL)
        // GET color=<color> contains=<x> <y> refersTo=<substring>

        String colourFilter = null;
        Integer containsX = null;
        Integer containsY = null;
        String refersTo = null;

        if (!trimmed.isEmpty()) {
            // Special handling: refersTo may be the last criterion and can contain spaces.
            int idx = indexOfIgnoreCase(trimmed, "refersTo=");
            String beforeRefers = trimmed;
            if (idx >= 0) {
                refersTo = trimmed.substring(idx + "refersTo=".length()).trim();
                beforeRefers = trimmed.substring(0, idx).trim();
                if (refersTo.isEmpty()) {
                    out.println("ERROR INVALID_FORMAT refersTo requires a substring");
                    return;
                }
            }

            String[] tokens = beforeRefers.isEmpty() ? new String[0] : beforeRefers.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                String t = tokens[i];
                if (t.startsWith("color=")) {
                    colourFilter = t.substring("color=".length());
                    if (colourFilter.isEmpty()) {
                        out.println("ERROR INVALID_FORMAT color requires a value");
                        return;
                    }
                } else if (t.startsWith("contains=")) {
                    String afterEq = t.substring("contains=".length());
                    if (afterEq.isEmpty()) {
                        out.println("ERROR INVALID_FORMAT contains requires x y");
                        return;
                    }
                    containsX = Integer.parseInt(afterEq);
                    if (i + 1 >= tokens.length) {
                        out.println("ERROR INVALID_FORMAT contains requires x y");
                        return;
                    }
                    containsY = Integer.parseInt(tokens[++i]);
                } else if (!t.isEmpty()) {
                    out.println("ERROR INVALID_FORMAT Unrecognized GET criterion: " + t);
                    return;
                }
            }
        }

        if (colourFilter != null && !board.isSupportedColour(colourFilter)) {
            out.println("ERROR COLOR_NOT_SUPPORTED " + colourFilter + " is not a valid color");
            return;
        }

        if (containsX != null && containsY != null && !board.isInBounds(containsX, containsY)) {
            out.println("ERROR OUT_OF_BOUNDS Coordinate exceeds the board boundaries");
            return;
        }

        List<Note> all = board.getNotes();
        List<Note> filtered = new ArrayList<>();
        for (Note n : all) {
            if (colourFilter != null && !n.getColour().equalsIgnoreCase(colourFilter)) {
                continue;
            }
            if (containsX != null && containsY != null && !n.contains(containsX, containsY)) {
                continue;
            }
            if (refersTo != null && !n.getContent().toLowerCase().contains(refersTo.toLowerCase())) {
                continue;
            }
            filtered.add(n);
        }

        out.println("OK " + filtered.size());
        for (Note n : filtered) {
            out.println(n.toProtocolString(board.isNotePinned(n)));
        }
    }

    private static int indexOfIgnoreCase(String haystack, String needle) {
        String h = haystack.toLowerCase();
        String n = needle.toLowerCase();
        return h.indexOf(n);
    }

    // ----------------- PIN -----------------
    private void handlePin(String rest, PrintWriter out) {
        String[] args = rest.trim().split("\\s+");
        if (args.length < 2) {
            out.println("ERROR INVALID_FORMAT PIN requires coordinates");
            return;
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        if (!board.isInBounds(x, y)) {
            out.println("ERROR OUT_OF_BOUNDS Coordinate exceeds the board boundaries");
            return;
        }

        if (!board.anyNoteContains(x, y)) {
            out.println("ERROR NO_NOTE_AT_COORDINATE No note contains the specified coordinate");
            return;
        }

        board.addPin(x, y);
        out.println("OK PIN_ADDED");
    }

    // ----------------- UNPIN -----------------
    private void handleUnpin(String rest, PrintWriter out) {
        String[] args = rest.trim().split("\\s+");
        if (args.length < 2) {
            out.println("ERROR INVALID_FORMAT UNPIN requires coordinates");
            return;
        }

        int x = Integer.parseInt(args[0]);
        int y = Integer.parseInt(args[1]);

        if (!board.isInBounds(x, y)) {
            out.println("ERROR OUT_OF_BOUNDS Coordinate exceeds the board boundaries");
            return;
        }

        if (!board.removePin(x, y)) {
            out.println("ERROR PIN_NOT_FOUND No pin exists at the given coordinates");
            return;
        }

        out.println("OK PIN_REMOVED");
    }
}












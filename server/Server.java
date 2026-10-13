import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {

        if (args.length < 6) {
            System.out.println("Usage: java Server <port> <board_width> <board_height> <note_width> <note_height> <color1> ... <colorN>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int boardWidth = Integer.parseInt(args[1]);
        int boardHeight = Integer.parseInt(args[2]);
        int noteWidth = Integer.parseInt(args[3]);
        int noteHeight = Integer.parseInt(args[4]);

        List<String> colours = new ArrayList<>();
        for (int i = 5; i < args.length; i++) {
            colours.add(args[i]);
        }

        Board board = new Board(boardWidth, boardHeight, noteWidth, noteHeight, colours);
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Server running on port " + port);
        System.out.println("Board size: " + boardWidth + "x" + boardHeight + ", Note size: " + noteWidth + "x" + noteHeight);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(new ProtocolHandler(client, board)).start();
        }
    }
}







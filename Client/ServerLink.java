import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
public class ServerLink{
    private Socket connection;//it's for the socket connection
    private BufferedReader serverReader;//this what we will use to listen/read what server says(like picking up the phone)
    private PrintWriter serverWriter;//this is what we use to speak/write to the server
    private boolean connected;//this will help us in keeping track if what we are connected or not
    private int boardWidth;//board's dimensions that server will tell us when we connect to it
    private int boardHeight;
    private int noteWidth;//note's dimensions that server will tell us when we connect to it
    private int noteHeight;
    private String[] colorsToUse;// colors that server supports for the notes
    public String connectToServer(String IP,int port){//this will help us in connecting to the server with the help of IP and port we give it
        try {
            connection=new Socket(IP,port);//creating the socket connectiion with the server
            serverReader=new BufferedReader(new InputStreamReader(connection.getInputStream()));//it will set up the reader so that we can read what server sends
            serverWriter=new PrintWriter(connection.getOutputStream(),true);//it will set up the writer so that we can send commands to the server
            connected=true;//this will mark that we are now connected
            String boardLine=serverReader.readLine();//first line has board dimensions like "BOARD 40 30"
            String noteLine=serverReader.readLine();//second line has note dimensions like "NOTE 4 2"
            String colorLine=serverReader.readLine();//third line has colors like "COLOURS red white green yellow"
            String readyLine=serverReader.readLine();//fourth line should be "READY"
            if(boardLine==null || noteLine==null || colorLine==null || readyLine==null){//if any line is missing something is wrong
                disconnectFromTheServer();//when server doesn't send complete handshake we disconnect
                return "ERROR: Server didn't send complete handshake.";
            }
            if(!readyLine.equals("READY")){//make sure the last line is READY
                disconnectFromTheServer();//if not READY something went wrong
                return "ERROR: Server handshake failed.";
            }
            parseBoardInfo(boardLine, noteLine, colorLine);//it will parse the 3 info lines that server sends us
            return "Connected! Board size is " +boardWidth+"x"+boardHeight;
        } catch(UnknownHostException e){
            connected=false;//it couldn't finf a derver at that IP address
            return "ERROR: No server found at "+IP+"!";
        }catch(IOException e){
            connected=false;//something must've gone wrong while trying to connect
            return "ERROR: Could not connect to server- "+e.getMessage();
        }

    }private void parseBoardInfo(String boardLine, String noteLine, String colorLine){//this will parse the 3 lines from server handshake
        String[] b=boardLine.split(" ");//it will split it by spaces
        boardWidth=Integer.parseInt(b[1]);//second part of it is width
        boardHeight=Integer.parseInt(b[2]);//third part of it is height
        String[] n=noteLine.split(" ");
        noteWidth=Integer.parseInt(n[1]);//second part of it is note width
        noteHeight=Integer.parseInt(n[2]);//third part of it is note height
        String[] c=colorLine.split(" ");
        colorsToUse=new String[c.length-1];//everything after COLOURS word is a color
        for(int i=1; i<c.length; i++){//loop starting from index 1 to skip the word COLOURS
            colorsToUse[i-1]=c[i];//store each color in the array
    }
}
    public String sendCommand(String command){//this will send a command to the server and reads back what it actually says
        if(!connected){//it will check if we are connected or not before trying to send anything
            return "ERROR: Not connected to the server.";// if nothing is connected so we can't send anything right now
            }
            try {
                serverWriter.println(command);//it will send the command to the server
                String reply=serverReader.readLine();//it will read back what server says
                if(reply==null){//if the reply is nothing then the server must be dosconnected or crashed
                    disconnectFromTheServer();//we will disconnect from the server
                    return "ERROR: Server stopped replying.";
                }if(reply.startsWith("OK")){//if the reply starts with OK it will check if there are more lines to read from
                    return readFullReply(reply);//GET commands can return multiple lines so we handle this
                }
                return reply;//if there is an error we can simply return it 
            }catch(IOException e){
                disconnectFromTheServer();//its when we lost the connection while sending or reading
                return "ERROR: Lost connection to the server- "+e.getMessage();
            }
    }private String readFullReply(String firstLine){//this will handle multiple lines when the server sends more than one like when GET returns OK 2 followed by two NOTE lines
        try {
            String[] parts=firstLine.split(" ");//it will split the first line to check if the second part is an integer
            if(parts.length==2){
                try {
                    int linesExtra=Integer.parseInt(parts[1]);//this will tell us how many extra lines we need to read
                    StringBuilder fullReply=new StringBuilder(firstLine+"\n");//we will start building the full reply with the first line
                    for(int x=0;x<linesExtra;x++){//it will loop through and read each extra line server is sending 
                        String nextLine=serverReader.readLine();//it will read the next line from the server
                        if(nextLine!=null){//it will add it to iur response if we got something
                            fullReply.append(nextLine).append("\n");
                        }
                  }return fullReply.toString().trim();//it will return the full reply we combined together 
                }catch(NumberFormatException e){
                return firstLine;//if the second part isn't an integer so its a status like NOTE_POSTED
      }
        }return firstLine;//it will retuen simply OK if theres nothing after

    }catch(IOException e){
        disconnectFromTheServer();//if we lost connection while reading the extra lines
        return "ERROR: Lost connection while reading the reply- "+e.getMessage();
    }
 }public String disconnectFromTheServer(){//it will disconnect from the server properly
    try {
        if(connected&&serverWriter!=null){//it will check if we are connected and the server will are now disconnecting
            serverWriter.println("DISCONNECT");
            String reply=serverReader.readLine();//it will read the OK DISCONNECTING  that server will send us back before closing
        }
        if(connection!=null&&!connection.isClosed()){//it will close the socket if it is still open
            connection.close();
        }
        connected=false;
        return "Disconnected from the server.";
    } catch (IOException e) {
        connected=false;//if something went wrong while disconnecting we will still mark ourselves as disconnected
        return "ERROR: Problem while disconnecting- "+e.getMessage();
        
    } 

 }
 public boolean isConnected(){//this will help us in checking if we are connected to the server or not
        return connected;
    }public String[] getColorsToUse(){//this will return the colors that server supports for the notes
        return colorsToUse;
    }
    public int getBoardWidth(){//this will return the board width that server told us when we connected
        return boardWidth;
    }public int getBoardHeight(){//this will return the board height that server told us when we connected
        return boardHeight;
   }public int getNoteWidth(){//this will return the note width that server told us when we connected
        return noteWidth;
    }public int getNoteHeight(){//this will return the note height that server told us when we connected
        return noteHeight;
    }
}




import java.awt.*;
import javax.swing.*; 

public class ClientPanel extends JFrame{
    //this for the connection panel these are the field and buttons for connecting
    private ServerLink serverLink;//this is the connection to the serverLink file
    private JTextField ipField;//this is where user will enter the IP address of the server
    private JTextField portField;//this is where user will enter the port number of the server
    private JButton connectButton;//this button will help us in connecting to the server
    private JButton disconnectButton;//this button will help us in disconnecting from the server
    //this is the post panel these are the fields and buttons for posting a note
    private JTextField postXField;//this is where user will enter the X coordinate for posting a note
    private JTextField postYField;//this is where user will enter the Y coordinate for posting a note
    private JComboBox<String> dropdownColor;//dropdown to pick up the color 
    private JTextField fieldForMessage;// where user types the note message
    private JButton postNoteButton;//this button will help the user in posting the note to the board
    //this is the get panel these are the fields and buttons for getting a note
    private JTextField getColorField;
    private JTextField getHasXField;//
    private JTextField getHasYField;
    private JTextField getRefersToField;
    private JButton getNoteButton;//this is the button to get a note from the board
    private JButton getPinsButton;//this is the button to get all the pins from the board
    //this is to pin unpin 
    private JTextField pinXField;//this is where user will enter the X coordinate for where to put the pin
    private JTextField pinYField;//this is where user will enter the Y coordinate for where to put the pin
    private JButton pinButton;//this button will help the user in pinning a note
    private JTextField unpinXField;//x coordinate to unpin 
    private JTextField unpinYField;//y coordinate to unpin
    private JButton unpinButton;//this button will help the user in unpinning a note
    //shake and clear buttons
    private JButton shakeButton;//this button will help the user in shaking the board
    private JButton clearButton;//this button will help the user in clearing the board
    private JTextArea outputArea;//this will the output area that show server responses to the user
    public ClientPanel(){
        setTitle("✨ Bulletin Board Client ✨");
        setSize(850,650);//this is for setting up the size of window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//this will make the program close when we close the window
        setLayout(new BorderLayout());//Using the BorderLayout so that we can put panels in different positions
        serverLink=new ServerLink();//this will create the serverLink to handle all the interactions
        UIManager.put("Panel.background",new Color(245,245,250));//light lavender background
        UIManager.put("TextField.background",new Color(255,255,255));//white text fields
        UIManager.put("ComboBox.background",new Color(255,255,255));//white dropdowns
        String[] colors= {"red","white","green","yellow"};//these are the colors that server supports
        JPanel mainPanel=new JPanel();//this is the main panel that holds other panel 
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));//BoxLayout to arrange panels vertically
        mainPanel.add(createConnectionPanel());//connection will go first at the top
        mainPanel.add(createPostPanel(colors));//post panel will go second
        mainPanel.add(createGetPanel());//get panel will go after the post panel
        mainPanel.add(makePinUnpinPanel());//pin unpin panel will go after get panel
        mainPanel.add(createActionsPanel());//shake and clear will go at the end
        JScrollPane scrollPane=new JScrollPane(mainPanel);//this will make the main panel scrollable if the window is small
        add(scrollPane, BorderLayout.CENTER);//adding the scrollable main panel to the center of the window
        add(createOutputPanel(), BorderLayout.SOUTH);//this will add the output panel at the bottom of the window
        setVisible(true);
        showOutput("✨ Welcome to the Bulletin Board Client!✨\nPlease connect to a server to begin.");

    }
    private JPanel createConnectionPanel(){//this will create the connection panel at the top with the IP port and connect disconnect buttons
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Connection"));//it will add the titled border so that the user know this is the connection panel
        panel.setLayout(new FlowLayout());//FlowLayout will arrange the components from left to right
        panel.add(new JLabel("Server IP:"));//adds the serber IP
        ipField=new JTextField("localhost",12);//this will create the text field with the default value localhost with the width of 12 charcters 
        panel.add(ipField);//adding the IP field to the panel
        panel.add(new JLabel("Port:"));//adds the port label
        portField=new JTextField("4554",6);//this will create the text field with the default value 12345 with the width of 6 charcters
        panel.add(portField);//adding the port field to the panel
        connectButton=new JButton("Connect");//this will create the connect button
        connectButton.addActionListener(e->handleConnect());//this will add the action listener to handle the connect button click
        panel.add(connectButton);//adding the connect button to the panel
        disconnectButton=new JButton("Disconnect");//this will create the disconnect button
        disconnectButton.setEnabled(false);//this is when we aren't connected yet
        disconnectButton.addActionListener(e->handleDisconnect());
        panel.add(disconnectButton);
        panel.setBackground(new Color(250,235,245));//pastel pink background for the connection panel
        return panel;
    }
    private JPanel createPostPanel(String[] colors){//this will create the POST panel whre the user will foll out the details in order to make a new note
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Post Note"));// it will create a titled border so user user knows this is the section where the post is supposed to go 
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel("X:"));//this is label for X coordinate
        postXField=new JTextField(4);//this is where user will enter the X coordinate
        panel.add(postXField);
        panel.add(new JLabel("Y:"));//this is label for Y coordinate
        postYField=new JTextField(4);//this is where user will enter the Y coordinate
        panel.add(postYField);
        panel.add(new JLabel("Color:"));//this is label for color so the user know what the dropdown is for
        dropdownColor=new JComboBox<>(colors);//this is where user will select the color from the dropdown
        panel.add(dropdownColor);
        panel.add(new JLabel("Message:"));//this is the label for message so the user know where to type the message
        fieldForMessage=new JTextField(20);//this is where user will type the message 
        panel.add(fieldForMessage);
        postNoteButton=new JButton("POST");//this is the button to post the note
        postNoteButton.setEnabled(false);//it will be disabled until we are connected to the server
        postNoteButton.addActionListener(e->handlePost());//when the user clicks POST it will call the handlePost method
        panel.add(postNoteButton);
        panel.setBackground(new Color(250,240,235));//peach background for the connection panel
        return panel;

    }
    private JPanel createGetPanel(){//this will create the GET panel where the user can get the notes with optional filters
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Get Notes"));//it will create a titled border so user user knows this is the section where the get is supposed to go 
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel("Color:"));//this is label for color
        getColorField=new JTextField(8);//this is where user will enter the color to filter notes
        panel.add(getColorField);
        panel.add(new JLabel("Contains X:"));//add has X label for the position filter
        getHasXField=new JTextField(4);//this is where user will enter the X coordinate to filter notes
        panel.add(getHasXField);
        panel.add(new JLabel("Y:"));//add Y label for contains y filter
        getHasYField=new JTextField(4);//this is where user will enter the Y coordinate to filter notes
        panel.add(getHasYField);
        panel.add(new JLabel("Refers To:"));//this is label for refers to
        getRefersToField=new JTextField(12);//this is where user will enter the refers to message to filter notes
        panel.add(getRefersToField);
        getNoteButton=new JButton("GET Notes");//makes the GET Notes button
        getNoteButton.setEnabled(false);//it will be disabled until we are connected to the server
        getNoteButton.addActionListener(e->handleGetNotes());//when the user clicks GET Note it will call the handleGetNote method
        panel.add(getNoteButton);
        getPinsButton=new JButton("GET Pins");//this is the button to get all pins from the board
        getPinsButton.setEnabled(false);//it will be disabled until we are connected to the server
        getPinsButton.addActionListener(e->handleGetPins());//when the user clicks GET Pins it will call the handleGetPins method
        panel.add(getPinsButton);
        panel.setBackground(new Color(235,240,250));//soft blue background for the connection panel
        return panel;
    }
    private JPanel makePinUnpinPanel(){//this will create the PIN UNPIN panel where the user can pin and unpin notes
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Pin/Unpin Notes"));//add a titled border so user user knows this is the section where the pin unpin is supposed to go 
        panel.setLayout(new FlowLayout());//FlowLayout will arrange the components from left to right
        panel.add(new JLabel("Pin X:"));//this is label for X coordinate to pin
        pinXField=new JTextField(4);//this is where user will enter the X coordinate to pin
        panel.add(pinXField);
        panel.add(new JLabel("Y:"));//this is label for Y coordinate to pin
        pinYField=new JTextField(4);//this is where user will enter the Y coordinate to pin
        panel.add(pinYField);
        pinButton=new JButton("PIN Note");//this is the button to pin a note at the given coordinates
        pinButton.setEnabled(false);//it will be disabled until we are connected to the server
        pinButton.addActionListener(e->handlePin());//when the user clicks PIN Note it will call the handlePin method
        panel.add(pinButton);
        panel.add(new JLabel("  "));//adding some spacer between pin and unpin sections so that they don't look squishy
        panel.add(new JLabel("Unpin X:"));//adding X coordinate to unpin
        unpinXField=new JTextField(4);// where user will enter the X coordinate to unpin
        panel.add(unpinXField);
        panel.add(new JLabel("Y:"));//adding Y coordinate to unpin
        unpinYField=new JTextField(4);// where user will enter the Y coordinate to unpin
        panel.add(unpinYField);
        unpinButton=new JButton("UNPIN Note");//this is the button to unpin a note at the given coordinates
        unpinButton.setEnabled(false);//it will be disabled until we are connected to the server
        unpinButton.addActionListener(e->handleUnpin());//when the user clicks UNPIN Note it will call the handleUnpin method
        panel.add(unpinButton);
        panel.setBackground(new Color(240,235,250));//soft lavender background for the connection panel

        return panel;
    }
    private JPanel createActionsPanel(){//creates the SHAKE and CLEAR buttons panel for board actions
        JPanel panel=new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Board Actions"));//creates a titled border so user user knows this is the section where the board actions are to go 
        panel.setLayout(new FlowLayout());//FlowLayout will arranges everything from left to right
        shakeButton=new JButton("SHAKE Board");//button to shake the board
        shakeButton.setEnabled(false);//disabled until we are connected to the server
        shakeButton.addActionListener(e->handleShake());//when the user clicks SHAKE Board it will call the handleShake method
        panel.add(shakeButton);
        clearButton=new JButton("CLEAR Board");//this is the button to clear the board
        clearButton.setEnabled(false);//it will be disabled until we are connected to the server
        clearButton.addActionListener(e->handleClear());//when the user clicks CLEAR Board it will call the handleClear method
        panel.add(clearButton);
        panel.setBackground(new Color(235,250,240));//soft mint background for the connection panel
        return panel;
    }
    private JPanel createOutputPanel(){//creates the output panel at the bottom where we show the responses by the server
        JPanel panel=new JPanel(new BorderLayout());//creates a panel with BorderLayout so that we can full it with the text area
        panel.setBorder(BorderFactory.createTitledBorder("Server Output"));//adds a titled border so user knows this is where the output section is
        panel.setPreferredSize(new Dimension(900,200));
        outputArea=new JTextArea();//makes the text area where we will show server responsesy
        outputArea.setEditable(false);//make it the way so that the user isn't able to edit the it
        outputArea.setFont(new Font("Roman", Font.PLAIN, 13));//sets a font so that everything lines up nicely
        outputArea.setBackground(new Color(255,250,245));//sets a pistachio green background color for the output area
        outputArea.setForeground(new Color(150,100,150));//sets a purple text color
        outputArea.setMargin(new Insets(5,10,5,10));//adds some margin so that the text isn't squished against the edges
        JScrollPane outputScroll=new JScrollPane(outputArea);//wraps the text area in a scroll pane so that the user can scroll the output as well
        panel.add(outputScroll,BorderLayout.CENTER);
        return panel;
    }
    private void handleConnect(){//will handle when the user clicks the connect button
        String ip=ipField.getText().trim();//it will get the IP and removes spaces
        String portText=portField.getText().trim();//it will get the port and removes spaces
        try {
            int port=Integer.parseInt(portText);//try to convert the port to an integer and will throw an error if it is not a number
            String output=serverLink.connectToServer(ip,port);//use ServerLink to try connecting to the server with the given IP and port
            showOutput(output);
            if(serverLink.isConnected()){//checks if we are actually connected now
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
                enableAllButtons(true);//turns on all the command buttons as we are now connected
                if(serverLink.getColorsToUse()!=null){//update colors from the server
                    dropdownColor.removeAllItems();
                    for(String color:serverLink.getColorsToUse()){//then we will loop through all the colors that server supports
                        dropdownColor.addItem(color);//and add each color to the dropdown
                    }
            }
        } }catch (NumberFormatException e) {
            showOutput("ERROR: Port must be a number.");
        }
    }private void handleDisconnect(){//handles when the user clicks the disconnect button
        String output=serverLink.disconnectFromTheServer();//it will disconnect from the server properly
        showOutput(output);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        enableAllButtons(false);//turns off all the command buttons as we are now disconnected

    }private void handlePost(){//handles when the user clicks the POST button
        String xText=postXField.getText().trim();//gets the X coordinate from the text field
        String yText=postYField.getText().trim();//gets the Y coordinate from the text field
        String color=(String)dropdownColor.getSelectedItem();//gets the selected color from the dropdown
        String message=fieldForMessage.getText().trim();//gets the message and removes spaces
        try {
            int x=Integer.parseInt(xText);//tries to convert the X coordinate to an integer will throw an error if those are not numbers
            int y=Integer.parseInt(yText);//tries to convert the Y coordinate to an integer
            //we will let the server do the validation of coordinates, message and boundaries
            String command=String.format("POST %d %d %s %s",x,y,color,message);//creates the POST command to send to the server
            String output=serverLink.sendCommand(command);//sends the command to the server and gets the response
            showOutput("Sent: "+command+"\nReceived: "+output);//shows what we sent and what server replied
            postXField.setText("");//clears the X field after posting
            postYField.setText("");//clears the Y field after posting
            fieldForMessage.setText("");//clears the message field after posting
        }catch (NumberFormatException e) {
            showOutput("ERROR: X and Y coordinates must be numbers.");
        }
    }
    private void handleGetNotes(){//creates the handles when user will click and GET notes button
        StringBuilder command=new StringBuilder("GET");//starts building the GET command
        String color=getColorField.getText().trim();//gets the color filter from the text field
        String HasX=getHasXField.getText().trim();//gets the contains X filter
        String HasY=getHasYField.getText().trim();
        String refersTo=getRefersToField.getText().trim();//gets the refers to filter
        if(!color.isEmpty()){//if the color filter is provided we will add it to the command
            command.append(" color=").append(color);
        }if(!HasX.isEmpty()&& !HasY.isEmpty()){//if the user typed both the x and y for contains add it as the filter
            command.append( " contains=").append(HasX).append(" ").append(HasY);
        }if(!refersTo.isEmpty()){//if the user typed something in refers to and add it as a filter 
            command.append(" refersTo=").append(refersTo);
        }
        String result=serverLink.sendCommand(command.toString());//sends the GET command to the server and gets the response
        showOutput("Sent: "+command.toString()+"\nReceived: "+result);//shows what we sent and what server replied
    }private void handleGetPins(){//handles when user clicks the GET Pins button
        String result=serverLink.sendCommand("GET PINS");//sends the GET PINS command to the server and gets the response
        showOutput("Sent: GET PINS\n"+result);//shows what we sent and what server replied
    }private void handlePin(){//handles when user clicks the PIN button
        String xText=pinXField.getText().trim();//gets the X coordinate from the text field
        String yText=pinYField.getText().trim();
        try {
            int x=Integer.parseInt(xText);//tries to convert the x and y to numbers
            int y=Integer.parseInt(yText);
            String command=String.format("PIN %d %d",x,y);//creates the PIN command to send to the server
            String output=serverLink.sendCommand(command);//sends the command to the server and gets the response
            showOutput("Sent: "+command+"\nReceived: "+output);//shows what we sent and what server replied
            pinXField.setText("");//clears the X field after pinning
            pinYField.setText("");//clears the Y field after pinning
        }catch (NumberFormatException e) {
            showOutput("ERROR: X and Y coordinates must be numbers.");
        }
    }
    private void handleUnpin(){//handles when user clicks the UNPIN button
        String xText=unpinXField.getText().trim();//gets the X coordinate from the text field
        String yText=unpinYField.getText().trim();
        try {
            int x=Integer.parseInt(xText);//tries to convert the x and y to numbers
            int y=Integer.parseInt(yText);
            String command=String.format("UNPIN %d %d",x,y);//creates the UNPIN command to send to the server
            String output=serverLink.sendCommand(command);//sends the command to the server and gets the response
            showOutput("Sent: "+command+"\nReceived: "+output);//shows what we sent and what server replied
            unpinXField.setText("");//clears the X field after unpinning
            unpinYField.setText("");//clears the Y field after unpinning
        }catch (NumberFormatException e) {
            showOutput("ERROR: X and Y must be numbers.");
        }
    }private void handleShake(){//handles when user clicks the SHAKE button
        String output=serverLink.sendCommand("SHAKE");//sends the SHAKE command to the server and gets the response
        showOutput("Sent: SHAKE\nReceived: "+output);//shows what we sent and what server replied
    }private void handleClear(){//handles when user clicks the CLEAR button 
        String output=serverLink.sendCommand("CLEAR");//sends the CLEAR command to the server and gets the response
        showOutput("Sent: CLEAR\nReceived: "+output);//shows what we sent and what server replied
    }private void showOutput(String text){//this will show the given message in the output area   
        outputArea.append(">"+text+"\n\n");//appends the text with a > symbol at the start and adds the new lines after
        outputArea.setCaretPosition(outputArea.getDocument().getLength());//moves the cursor to the bottom so it auto scroll to show the lastest output
    }
    private void enableAllButtons(boolean enable){//this will enable or disable all the command buttons based on the parameter
        postNoteButton.setEnabled(enable);
        getNoteButton.setEnabled(enable);
        getPinsButton.setEnabled(enable);
        pinButton.setEnabled(enable);
        unpinButton.setEnabled(enable);
        shakeButton.setEnabled(enable);
        clearButton.setEnabled(enable);
    }
    public static void main(String[] args) {//main method that will run to start the program
        SwingUtilities.invokeLater(()->new ClientPanel());//this will start the client panel in the event dispatch thread
    }
}

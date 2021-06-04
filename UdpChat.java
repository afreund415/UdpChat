
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #1
*/

import org.json.JSONObject;
import java.util.Scanner;


public class UdpChat {

    //instance variables
    static Server server; 
    static Client client;
    static boolean running = true;
    static int clientPort = 3000;
    static String clientName = "Unknown";
    static int serverPort = 3001;  
    static String serverAddr = null;

    public static void main(String[] args) {
        argParse(args, "");
        Scanner input = new Scanner(System.in);
        
        while (running){

            //allows for continous message sending
            System.out.print(">>> ");
            String line = input.nextLine();
            String[] newArgs = line.split(" ");

            argParse(newArgs, line);
            
        }
        input.close();

        //clean server + client shut down
        System.out.println("Shutting down");
        if (client != null){
            client.stop();
        }

        if (server !=null){
            server.stop();
        }      
    }

    //method for detecting commandline args 
    private static void argParse(String[] args, String line){
        int pos = 0;

        try{
            //switch method for processing CL args
            while (pos < args.length){
                
                switch(args[pos].toLowerCase()) {
                    //client commandline case
                    case "-c":
                        if (args.length - pos <= 4){
                            Message.printError("Enter -c <user-name> " +
                            "<server-ip> <server-port> <client-port> " + 
                                                "to create client");
                            return;    
                        }
                        //**what does this handle exactly?**
                        if (client != null){
                            client.stop();
                        }

                        //client constructor variables from args    
                        clientName = args[++pos];
                        serverAddr = args[++pos];
                        serverPort = Integer.parseInt(args[++pos]);
                        clientPort = Integer.parseInt(args[++pos]);
                        
                        //creates client w/ args
                        client = new Client(clientName, serverAddr, 
                                            serverPort, clientPort);
                        break; 
                   
                    //server commandline case    
                    case "-s":
                        if ((args.length - pos <= 1)){
                            Message.printError("Enter -s <port> to" +  
                                                " create server");
                            return;   
                        }
                        if (server != null){
                            server.stop();
                        }
                        //creates new server
                        server = new Server(Integer.parseInt(args[++pos]));
                        
                        break;
                    //send case for client
                    case "send":
                        if ((args.length - pos <= 2)){
                            Message.printError("To send a message, enter " +
                                                "send <name> <message>");       
                            return;
                        }
                        //checks if sender is an actual client 
                        if (client == null){
                            Message.printError("Instance is not running " + 
                                                "in client mode");
                            return;
                        }
                        //creating message variables
                        //name is the recipient name
                        String name = args[++pos].toLowerCase();
                        String chatLine;
                        //looks up recipient
                        JSONObject recipient = client.findUser(name);
                        JSONObject msgChat = new JSONObject();

                        //checks if recipient is real
                        if (recipient == null){
                            Message.printError(name + " does not exist");
                            return;
                        }

                        //if passed an empty line, sets chatline to args
                        if (line.isEmpty()){
                            chatLine = args[++pos];
                        }

                        //starts the message after username position
                        else{
                            chatLine = line.substring(line.indexOf(args[pos])
                                                      + name.length() + 1);
                            pos = args.length;
                        }
                        //adding information to the message
                        msgChat.put("text", chatLine);
                        msgChat.put("from", client.uName);
                        msgChat.put("name", name);
                        //client send if recipient online
                        if (recipient.optBoolean("online")){
                            client.sendMessage(msgChat, 
                                               recipient.getString("addr"),
                                               recipient.getInt("port"), "chat");    
                        }
                        //otherwise, send message to server for offline chat
                        else{
                            client.sendMessage(msgChat, client.sAddr,
                                                client.sPort, "chat");
                        }
                        break;
                    //register case
                    case "reg":
                        if (args.length - pos >= 2){
                            clientName = args[++pos];
                        }
                        if (serverAddr == null){
                            Message.printError("Server never specified");
                            return;
                        }
                        //again not sure what this does
                        if (client != null){
                            client.stop();
                        }
                        //creates new client w/ constructor
                        client = new Client(clientName, serverAddr, 
                                            serverPort, clientPort);
                    
                        break;
                     //deregister case
                     case "dereg":
                        if (client!=null){
                            client.stop();
                            Message.printMessage("You are Offline. Bye.");
                            //sets client to null to avoid issues w/ offline send etc.
                            client = null;
                        }
                        break;    
                    //quit commandline case
                    case "quit":
                    case "-q":
                        running = false;
                        break;
                    //empty line case
                    case "":
                        return;
                    default: 
                        Message.printError("Unknown command");
                    }
                pos++;
            }
        }
        catch (Exception e){
            Message.printError(e.getMessage());
        }   
    }
}
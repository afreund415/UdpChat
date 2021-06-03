
//import java.net.*;  
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

            System.out.print(">>> ");
            String line = input.nextLine();
            String[] newArgs = line.split(" ");

            argParse(newArgs, line);
            
        }
        input.close();

        //clean shut down
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
            //**changed to while equal DELETE**
            while (pos < args.length){
                
                switch(args[pos].toLowerCase()) {
                    //client commandline case
                    case "-c":
                        if (args.length - pos <= 4){
                            Message.printError("Not enough args to create client");
                            return;    
                        }
                        if (client != null){
                            client.stop();
                        }

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
                            Message.printError("Not enough args to create server");
                            return;   
                        }
                        if (server != null){
                            server.stop();
                        }
                        //creates new server
                        server = new Server(Integer.parseInt(args[++pos]));
                        
                        break;
                    //send case
                    case "send":
                        if ((args.length - pos <= 2)){
                            Message.printError("To send a message, include the " +
                                        "recepient name and message");       
                            return;
                        }
                        //checks if sender is an actual client 
                        if (client == null){
                            Message.printError("Instance is not running in client mode");
                            return;
                        }
                        //creating message
                        String name = args[++pos].toLowerCase();
                        String chatLine;
                        JSONObject user = client.findUser(name);
                        JSONObject msgChat = new JSONObject();

                        if (user == null){
                            Message.printError(name + " does not exist");
                            return;
                        }
                        

                        if (line.isEmpty()){
                            chatLine = args[++pos];
                        }

                        else{
                            chatLine = line.substring(line.indexOf(name) + name.length() + 1);
                            pos = args.length;
                        }

                        msgChat.put("text", chatLine);
                        msgChat.put("from", client.uName);
                        msgChat.put("name", name);
                        //client send if user online
                        if (user.optBoolean("online")){
                            client.sendMessage(msgChat, user.getString("addr"), user.getInt("port"), "chat");    
                        }
                        //otherwise, send message to server
                        else{
                            client.sendMessage(msgChat, client.sAddr, client.sPort, "chat");
                        }
                        break;
                    //deregister case
                    case "dereg":
                        if (client!=null){
                            client.stop();
                            Message.printMessage("You are Offline. Bye.");
                            client = null;
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
                        if (client != null){
                            client.stop();
                        }
                        client = new Client(clientName, serverAddr, 
                                            serverPort, clientPort);
                    
                        break;

                    //quit commandline case
                    case "-q":
                        running = false;
                        break;
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
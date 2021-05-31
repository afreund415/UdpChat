
//import java.net.*;  
import org.json.JSONObject;
import java.util.Scanner;


public class UdpChat {

    //instance variables
    static Server server; 
    static Client client;
    static boolean running = true;

    public static void main(String[] args) {
        argParse(args);
        
        //message testing code
        // for (int i = 0; i < 10; i++){
        //     JSONObject message = new JSONObject();
        //     message.put("text", "Hello andreas " + i);

        //     client.sendMessage(message, client.sAddr, client.sPort, "chat");
            
        // }
        Scanner input = new Scanner(System.in);
        while (running){

            String s = input.nextLine();
            String[] newArgs = s.split(" ");

            argParse(newArgs);
            
        }
        input.close();

        if (client != null){
            client.stop();
        }

        if (server !=null){
            server.stop();
        }
        
    }

    //method for detecting commandline args 
    private static void argParse(String[] args){

        int pos = 0;
        //try and catch errors here

        try{

            while (pos < args.length){

                
                switch(args[pos].toLowerCase()) {
                    //client commandline case
                    case "-c":
                        if (args.length - pos <= 4){
                            errorHandler("Not enough args to create client");
                            return;    
                        }
                        if (client != null){
                            client.stop();
                        }
                        //creates client w/ args
                        client = new Client(args[++pos], args[++pos], 
                                            (Integer.parseInt(args[++pos])),
                                            (Integer.parseInt(args[++pos])));
                        break; 
                    //server commandline case    
                    case "-s":
                        if ((args.length - pos <= 1)){
                            errorHandler("Not enough args to create server");
                            return;   
                        }
                        if (server != null){
                            server.stop();
                        }
                        //creates new server
                        server = new Server(Integer.parseInt(args[++pos]));
                        
                        break;
                    //send <name> <message>.
                    case "send":
                        if ((args.length - pos <= 2)){
                            errorHandler("To send a message, include the " +
                                        "recepient name and message");       
                            return;
                        }
                        //checks if sender is an actual client 
                        if (client == null){
                            errorHandler("Instance is not running in client mode");
                            return;
                        }
                        String name = args[++pos].toLowerCase();
                        JSONObject user = client.findUser(name);
                        JSONObject msgChat = new JSONObject();
                        msgChat.put("text", args[++pos]);
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
                        //1. look up name a. if no match, return error
                        //2. user offline, send message to server for storage 
                        //3. if user online, send message to user 
                        
                    //quit commandline case
                    case "-q":
                        running = false;
                        break;
                    default: 
                        errorHandler("Unknown command");
                    }
                pos++;
            }
        }
        catch (Exception e){
            errorHandler(e.toString());
        }   
    }

    private static void errorHandler(String s){
        System.out.println("Error " + s);
    }
}
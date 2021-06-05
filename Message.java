
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #1
*/

import java.net.*;
import java.text.SimpleDateFormat;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Date;
import java.util.UUID;
//import java.util.concurrent.TimeoutException;


public abstract class Message{

    //instance variables
    int port;
    Send send; 
    Receive receive; 
    boolean running = true;
    static boolean stopApp = false;
    static boolean debug = false;
    DatagramSocket ds;
    JSONArray messageQueue = new JSONArray();
    //tag for debugging threads
    String tag;
    JSONObject clientTable = new JSONObject(); 
    

    //constructor method for classes 
    public Message(int port, String tag) throws Exception{
        
        this.port = port;
        //**tag for debugging thread handling*
        this.tag = tag;
        send = new Send();
        receive = new Receive();
        //shared socket and sending and receiving on same port
        ds = new DatagramSocket(port);
        //allows receive thread to breath
        ds.setSoTimeout(500);
        //starting threads
        receive.start();
        send.start();
    }

    //stop thread helper method
    public void stopMessages(){
        running = false;
        //makes sure to allow send & receive threads to finish
        while(send.isAlive() && receive.isAlive()){
            sleepMs(100);
        }
        try{
            ds.close();
        }
        catch(Exception e){};
    }

    //time delay helper method
    public void sleepMs(int i){
        try{
            Thread.sleep(i);
        }
        catch(Exception e){
        }
    }

    //method for locating messages in message queue if needed
    synchronized public JSONObject findMessage(String id){
        JSONObject msg = null; 

        for (int i = 0; i < messageQueue.length(); i++){
            msg = messageQueue.optJSONObject(i);
            if (msg != null && msg.getString("id").equals(id)){
                return msg;
            }
        } 
        return null;
    }

    //message creator method that adds messages to JSON message queue 
    synchronized public void sendMessage(JSONObject msg, String addr, int port,
                                        String type){
        //unique ID for ensuring correct sending of messages
        String id = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        //adding necessary fields to message obj
        msg.put("id", id);
        msg.put("addr", addr);
        msg.put("port", port);
        msg.put("type", type);
        msg.put("date", formatter.format(date));

        //add message to queue
        messageQueue.put(msg);
    }

    //ACK method for ensuring successful delivery of messages
    synchronized public void sendACK(JSONObject msg, String addr, int port){
        
        try{
            //ensuring ACKs are not sent in response to other ACKs
            if (msg.getString("type").equals("ack")){
                return;
            }
            //creat ACK obj
            JSONObject ack = new JSONObject();
            //add correct ACK msg fields
            ack.put("addr", addr);
            ack.put("port", port);
            ack.put("type", "ack");
            //creating string from unique ID 
            String id = msg.getString("id");
            ack.put("id", id);

            //setting destination address + port #
            InetAddress ip; 
            ip = InetAddress.getByName(ack.getString("addr"));
            int p = ack.getInt("port");

            //Creating string from ACK msg
            String str = ack.toString();
           
            //creating UDP packet 
            DatagramPacket dp = new DatagramPacket(str.getBytes(), 
                                    str.length(), ip, p);
            //send ACK (no delay)
            ds.send(dp);
        }
        catch(Exception e){
            printError(e.getMessage());
        }   
    }
    
    //receive abstract ack method 
    abstract void recvACK(JSONObject msgAck, String addr, int port);

    //receive message (abstract for server + client overload)
    abstract void recvMsg(JSONObject msg, String addr, int port);
    
    //send chat fail message (abstract for server + client overload)
    abstract void sendFail(JSONObject msg, String addr, int port);

    //user lookup w/ string
    public JSONObject findUser(String name){
        JSONObject user = clientTable.optJSONObject(name.toLowerCase());

        return user;
    }
    //user lookkup w/ JSON obj
    public JSONObject findUser(JSONObject msg){
        String name = msg.optString("name");

        return findUser(name);
    }

    //send thread 
    class Send extends Thread{

        public void run(){
            //debug statement
            printDebug("Send Thread started " + tag);
            InetAddress ip; 
            
            while(running || messageQueue.length() > 0){
                try{
                    if (!(messageQueue.isEmpty())){
                        //sets message to first message in queue
                        JSONObject message = messageQueue.optJSONObject(0);

                        if (message != null){
                            //**create debug string from JSON Obj msg*
                            String str = message.toString();
                            //getting destination address + port
                            String addr = message.getString("addr");
                            ip = InetAddress.getByName(addr);
                            int p = message.getInt("port");
                            //creating UDP packet
                            DatagramPacket dp = new DatagramPacket(
                                                    str.getBytes(), 
                                                    str.length(), ip, p);
                            //count for sending attempts
                            int count = 0;
                            while(count < 5){
                                //send msg then wait 500ms
                                ds.send(dp);
                                sleepMs(500);
                                //if ACK added to msg object, sending
                                //was successful and can exit inner while loop
                                if (message.optBoolean("ack",false)){
                                    if(message.optString("type").equals("dereg")){
                                        printMessage("Deregistration complete");
                                    }
                                    break;    
                                }
                                //alert user that system is attempting resend
                                else { 
                                    printDebug("Resending");
                                }
                                count++;
                            }
                            //remove msg from queue 
                            messageQueue.remove(0);
                            //if msg failed to send, alert user
                            if (count == 5){
                                sendFail(message, addr, p);
                            }
                        }
                    }
                }
                catch(Exception e){
                    printError(e.getMessage());
                }
                //10ms timeout to wait for receiver thread
                sleepMs(10);
            }
            //debug statement
            printDebug("Send Thread stopped " + tag);
        } 
    }


    //Receive thread
    class Receive extends Thread{

        public void run(){
            //**debug statement*
            printDebug("Receive Thread started " + tag);
            //set max # of bytes receiver can receive
            byte[] buf = new byte[1024]; 
            
            while(running || send.isAlive()){
                try{
                    //create new empty packet and open socket for receiving
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);  
                    ds.receive(dp); 
                    if (dp.getLength() > 0){           
                        //set address and port 
                        String addr = dp.getAddress().getHostAddress();
                        int p = dp.getPort();
                        //create string and JSON obj from received message
                        String str = new String(dp.getData(), 0, dp.getLength());  
                        JSONObject message = new JSONObject(str);

                        //debug print entire message obj
                        printDebug(tag + ": " + str);  
                        
                        //if system receives ACK, checks to see if unique 
                        //ids are the same on ACK and message 
                        if (message.getString("type").equals("ack")){

                            String ackId = message.getString("id");                    
                            JSONObject msg = findMessage(ackId);

                            //if ids match, set msg ACK to true
                            //this enables sender to remove msg from queue
                            if (msg != null && msg.getString("id").equals(ackId)){
                                msg.put("ack", true);
                                recvACK(msg, addr, p);
                            }
                            else{
                                printDebug(message + " not found");
                            }  
                        }
                        else{
                            //call send ACK with msg, addr, and port 
                            sendACK(message, addr, p);
                            recvMsg(message, addr, p);
                        }
                    }
                }
                catch(SocketTimeoutException e){
                }
                catch(Exception e){
                    printError("receive thread" + e.getMessage());
                }
            }
            //debug statement
            printDebug("Receive Thread stopped " + tag);
        } 
    }

    //prints messages
    public static void printMessage(String s){
        System.out.println(s);
        System.out.print(">>> ");
    }
    //prints error messages
    public static void printError(String s){
        printMessage("Error " + s);
    }
    //print debug messages
    public static void printDebug(String s){
        if (debug){
            printMessage("Debug: " + s);
        }
    }
}
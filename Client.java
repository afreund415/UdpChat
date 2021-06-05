
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #1
*/

import org.json.JSONObject;
import org.json.JSONArray;


public class Client extends Message{

    String uName;
    String sAddr;
    int sPort;
    boolean userTableShown = false;
   
    //Constructor method for client
    public Client(String uName, String sAddr, int sPort, int port) throws Exception{
        super(port, "client"); 
        this.uName = uName; 
        this.sAddr = sAddr; 
        this.sPort = sPort;
        JSONObject msgReg = new JSONObject();
        msgReg.put("name", uName);
        sendMessage(msgReg, sAddr, sPort, "reg");
    }

    public void recvMsg(JSONObject msg, String addr, int port){
        String type = msg.getString("type");

        switch(type){
            //handles table updates 
            case "table":
                clientTable = msg.getJSONObject("table");
                JSONArray users = clientTable.names();
                //loop for sending updated table to online users
                if (!userTableShown){
                    for (int i = 0; i < users.length(); i++){
                        String userName = users.optString(i);
                        JSONObject user = clientTable.optJSONObject(
                                                        userName);
                        if (user != null && !userName.equals(uName.toLowerCase())){
                            printMessage(user.optString("name") + " is " + 
                                (user.optBoolean("online")?"online":"offline"));
                        }
                    }
                    userTableShown = true;
                }
                else{
                    printMessage(msg.optString("name") + " is " + 
                        (msg.optBoolean("online")?"online":"offline"));
                } 
                break;
            //prints chat messages case
            case "chat":
                String from = msg.optString("from");
                String text = msg.optString("text");
                String date = msg.optString("date");

                printMessage(date + " - " + from + ": " + text);
                break;
            //handles registration errors (ie duplicate user name)
            case "regresult":
                if (msg.optBoolean("success")){
                    printMessage("Welcome " + uName + "! You are registered");
                }
                else {
                    printMessage(msg.optString("text"));
                    super.stopMessages();
                } 
                break;
            default:
                printError("Unknown message received");
        }
    }

    public void recvACK(JSONObject msg, String addr, int port){
        String type = msg.optString("type");
        String recipient = msg.optString("name");

        if (type.equals("chat")){
            if (msg.optBoolean("viaserver")){
                printMessage("Message received and saved by server");       
            }
            else {
                printMessage("Message received by " + recipient);
            }
        }
    }

    //client send fail method, invokes offline chat flow
    public void sendFail(JSONObject msg, String addr, int port){

        JSONObject user = findUser(msg);

        if (user!=null){

            if (msg.optString("type").equals("chat")){
                user.put("online", false);
                printMessage("No ACK received after timeout and 5 retries");
            
                //checks to see if message was originally sent to server
                if (msg.getString("addr").equals(sAddr) && msg.getInt("port") == sPort){
                    printMessage("Server not responding");
                    printMessage("Exiting");
                    stopApp = true;
                    UdpChat.shutdown();
                    return;
                }
                //resends message to server if msg was not originally sent to server 
                //for offline handling
                sendMessage(msg, sAddr, sPort, "chat");
            }
            else if (msg.optString("type").equals("dereg")){
                printMessage("Server not responding");
                printMessage("Exiting");
                stopApp = true;
            }   
        }
    }

    //stop method for client
    public void stopMessages(){
        JSONObject msgDereg = new JSONObject();
        msgDereg.put("name", uName);
        sendMessage(msgDereg, sAddr, sPort, "dereg");
        printMessage("Attempting deregistration for " + uName);
        super.stopMessages();
    }
}

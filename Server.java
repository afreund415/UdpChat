
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #1
*/

import org.json.JSONObject;
import org.json.JSONArray;

public class Server extends Message{

    //table of users + status
    JSONArray table = new JSONArray(); 
    JSONObject offlineMsgs = new JSONObject();

    //constructor method
    public Server(int port) throws Exception{
        super(port, "server");
        printMessage("The server is running on port " + port );
    }
    
    //server send fail method that invokes offline chat
    public void sendFail(JSONObject msg, String addr, int port){

        JSONObject user = findUser(msg);

        if (user!=null){
            String name = msg.optString("name"); 
            user.put("online", false);
            
            if (msg.optString("type").equals("chat")){
                storeChatOffline(msg);
            }
            updateClients(name, false);
        }
    }

    //stores chat messages sent to offline users
    public void storeChatOffline(JSONObject msg){
        String name;
        JSONArray userMsgs;

        name = msg.optString("name").toLowerCase(); 
        userMsgs = offlineMsgs.optJSONArray(name);
        if (userMsgs==null){
            userMsgs = new JSONArray();
            offlineMsgs.put(name, userMsgs);                        
        }
        userMsgs.put(msg);
    }


    //server receive message method, switches based on msg type
    public void recvMsg(JSONObject msg, String addr, int port){
        String type = msg.getString("type");
        String name;
        JSONArray userMsgs;

        switch(type){
            case "reg":
                //creates case-agnostic key for clientTable
                name = msg.getString("name"); 
                JSONObject user = findUser(name.toLowerCase());
                JSONObject msgRegResult = new JSONObject();
                //checks if user name is taken or not
                if (user != null && user.optBoolean("online")){
                    msgRegResult.put("success", false);
                    msgRegResult.put("text", name + " username is taken");
                    sendMessage(msgRegResult, addr, port, "regresult");
                    return;
                }
                msgRegResult.put("success", true);
                msgRegResult.put("text", name + " is registered");
                sendMessage(msgRegResult, addr, port, "regresult");
                //adds addr, port, and online status to msg
                msg.put("addr", addr);
                msg.put("port", port);
                msg.put("online", true);
                //adds key(by name) to table and contact info
                clientTable.put(name.toLowerCase(), msg); 
                //broadcasting updated table to all online users
                updateClients(name, true);
                //check for offline messages
                userMsgs = offlineMsgs.optJSONArray(name.toLowerCase());

                if (userMsgs!=null){
                    JSONObject greeting = new JSONObject();
                    greeting.put("text", "Welcome back! You got mail.");
                    greeting.put("from", "Chat Server");
                    sendMessage(greeting, addr, port, "chat");
                    for (int i = 0; i < userMsgs.length(); i++){
                        JSONObject oldMsg = userMsgs.getJSONObject(i);
                        sendMessage(oldMsg, addr, port, "chat");
                    }
                    offlineMsgs.remove(name.toLowerCase());
                }
                break;
            case "dereg":
                name = msg.getString("name");
                JSONObject client = clientTable.getJSONObject(name.toLowerCase());
                client.put("online", false);
                updateClients(name, false);
                break;
            case "chat":
                storeChatOffline(msg);
                break;
            default:
                printError("Unknown message received");
        }
    }
    
    //abstract implementation
    public void recvACK(JSONObject msgAck, String addr, int port){}

    //updates table and broadcasts to clients 
    private void updateClients(String name, Boolean online){
        //users are case-agnostic (see recvMessg)
        JSONArray users = clientTable.names();

        //loop for sending updated table to online users
        for (int i = 0; i < users.length(); i++){
            JSONObject msgTable = new JSONObject();
            //adds reference to clientTable to msgTables
            msgTable.put("table", clientTable); 
            msgTable.put("name", name);
            msgTable.put("online", online);
            String user = users.getString(i);
            JSONObject dest = clientTable.getJSONObject(user);
                
            if(dest.optBoolean("online")){
                //sends updated table to each online user    
                printDebug("updateClients " + user + " " + dest.toString());
                sendMessage(msgTable, dest.getString("addr"),
                            dest.getInt("port"), "table");
            }

        }
    }

}

import org.json.JSONObject;
import org.json.JSONArray;

public class Server extends Message{

    //table of users + status
    JSONArray table = new JSONArray(); 
    JSONObject offlineMsgs = new JSONObject();

    //UdpChat -s <port>
    public Server(int port) throws Exception{
        super(port, "server"); 
        
    }
    //update table could fail, forwarding message could fail 
    //currently logs out any user that receives failed message
    public void sendFail(JSONObject msg, String addr, int port){

        JSONObject user = findUser(msg);

        if (user!=null){
            user.put("online", false);
            //assumes user has registered
            if (msg.optString("type").equals("chat")){
                storeChatOffline(msg);
            }
            updateClients();
        }
    }

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


    //msg = message, addr = return addr, port = return port
    public void recvMsg(JSONObject msg, String addr, int port){
        String type = msg.getString("type");
        String name;

        JSONArray userMsgs;


        switch(type){
            case "reg":
                //adds addr, port, and online status to msg
                msg.put("addr", addr);
                msg.put("port", port);
                msg.put("online", true);
                //creates case-agnostic key for clientTable
                name = msg.getString("name").toLowerCase();
                //adds key(by name) to table and contact info
                clientTable.put(name, msg); 
                //helper method for broadcasting updated table 
                //to all online users
                updateClients();

                //check for offline messages
                userMsgs = offlineMsgs.optJSONArray(name);

                if (userMsgs!=null){
                    for (int i = 0; i < userMsgs.length(); i++){
                        JSONObject oldMsg = userMsgs.getJSONObject(i);
                        sendMessage(oldMsg, addr, port, "chat");
                    }
                    offlineMsgs.remove(name);
                }

                break;

            case "dereg":
                name = msg.getString("name").toLowerCase();
                JSONObject client = clientTable.getJSONObject(name);
                client.put("online", false);
                updateClients();
                break;
            case "chat":
                storeChatOffline(msg);
                break;
            default:
                printError("Unknown message received");
        }
    }

    private void updateClients(){
        //users are case-agnostic (see recvMessg)
        JSONArray users = clientTable.names();
        JSONObject msgTable = new JSONObject();
        //adds reference to clientTable to msgTables
        msgTable.put("table", clientTable); 

        //loop for sending updated table to online users
        for (int i = 0; i < users.length(); i++){
            String user = users.getString(i);
            JSONObject dest = clientTable.getJSONObject(user);
            
            
            if(dest.optBoolean("online")){
                //sends updated table to each online user    
                sendMessage(msgTable, dest.getString("addr"),
                            dest.getInt("port"), "table");
            }

        }
    }

}

import org.json.JSONObject;
import org.json.JSONArray;


public class Client extends Message{

    String uName;
    String sAddr;
    int sPort;
   
    //sendMessage(JSONObject m, String addr, int port, String type){
    //UdpChat -c <nick-name> <server-ip> <server-port> <client-port>
    public Client(String uName, String sAddr, int sPort, int port) throws Exception{
        super(port, "client"); 
        this.uName = uName; 
        this.sAddr = sAddr; 
        this.sPort = sPort;
        JSONObject msgReg = new JSONObject();
        msgReg.put("name", uName);
        sendMessage(msgReg, sAddr, sPort, "reg");
        System.out.println("Welcome, You are registered");
    }

    public void recvMsg(JSONObject msg, String addr, int port){
        String type = msg.getString("type");

        switch(type){
            case "table":
                clientTable = msg.getJSONObject("table");
                break;
            case "chat":
                //print out message 
                String from = msg.optString("from");
                String text = msg.optString("text");
                System.out.println(from + ": " + text);
                break;
            default:
                System.out.println("Unknown message received");
        }
    }

    public void sendFail(JSONObject msg, String addr, int port){

        JSONObject user = findUser(msg);

        if (user!=null && msg.optString("type").equals("chat")){
            user.put("online", false);
            //checks to see if message was originally sent to server
            if (msg.getString("addr").equals(sAddr) && msg.getInt("port") == sPort){
                return;
            }
            //resends message to server if msg was not originally sent to server 
            //for offline handling
            sendMessage(msg, sAddr, sPort, "chat");
        }
    }

    public void stop(){
        JSONObject msgDereg = new JSONObject();
        msgDereg.put("name", uName);
        sendMessage(msgDereg, sAddr, sPort, "dereg");
        System.out.println(uName + " deregistered");
        //shut down sequence not correct. Can't just shut down without server ack
        super.stop();
    }

}

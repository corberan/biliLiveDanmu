import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by liuzc on 2016/3/2.
 */

public class server_client {

    private final String CID_INFO_URL = "http://live.bilibili.com/api/player?id=cid:";
    private final String DEFAULT_COMMENT_HOST = "livecmt-1.bilibili.com";
    private final int DEFAULT_COMMENT_PORT = 788;
    private final int PROTOCOL_VERSION = 1;
    public final int RECEIVE_BUFFER_SIZE = 10 * 1024;
    private Timer heartBeattimer;

    public String getRealRoomID(String roomID){
        try {
            String html = util.httpGet("http://live.bilibili.com/" + roomID);
            String realRoomID = util.getStrBetween(html, "var ROOMID = ", ";");
            if (realRoomID != null && realRoomID.matches("\\d+")){
                return realRoomID;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return roomID;
    }

    public String getSocketServerUrl(String roomID){
        try {
            String html = util.httpGet(CID_INFO_URL + roomID);
            String serverUrl = util.getStrBetween(html, "<server>", "</server>");
            if (serverUrl != null){
                return serverUrl;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return DEFAULT_COMMENT_HOST;
    }

    public boolean sendSocketData(Socket socket, int total_len, int head_len, int version, int action, int param5, byte[] data){
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeInt(total_len);
            out.writeShort(head_len);
            out.writeShort(version);
            out.writeInt(action);
            out.writeInt(param5);
            if (data != null && data.length > 0) out.write(data);
            out.flush();
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public boolean sendJoinRoomMsg(Socket socket, String roomID){
        long uid = 1000000000 + (long)(2000000000 * Math.random());
        String jsonBody = "{\"roomid\": " + roomID + ", \"uid\": " + uid + "}";
        try {
            return sendSocketData(socket, jsonBody.length() + 16, 16, PROTOCOL_VERSION, 7, 1, jsonBody.getBytes("utf-8"));
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public class sendHeartbeat extends TimerTask {
        private Socket socket;
        public sendHeartbeat(Socket sock){
            this.socket = sock;
        }
        public void run(){
            if (!sendSocketData(this.socket, 16, 16, PROTOCOL_VERSION, 2, 1, null)){
                this.cancel();
            }
        }
    }

    public Socket connect(String roomID){
        String realRoomID = getRealRoomID(roomID);
        String socketServerUrl = getSocketServerUrl(realRoomID);
        Socket socket = null;
        InetSocketAddress address = new InetSocketAddress(socketServerUrl, DEFAULT_COMMENT_PORT);
        try {
            socket = new Socket();
            socket.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);
            socket.connect(address);
            if(sendJoinRoomMsg(socket, realRoomID)){
                heartBeattimer = new Timer();
                heartBeattimer.schedule(new sendHeartbeat(socket), 2000, 20000);
                return socket;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return socket;
    }

    public void disconnect(Socket socket){
        try {
            heartBeattimer.cancel();
            socket.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}

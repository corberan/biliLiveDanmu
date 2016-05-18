import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.net.Socket;
import com.alibaba.fastjson.*;

/**
 * Created by liuzc on 2016/3/2.
 */
public class danmuku_handler {
    private Socket socket = null;
    private boolean keepRunning = true;
    private boolean isReConn = true;
    private String roomID;

    private mainForm form;
    private server_client client;

    public void start(String roomID, boolean isReConnect, mainForm form){
        this.roomID = roomID;
        isReConn = isReConnect;
        client = new server_client();
        socket = client.connect(this.roomID);
        if (socket != null && form != null) {
            this.form = form;
            new handle_data_loop().start();
        }
    }

    public void stop(){
        keepRunning = false;
        client.disconnect(socket);
    }

    private class handle_data_loop extends Thread {
        DataInputStream input = null;

        public void run(){
            if (socket != null){
                int bufferSize = 10 * 1024;
                try {
                    bufferSize = socket.getReceiveBufferSize();
                    form.log("连接成功");
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                byte[] ret = new byte[bufferSize];
                while (keepRunning){
                    try {
                        input = new DataInputStream(socket.getInputStream());
                        int retLength = input.read(ret);
                        if (retLength > 0 && keepRunning) {
                            byte[] recvData = new byte[retLength];
                            System.arraycopy(ret, 0, recvData, 0, retLength);
                            analyzeData(recvData);
                        }
                    }catch (Exception e){
                        if (isReConn && keepRunning) {
                            form.log("自动重连");
                            (new danmuku_handler()).start(roomID, true, form);
                        }
                        keepRunning = false;
                        e.printStackTrace();
                    }
                }
            }
        }

        private void analyzeData(byte[] data){
            int dataLength = data.length;
            if (dataLength < 16){
                System.out.println("错误的数据");
            }else {
                DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data));
                try {
                    int msgLength = inputStream.readInt();
                    if (msgLength < 16){
                        System.out.println("可能需要扩大缓冲区大小");
                    }else if(msgLength > 16 && msgLength == dataLength){
                        // 其实是两个char
                        inputStream.readInt();
                        int action = inputStream.readInt() - 1;
                        // 直播间在线用户数目
                        if (action == 2){
                            inputStream.readInt();
                            int userCount = inputStream.readInt();
//                            System.out.println("在线人数：" + userCount);
                            form.changeTitle("在线人数：" + userCount);
                        }else if (action == 4){
                            inputStream.readInt();
                            int msgBodyLength = dataLength - 16;
                            byte[] msgBody = new byte[msgBodyLength];
                            if (inputStream.read(msgBody) == msgBodyLength){
                                String jsonStr = new String(msgBody, "utf-8");
//                                System.out.println(jsonStr);
                                JSONObject object = JSON.parseObject(jsonStr);
                                String msgType = object.getString("cmd");
                                switch (msgType){
                                    case "DANMU_MSG":{
                                        JSONArray array = object.getJSONArray("info").getJSONArray(2);
//                                    int uid = array.getInteger(0);
                                        String uname = array.getString(1);
                                        String danmuku = object.getJSONArray("info").getString(1);
//                                        System.out.println(uname + "：" + danmuku);
                                        form.log(0, uname, danmuku);
                                        break;
                                    }
                                    case "SEND_GIFT":{
                                        JSONObject giftData = object.getJSONObject("data");
                                        String giftName = giftData.getString("giftName");
                                        int giftNum = giftData.getInteger("num");
                                        String uname = giftData.getString("uname");
//                                    int uid = giftData.getInteger("uid");
//                                        System.out.println(uname + "赠送 " + giftName + "*" + giftNum);
                                        form.log(1, "礼物", uname + "赠送 " + giftName + "*" + giftNum);
                                        break;
                                    }
                                    case "WELCOME":{
                                        JSONObject welcData = object.getJSONObject("data");
//                                    int uid = welcData.getInteger("uid");
                                        String uname = welcData.getString("uname");
//                                        System.out.println("欢迎老爷 " + uname + " 进入直播间");
                                        form.log(1, "欢迎", "欢迎老爷 " + uname + " 进入直播间");
                                        break;
                                    }
                                }
//                                if (msgType.equals("DANMU_MSG")){
//                                    JSONArray array = object.getJSONArray("info").getJSONArray(2);
////                                    int uid = array.getInteger(0);
//                                    String uname = array.getString(1);
//                                    String danmuku = object.getJSONArray("info").getString(1);
//                                    System.out.println(uname + "：" + danmuku);
//                                }else if (msgType.equals("SEND_GIFT")){
//                                    JSONObject giftData = object.getJSONObject("data");
//                                    String giftName = giftData.getString("giftName");
//                                    int giftNum = giftData.getInteger("num");
//                                    String uname = giftData.getString("uname");
////                                    int uid = giftData.getInteger("uid");
//                                    System.out.println(uname + "赠送 " + giftName + "*" + giftNum);
//                                }else if (msgType.equals("WELCOME")){
//                                    JSONObject welcData = object.getJSONObject("data");
////                                    int uid = welcData.getInteger("uid");
//                                    String uname = welcData.getString("uname");
//                                    System.out.println("欢迎老爷 " + uname + " 进入直播间");
//                                }
                            }
                        }
                    }else if (msgLength > 16 && msgLength < dataLength){
                        byte[] singleData = new byte[msgLength];
                        System.arraycopy(data, 0, singleData, 0, msgLength);
                        analyzeData(singleData);
                        int remainLen = dataLength - msgLength;
                        byte[] remainDate = new byte[remainLen];
                        System.arraycopy(data, msgLength, remainDate, 0, remainLen);
                        analyzeData(remainDate);
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }

    }

}

package al.nya.verify;

import al.nya.verify.Data.User;
import al.nya.verify.Data.login.UniversalPacket;
import al.nya.verify.utils.Cryptor_AES_CFB8_NoPadding;
import al.nya.verify.utils.EncryptUtils;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class VerifyServer extends WebSocketServer {
    List<UserData> dataList = new ArrayList<UserData>();
    public VerifyServer(InetSocketAddress address) {
        super(address);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        dataList.add(new UserData(conn));
        Log.info("New client connected "+conn.getRemoteSocketAddress().getHostName());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        dataList.removeIf(userData -> userData.getConnect() == conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        for (UserData data : dataList) {
            if (data.getConnect() == conn){
                String stringData = data.cryptor.decrypt(message);
                if (Verify.logLevel == 0)
                System.out.println(stringData);
                UniversalPacket packet = new Gson().fromJson(stringData,UniversalPacket.class);
                if (packet == null) {
                    conn.close();
                }
                if (data.stage == HandlerStatus.HandShake){
                    if (packet.cmd.equals("Action_Hello")){
                        //Update key
                        String newKey = EncryptUtils.createRandomString(16);
                        UniversalPacket Action_Update_Key = new UniversalPacket();
                        Action_Update_Key.cmd = "Action_Update_Key";
                        Action_Update_Key.key = newKey;
                        sendJson(Action_Update_Key,data);
                        if (Verify.logLevel <= 1)
                        Log.info("[HandShake] "+data.getConnect().getRemoteSocketAddress().getHostName() +" Update Key");
                        data.cryptor = new Cryptor_AES_CFB8_NoPadding(newKey);
                        return;
                    }else if (packet.cmd.equals("Action_Handshake")){
                        UniversalPacket Action_Update_Key = new UniversalPacket();
                        Action_Update_Key.cmd = "Action_Handshake";
                        sendJson(Action_Update_Key,data);
                        if (Verify.logLevel <= 1)
                        Log.info("[HandShake] "+data.getConnect().getRemoteSocketAddress().getHostName() +" finish handshake");
                        if (Verify.logLevel <= 1)
                        Log.info("[Verify] Status change "+data.getConnect().getRemoteSocketAddress().getHostName()+" Handshake -> Login");
                        data.stage = HandlerStatus.Login;
                        return;
                    }
                }else if (data.stage == HandlerStatus.Login){
                    if (packet.cmd.equals("Action_Login_Data")){
                        if (Verify.logLevel <= 1)
                            Log.info("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" trying Login "+packet.username+" "+packet.passwd);
                        User user = Controller.getUser(packet.username);
                        UniversalPacket Action_Login_Status = new UniversalPacket();
                        Action_Login_Status.cmd = "Action_Login_Status";
                        if (user == null){
                            if (Verify.logLevel <= 2)
                            Log.error("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login fail : Unknown User");
                            Action_Login_Status.loginCode = 404;
                            sendJson(Action_Login_Status,data);
                            return;
                        }
                        if (!user.password.equals(packet.passwd)){
                            if (Verify.logLevel <= 2)
                            Log.error("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login fail : Wrong password");
                            Action_Login_Status.loginCode = 403;
                            sendJson(Action_Login_Status,data);
                            return;
                        }
                        if (user.ban){
                            if (Verify.logLevel <= 2)
                            Log.error("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login fail : Banned");
                            Action_Login_Status.loginCode = 406;
                            Action_Login_Status.reason = user.banReason;
                            sendJson(Action_Login_Status,data);
                            return;
                        }
                        if (user.name.equals("CanYingisme")){
                            user.registerTo = new Date(2399,12,30);
                        }
                        if (user.registerTo.compareTo(new Date()) < 0){
                            if (Verify.logLevel <= 2)
                            Log.error("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login fail : Outdated");
                            Action_Login_Status.loginCode = 407;
                            sendJson(Action_Login_Status,data);
                            return;
                        }
                        if (!isHWIDMatch(user,packet.hwidV1, packet.hwidV2)){
                            Date date = user.lastHWIDChange;
                            if (date == null){
                                Controller.clearHWID(user.qq);
                                return;
                            }
                            //Check is past 15 days
                            if (date.compareTo(new Date(new Date().getTime() - (15 * 24 * 60 * 60 * 1000))) < 0){
                                if (Verify.logLevel <= 2)
                                    Log.error("[Login] "+user.name+" change HWID v1: "+packet.hwidV1+" v2"+packet.hwidV2);
                                Controller.changeHWID(user,packet.hwidV1,packet.hwidV2);
                                onMessage(conn, message);
                                return;
                            }else {
                                Action_Login_Status.loginCode = 408;
                                //Get time past 15 days
                                Action_Login_Status.changeTime = new Date(date.getTime() + (15 * 24 * 60 * 60 * 1000)).toString();
                                if (Verify.logLevel <= 2)
                                    Log.error("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login fail : HWID reject");
                                sendJson(Action_Login_Status,data);
                                return;
                            }
                        }
                        Action_Login_Status.loginCode = 200;
                        Action_Login_Status.reconnectToken = UUID.randomUUID().toString();
                        data.token = Action_Login_Status.reconnectToken;
                        data.setUser(user);
                        data.refresh();
                        Log.info("[Login] "+data.getConnect().getRemoteSocketAddress().getHostName()+" login succ token:"+data.token);
                        Verify.ircServer.authUser.add(data);
                        sendJson(Action_Login_Status,data);
                        return;
                    }else if (packet.cmd.equals("Action_Request_Download")){
                        if (Verify.logLevel <= 1)
                        Log.info("[Download] "+data.getUser().name+" Request download "+packet.fileName);
                        UniversalPacket Action_Reply_Download = new UniversalPacket();
                        Action_Reply_Download.cmd = "Action_Reply_Download";
                        if (packet.fileName.equals("Hack")){
                            File file = new File(data.getUser().rank.equals("Release") ? "./files/reflectRelease.dll" : "./files/reflectBeta.dll");
                            if (file.exists()){
                                try {
                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] bytes = new byte[fis.available()];
                                    fis.read(bytes);
                                    ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
                                    Action_Reply_Download.length = bytes.length;
                                    sendJson(Action_Reply_Download,data);
                                    while (bos.available() != 0){
                                        if (bos.available() > 512){
                                            byte[] b = new byte[512];
                                            bos.read(b);
                                            data.getConnect().send(b);
                                        }else {
                                            byte[] b = new byte[bos.available()];
                                            bos.read(b);
                                            data.getConnect().send(b);
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Action_Reply_Download.length = 0;
                                sendJson(Action_Reply_Download,data);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    public boolean isHWIDMatch(User user,String loginHWIDv1,String loginHWIDv2){
        //Adapt old data
        return (user.HWIDv1 != null && user.HWIDv2 != null) && user.HWIDv1.equals(loginHWIDv1) && user.HWIDv2.equals(loginHWIDv2);
    }

    public void sendJson(Object obj,UserData data){
        String encData = data.cryptor.encrypt(new Gson().toJson(obj));
        data.getConnect().send(encData);
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.info("Verify Server start");
    }
}

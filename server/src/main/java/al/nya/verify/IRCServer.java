package al.nya.verify;

import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.User;
import al.nya.verify.Data.irc.*;
import al.nya.verify.resource.ResourceManager;
import al.nya.verify.transform.TransformManager;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * IRC Server
 * Payload Command Upload:
 *  Allocate {size} ByteBuffer
 *  Server save data wait next command
 *  WARN: No buffer available check
 * Payload Command TransformClass:
 *  Convert data to class
 *  Write to buffer after transform
 */
public class IRCServer extends WebSocketServer {
    public List<UserData> authUser = new ArrayList<UserData>();
    public List<WebSocket> handshakes = new ArrayList<WebSocket>();
    public IRCServer(InetSocketAddress address) {
        super(address);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        synchronized (authUser){
            if (conn.getRemoteSocketAddress().getHostName().equals("127.0.0.1")){
                UserData data = new UserData(conn);
                data.setUser(Controller.getUser(Long.parseLong("3192799549")));
                authUser.add(data);
            }else {
                handshakes.add(conn);
            }
            if (Verify.logLevel <= 1)
            Log.info("New connect "+conn.getRemoteSocketAddress().getHostName());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        synchronized (authUser){
            for (UserData data : authUser) {
                if (data.getConnect() == conn){
                    if (Verify.logLevel <= 1)
                    Log.info(data.getUser().name+" disconnected");
                }
            }
            for (UserData data : authUser) {
                if (data.getConnect() == conn){
                    //Agree reconnect as old token
                    data.refresh();
                }
            }
        }
    }
    public void removeExpiredToken(){
        synchronized (authUser){
            authUser.removeIf(data -> !data.getConnect().isOpen() && data.isExpired());
        }
    }
    public void removeDisconnectedSession(User user){
        synchronized (authUser){
            authUser.removeIf(data -> data.getUser() == user && !data.getConnect().isOpen());
        }
    }
    @Override
    public void onMessage(WebSocket conn, String messageUndecoded) {
        for (WebSocket handshake : handshakes) {
            if (handshake == conn){
                synchronized (authUser){
                    for (UserData data : authUser) {
                        if (data.token.equals(messageUndecoded)){
                            if (data.isExpired()){
                                authUser.remove(data);
                                data.setConnect(conn);
                                conn.close(403,"Token expired");
                                handshakes.remove(handshake);
                                removeExpiredToken();
                                return;
                            }
                            data.setConnect(conn);
                            if (Verify.logLevel <= 1)
                            Log.info(data.getUser().name+" verified");
                            handshakes.remove(handshake);
                            removeExpiredToken();
                            removeDisconnectedSession(data.getUser());
                            return;
                        }
                    }
                }
            }
        }
        String message = "";
        for (UserData userData : authUser) {
            if (userData.getConnect() == conn){
                message = userData.cryptor.decrypt(messageUndecoded);
            }
        }
        ControlCommand command = new Gson().fromJson(message,ControlCommand.class);
        if (command == null){
            conn.close();
        }
        if (command.getCommand().equals("UploadData")){
            CommandUpload upload = new Gson().fromJson(command.getData(),CommandUpload.class);
            for (UserData userData : authUser) {
                if (userData.getConnect() == conn){
                    userData.dataLen = upload.getSize();
                    userData.buffer = ByteBuffer.allocate(upload.getSize());
                }
            }
        }
        if (command.getCommand().equals("DownloadData")){
            CommandDownload download = new Gson().fromJson(command.getData(),CommandDownload.class);
            for (UserData userData : authUser) {
                if (userData.getConnect() == conn){
                    if (Verify.logLevel <= 1)
                    Log.info("[Download] "+userData.getUser().name+" Download " + download.fileName);
                    if (download.fileName.equals("UserData")){
                        IRCUserData ircUserData = new IRCUserData(userData.getUser().name,userData.getUser().rank);
                        try {
                            ResourceManager.send(userData,new ByteArrayInputStream(new Gson().toJson(ircUserData).getBytes(StandardCharsets.UTF_8)),"UserData");
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    ResourceManager.download(userData,download);
                }
            }
        }
        if (command.getCommand().equals("HeartBeat")){
            CommandHeartBeat heartBeat = new Gson().fromJson(command.getData(),CommandHeartBeat.class);
            for (UserData userData : authUser) {
                if (userData.getConnect() == conn){
                    userData.ping = System.currentTimeMillis() - heartBeat.getPushTime();
                    userData.getConnect().send(new Gson().toJson(new ControlCommand("HeartBeat",new Gson().toJson(
                            new CommandHeartBeat(System.currentTimeMillis())))));
                }
            }
        }
        if (command.getCommand().equals("TransformClass")){
            CommandTransformClass transformClass = new Gson().fromJson(command.getData(),CommandTransformClass.class);
            for (UserData userData : authUser) {
                if (userData.getConnect() == conn){
                    if (Verify.logLevel <= 1)
                    Log.info("[Transform] "+userData.getUser().name+" TransformClass " + transformClass.transformType);
                    TransformManager.transformClass(userData,transformClass);
                }
            }
        }
        if (command.getCommand().equals("IRCChat")){
            CommandIRCChat ircChat = new Gson().fromJson(command.getData(),CommandIRCChat.class);
            if (Verify.logLevel <= 2)
            Log.info("[IRC]["+ircChat.dst.name+"]"+ircChat.text);
            for (UserData userData : authUser) {
                if (userData.getConnect() == conn){
                    if (!ircChat.text.startsWith("/")){
                        IRCController.onMessage(userData,ircChat.text);
                    }else {
                        IRCController.onCommand(userData,ircChat.text);
                    }
                    return;
                }
            }
        }
    }
    public boolean isOnline(User user){
        for (UserData data : authUser) {
            if (data.getUser().equals(user)){
                return data.getConnect().isOpen();
            }
        }
        return false;
    }
    public UserData getUser(User user){
        for (UserData data : authUser) {
            if (data.getUser().equals(user) && data.getConnect().isOpen()){
                return data;
            }
        }
        return null;
    }
    @Override
    public void onMessage(WebSocket conn, ByteBuffer data){
        for (UserData userData : authUser) {
            if (userData.getConnect() == conn){
                userData.buffer.put(data);
            }
        }
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.info("IRC Server start");
    }
}

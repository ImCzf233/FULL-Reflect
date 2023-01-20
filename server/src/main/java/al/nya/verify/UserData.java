package al.nya.verify;

import al.nya.verify.Data.User;
import al.nya.verify.utils.Cryptor_AES_CFB8_NoPadding;
import org.java_websocket.WebSocket;

import java.nio.ByteBuffer;

public class UserData {
    private WebSocket conn;
    private User user;
    public int dataLen;
    public ByteBuffer buffer = ByteBuffer.allocate(0);
    public String IP = "";
    public String fileInDownload = "";
    public int dataType = -1;
    public String transformType = "";
    public String token = "";
    public Cryptor_AES_CFB8_NoPadding cryptor = new Cryptor_AES_CFB8_NoPadding("5ca37899z7b0f22e");
    public HandlerStatus stage = HandlerStatus.HandShake;
    public long createTime = 0;
    //IRCServer <-> IRCClient
    public long ping = 0;
    public UserData(WebSocket conn){
        this.conn = conn;
        IP = conn.getRemoteSocketAddress().getHostName();
    }


    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
    public void setConnect(WebSocket ws){
        this.conn = ws;
        IP = conn.getRemoteSocketAddress().getHostName();
    }
    public WebSocket getConnect() {
        return conn;
    }
    public void refresh(){
        this.createTime = System.currentTimeMillis();
    }
    public boolean isExpired(){
        //1hr
        return System.currentTimeMillis() - this.createTime > 1000 * 60 * 60 * 1;
    }
}

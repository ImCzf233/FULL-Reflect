package al.nya.verify;

import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.KeyCode;
import al.nya.verify.Data.commands.*;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class LocalController extends WebSocketServer {
    public LocalController(InetSocketAddress address) {
        super(address);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if(!conn.getRemoteSocketAddress().getHostName().equals("127.0.0.1") && !conn.getRemoteSocketAddress().getHostName().equals("localhost")) conn.close();
        Log.info("Controller connected "+conn.getRemoteSocketAddress().getHostName());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.info("Controller disconnected");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ControlCommand command = new Gson().fromJson(message,ControlCommand.class);
        if (command.getCommand().equals("GenKeyCode")){
            CommandGenKeyCode keyCode = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandGenKeyCode.class);
            Log.info("Bot request "+keyCode.getRequestCount()+" KeyCode(s) Type:"+keyCode.getType());
            List<KeyCode> keycodes = new ArrayList<KeyCode>();
            while (keycodes.size() != keyCode.getRequestCount()){
                keycodes.add(new KeyCode(UUID.randomUUID().toString(),keyCode.getType()));
            }
            Controller.addKeyCodes(keycodes);
            List<String> c = new ArrayList<String>();
            for (KeyCode keycode : keycodes) {
                c.add(keycode.getKey());
            }
            ReplyKeyCode replyKeyCode = new ReplyKeyCode(keyCode.getRequestID(),c);
            conn.send(packet("ReplyKeyCode",replyKeyCode));
        }
        if (command.getCommand().equals("VerifyKeyCode")){
            CommandVerifyKeyCode verify = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandVerifyKeyCode.class);
            Log.info("New join request ID:"+verify.id+" KeyCode:"+verify.key);
            ReplyVerify replyVerify = new ReplyVerify(Controller.verifyKeyCode(verify.getId(),verify.getKey()),verify.id);
            conn.send(packet("ReplyVerify",replyVerify));
        }
        if (command.getCommand().equals("Status")){
            CommandStatus status = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandStatus.class);
            Log.info(status.getId()+" request user data");
            ReplyStatus replyStatus = new ReplyStatus(Controller.getUser(status.getId()),false);
            conn.send(packet("ReplyStatus",replyStatus));
        }
        if (command.getCommand().equals("Register")){
            CommandRegister register = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandRegister.class);
            Log.info(register.getId()+" trying register "+register.getUsername()+":"+register.getPassword());
            conn.send(packet("ReplyRegister",Controller.register(register.getId(),register.getUsername(),register.getPassword())));
        }
        if (command.getCommand().equals("Remove")){
            CommandRemove remove = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandRemove.class);
            Log.info("Remove user "+remove.getId());
            Controller.remove(remove.getId());
            conn.send(packet("ReplyRemove",new ReplyRemove(remove.getId(),true)));
        }
        if (command.getCommand().equals("ChangeRank")){
            CommandChangeRank changeRank = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())), CommandChangeRank.class);
            Log.info("Change user "+changeRank.getId()+" rank ->"+changeRank.getRank());
            Controller.changeRank(changeRank.getId(),changeRank.getRank());
        }
        if (command.getCommand().equals("Ban")){
            CommandBan ban = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())),CommandBan.class);
            Log.info("Ban user "+ban+" reason:"+ban.getReason());
            conn.send(packet("ReplyBan",Controller.ban(ban.getId(),ban.getReason())));
        }
        if (command.getCommand().equals("Renew")){
            CommandRenew renew = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())),CommandRenew.class);
            Log.info("Renew user "+renew.id+" keyCode:"+renew.keyCode);
            conn.send(packet("ReplyRenew",Controller.renew(renew.id,renew.keyCode)));
        }
        if (command.getCommand().equals("Reload")){
            CommandReload reload = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())),CommandReload.class);
            Log.info("Reload data");
            Controller.initData();
        }
        if (command.getCommand().equals("ForcePush")){
            CommandForcePush forcePush = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())),CommandForcePush.class);
            Log.info("Force push user data to "+forcePush.id);
            conn.send(packet("ReplyForcePush",Controller.forcePush(forcePush)));
        }
        if (command.getCommand().equals("Revoke")){
            CommandRevoke revoke = new Gson().fromJson(new String(Base64.getDecoder().decode(command.getData())),CommandRevoke.class);
            Log.info("Revoke key "+revoke.key);
            Controller.revoke(revoke.key);
        }
    }
    public static String packet(String cmd,Object obj){
        ControlCommand command = new ControlCommand(cmd, Base64.getEncoder().encodeToString(new Gson().toJson(obj).getBytes(StandardCharsets.UTF_8)));
        return new Gson().toJson(command);
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        Log.info("Control service start");
    }
}

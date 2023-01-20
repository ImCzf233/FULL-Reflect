package al.nya.verify;

import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.User;
import al.nya.verify.Data.irc.CommandIRCChat;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IRCController {
    public static User server = new User("Server","Admin");
    public static void onMessage(UserData userData,String text){
        for (UserData data : getOnline()) {
            data.getConnect().send(new Gson().toJson(new ControlCommand("IRCChat",new Gson().toJson(
                        new CommandIRCChat(new User(userData.getUser().name,userData.getUser().rank),text)))));
        }
    }
    public static List<UserData> getOnline(){
        List<UserData> authed = Verify.ircServer.authUser;
        List<UserData> online = new ArrayList<UserData>();
        for (UserData userData : authed) {
            if (userData.getConnect().isOpen()) online.add(userData);
        }
        return online;
    }
    public static void onCommand(UserData data,String text){
        String command = text.replaceFirst("/","");
        if (command.equalsIgnoreCase("online")){
            List<UserData> online = getOnline();
            StringBuilder sb = new StringBuilder();
            sb.append("\u00a7e").append(online.size()).append(" User(s) online:\u00a77\n");
            for (UserData userData : online) {
                sb.append("\u00a7a").append(userData.getUser().name).append(pingMessage(userData.ping)).append("\u00a77,");
            }
            sendMessage(data,server,sb.toString());
        }
        if (command.toLowerCase(Locale.ROOT).startsWith("private")){
            String[] msg = command.replaceFirst("private ","").split(" ",2);
            if (msg.length != 2) sendMessage(data,server,"\u00a72 /private [TargetName] [Message]");
            for (UserData userData : getOnline()) {
                if (userData.getUser().name.equals(msg[0])){
                    sendMessage(userData,data.getUser(),"\u00a75[Private]\u00a77"+msg[1]);
                    sendMessage(data,data.getUser(),"\u00a75[Private]\u00a77"+msg[1]);
                    Log.info("[IRC] "+data.getUser().name +" -> "+userData.getUser().name+" "+text);
                    return;
                }
            }
            sendMessage(data,server,"\u00a72 Can't find target user :"+msg[0]);
        }
    }
    public static String pingMessage(long ping){
        return "\u00a7a" + (ping > 100 ? (ping > 200 ? "\u00a72" : "\u00a76") : "") + "["+ping+"ms]";
    }
    public static void sendMessage(UserData data,User dst,String text){
        data.getConnect().send(new Gson().toJson(new ControlCommand("IRCChat",new Gson().toJson(
                new CommandIRCChat(new User(dst.name,dst.rank),text)))));
    }
    public static void broadcast(String text){
        for (UserData data : getOnline()) {
            sendMessage(data,server,"\u00a7a"+text);
        }
    }
}

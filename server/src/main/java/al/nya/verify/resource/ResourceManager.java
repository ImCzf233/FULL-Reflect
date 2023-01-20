package al.nya.verify.resource;

import al.nya.verify.Data.ControlCommand;
import al.nya.verify.Data.irc.CommandDownload;
import al.nya.verify.Data.irc.CommandSendFinish;
import al.nya.verify.Data.irc.CommandUpload;
import al.nya.verify.Data.irc.IRCUserData;
import al.nya.verify.UserData;
import com.google.gson.Gson;

import java.io.*;

public class ResourceManager {
    public static void download(UserData ud, CommandDownload download){
        File file = new File("./files/"+download.fileName);
        if (file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = new byte[fis.available()];
                fis.read(bytes);
                send(ud,new ByteArrayInputStream(bytes), download.fileName);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            ud.dataLen = 0;
            ud.getConnect().send(new Gson().toJson(new ControlCommand("UploadData",new Gson().toJson(new CommandUpload(ud.dataLen)))));
            ud.getConnect().send(new Gson().toJson(new ControlCommand("SendFinish",new Gson().toJson(new CommandSendFinish(download.fileName)))));
        }
    }
    public static void send(UserData ud,ByteArrayInputStream bos,String name) throws IOException {
        ud.dataLen = bos.available();
        ud.getConnect().send(new Gson().toJson(new ControlCommand("UploadData",new Gson().toJson(new CommandUpload(ud.dataLen)))));
        while (bos.available() != 0){
            if (bos.available() > 512){
                byte[] b = new byte[512];
                bos.read(b);
                ud.getConnect().send(b);
            }else {
                byte[] b = new byte[bos.available()];
                bos.read(b);
                ud.getConnect().send(b);
            }
        }
        ud.getConnect().send(new Gson().toJson(new ControlCommand("SendFinish",new Gson().toJson(new CommandSendFinish(name)))));
    }
}

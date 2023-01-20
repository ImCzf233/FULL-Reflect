package al.nya.verify.Data.irc;

import al.nya.verify.Data.User;
import al.nya.verify.UserData;

public class CommandIRCChat {
    public User dst;
    public String text;
    public CommandIRCChat(User dst, String text){
        this.dst = dst;
        this.text = text;
    }
}

package al.nya.verify.Data.commands;

import al.nya.verify.Data.User;

public class ReplyStatus {
    private User user;
    private boolean online;
    public ReplyStatus(User user,boolean online){
        this.user = user;
        this.online = online;
    }

    public User getUser() {
        return user;
    }

    public boolean isOnline() {
        return online;
    }
}

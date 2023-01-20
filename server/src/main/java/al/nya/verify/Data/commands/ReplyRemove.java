package al.nya.verify.Data.commands;

public class ReplyRemove {
    private boolean succ;
    public ReplyRemove(long id, boolean succ){
        this.succ = succ;
    }

    public boolean isSucc() {
        return succ;
    }
}

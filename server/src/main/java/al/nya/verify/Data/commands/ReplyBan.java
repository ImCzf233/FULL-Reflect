package al.nya.verify.Data.commands;

public class ReplyBan {
    private long id;
    private boolean succ;
    private String reason;
    public ReplyBan(long id,boolean succ,String reason){
        this.id = id;
        this.succ = succ;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public long getId() {
        return id;
    }

    public boolean isSucc() {
        return succ;
    }
}

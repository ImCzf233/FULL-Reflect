package al.nya.verify.Data.commands;

public class ReplyRegister {
    private boolean succ;
    private String reason;
    private long id;
    public ReplyRegister(long id,boolean succ,String reason){
        this.succ = succ;
        this.reason = reason;
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public boolean isSucc() {
        return succ;
    }

    public long getId() {
        return id;
    }
}

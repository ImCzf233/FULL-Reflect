package al.nya.verify.Data.commands;

public class CommandBan {
    private long id;
    private String reason;
    public CommandBan(long id,String reason){
        this.id = id;
        this.reason = reason;
    }

    public long getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }
}

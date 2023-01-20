package al.nya.verify.Data.commands;

public class ReplyRenew {
    private long id;
    private String status;
    public ReplyRenew(long id,String status){
        this.id = id;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}

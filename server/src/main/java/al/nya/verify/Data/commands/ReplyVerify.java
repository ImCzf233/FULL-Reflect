package al.nya.verify.Data.commands;

public class ReplyVerify {
    private boolean pass;
    private long id;
    public ReplyVerify(boolean b, long id){
        pass = b;
        this.id = id;
    }

    public boolean isPass() {
        return pass;
    }

    public long getId() {
        return id;
    }
}

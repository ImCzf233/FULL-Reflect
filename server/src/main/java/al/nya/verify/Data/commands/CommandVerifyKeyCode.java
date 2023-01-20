package al.nya.verify.Data.commands;

public class CommandVerifyKeyCode {
    public long id;
    public String key;
    public CommandVerifyKeyCode(long id,String key){
        this.id = id;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public long getId() {
        return id;
    }
}

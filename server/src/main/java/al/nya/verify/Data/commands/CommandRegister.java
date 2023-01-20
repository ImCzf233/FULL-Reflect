package al.nya.verify.Data.commands;

public class CommandRegister {
    private long id;
    private String username;
    private String password;
    public CommandRegister(long id,String username,String password){
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}

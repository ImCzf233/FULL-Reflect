package al.nya.verify.Data.commands;

public class CommandChangeRank {
    private long id;
    private String rank;
    public CommandChangeRank(long id,String rank){
        this.id = id;
        this.rank = rank;
    }

    public long getId() {
        return id;
    }

    public String getRank() {
        return rank;
    }
}

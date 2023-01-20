package al.nya.verify.Data.irc;

import lombok.Getter;

public class CommandHeartBeat {
    @Getter
    private long pushTime;
    public CommandHeartBeat(long pushTime){
        this.pushTime = pushTime;
    }
}

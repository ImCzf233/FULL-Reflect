package al.nya.verify.Data.commands;

import java.util.List;

public class ReplyKeyCode {
    private long requesterID;
    private List<String> keycodes;
    public ReplyKeyCode(long requesterID, List<String> keycodes){
        this.requesterID = requesterID;
        this.keycodes = keycodes;
    }

    public List<String> getKeycodes() {
        return keycodes;
    }

    public long getRequesterID() {
        return requesterID;
    }
}

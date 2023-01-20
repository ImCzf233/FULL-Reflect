package al.nya.verify.Data.api;

import al.nya.verify.api.data.UserToken;
import lombok.Setter;

public class Status {
    private boolean status;
    private String reason;
    @Setter
    private UserToken token;
    public Status(boolean status,String reason){
        this.status = status;
        this.reason = reason;
    }
}

package al.nya.verify.api.data;

import lombok.Getter;

import java.util.UUID;

public class UserToken {
    @Getter private long belongTo;
    @Getter private long createTime;
    @Getter private String token;
    public UserToken(long qq) {
        this.belongTo = qq;
        this.createTime = System.currentTimeMillis();
        token = UUID.randomUUID().toString();
    }
    public void refresh(){
        this.createTime = System.currentTimeMillis();
    }
    public boolean isExpired(){
        return System.currentTimeMillis() - this.createTime > 1000 * 60 * 60 * 24;
    }
}

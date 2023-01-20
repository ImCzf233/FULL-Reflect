package al.nya.verify.Data;

import java.util.Calendar;
import java.util.Date;

public class User {
    public String name;
    public String password;
    public long qq;
    public KeyCode inviteCode;
    public String rank;
    public Date registerTo;
    public boolean ban;
    public String banReason = "";
    //Harddisk md5
    public String HWIDv1 = "";
    //CPUID
    public String HWIDv2 = "";
    public Date lastHWIDChange = new Date(1, Calendar.NOVEMBER,10);
    public User(String name,String password,long qq,KeyCode inviteCode,String rank,Date registerTo,boolean ban){
        this.name = name;
        this.password = password;
        this.qq = qq;
        this.inviteCode = inviteCode;
        this.rank = rank;
        this.registerTo = registerTo;
        this.ban = ban;
    }
    public User(String name,String rank){
        this.name = name;
        this.rank = rank;
    }

    public void setRegisterTo(Date registerTo) {
        this.registerTo = registerTo;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

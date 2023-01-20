package al.nya.verify.Data.login;

import al.nya.verify.Data.User;
import al.nya.verify.UserData;

public class LoginReturnPack {
    public int Code;
    public User Data;
    public String token;
    public LoginReturnPack(int code, User extraData){
        Code = code;
        Data = extraData;
    }
}

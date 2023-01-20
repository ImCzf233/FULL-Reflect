package al.nya.verify.Data.irc;

import java.util.Base64;

public class CommandByteData {
    private String data;
    public CommandByteData(byte[] bytes){
        this.data = Base64.getEncoder().encodeToString(bytes);
    }
    public byte[] getData(){
        return Base64.getDecoder().decode(data);
    }
}

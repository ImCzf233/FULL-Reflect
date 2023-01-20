package al.nya.verify.Data.irc;

public class CommandTransformClass {
    public String transformType;
    public String[] extraData;
    public CommandTransformClass(String transformType,String[] extraData){
        this.transformType = transformType;
        this.extraData = extraData;
    }
}

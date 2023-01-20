package al.nya.verify.Data.commands;

public class CommandGenKeyCode {
    private long requestID;
    private int requestCount;
    private int type;
    public CommandGenKeyCode(long requestID,int requestCount,int type){
        this.requestID = requestID;
        this.requestCount = requestCount;
        this.type = type;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public long getRequestID() {
        return requestID;
    }

    public int getType() {
        return type;
    }
}

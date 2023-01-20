package al.nya.verify.Data.login;

public class UniversalPacket {
    /**
     * String cmd
     * Action_Hello C
     * Action_Update_Key S
     * Action_Handshake C/S
     * Action_Login_Data C
     * Action_Login_Status S
     * Action_Request_Download C
     * Action_Reply_Download S
     * Action_Send_Data S
     */
    public String cmd;
    public String key;
    public String username;
    public String passwd;
    public int loginCode;
    public String loginStatus;
    public String fileName;
    public String md5;
    public int length;
    public String reconnectToken;
    public String reason;
    public String hwidV1;
    public String hwidV2;
    public String injectorMD5;
    public String changeTime;
}

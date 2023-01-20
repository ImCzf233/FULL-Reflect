package al.nya.verify.Data.api;

public class reCaptchaVerify {
    private String secret;
    private String response;
    public reCaptchaVerify(String secret,String response){
        this.secret = secret;
        this.response = response;
    }
}

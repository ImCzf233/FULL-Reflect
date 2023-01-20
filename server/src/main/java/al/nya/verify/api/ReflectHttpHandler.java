package al.nya.verify.api;

import al.nya.verify.Controller;
import al.nya.verify.Data.User;
import al.nya.verify.Data.api.Status;
import al.nya.verify.Data.api.reCaptchaResponse;
import al.nya.verify.Data.commands.ReplyRegister;
import al.nya.verify.Data.commands.ReplyRenew;
import al.nya.verify.Log;
import al.nya.verify.Verify;
import al.nya.verify.api.data.UserToken;
import al.nya.verify.utils.EncryptUtils;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectHttpHandler implements HttpHandler {
    private Gson gson = new Gson();
    private String passwordRegex = "(?!^(\\d+|[a-zA-Z]+|[~!@#$%^&*()_.]+)$)^[\\w~!@#$%^&*()_.]{8,16}$";
    private String qqRegex = "[1-9][0-9]{4,14}";
    private List<UserToken> tokens = new ArrayList<UserToken>();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Log.info("Api request:"+exchange.getRequestURI());
        String[] req = exchange.getRequestURI().toString().split("\\?");
        if (req.length == 2){
            if (req[0].equalsIgnoreCase("/register")){
                //Handle register
                List<RequestParameter<?>> parameters = Arrays.asList(
                        new StringParameter("name"),
                        new StringParameter("qq"),
                        new StringParameter("pwd"),
                        new StringParameter("key"),
                        new StringParameter("token"));
                FastParameterFindList list = RequestProcess.process(req[1],parameters);
                if (!list.checkNull()){
                    //Check prams
                    String name = (String) list.getParameter("name").getValue();
                    String qq = (String) list.getParameter("qq").getValue();
                    String pwd = (String) list.getParameter("pwd").getValue();
                    String code = (String) list.getParameter("key").getValue();
                    String token = (String) list.getParameter("token").getValue();
                    if (!verifyToken(token)){
                        response(exchange,403,"");
                        return;
                    }
                    System.out.println(name+" "+qq+" "+pwd+" "+code);
                    if (code.length() != 36){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong invite code")));
                        return;
                    }
                    if (!pwd.matches(passwordRegex)){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong password format")));
                        return;
                    }
                    if (!qq.matches(qqRegex)){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong QQ")));
                        return;
                    }
                    if (name.contains(" ")){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong Username format")));
                        return;
                    }
                    ReplyRegister rr = Controller.register(Long.parseLong(qq),name,pwd,code);
                    response(exchange,rr.isSucc() ? 200 : 400,gson.toJson(new Status(rr.isSucc(),rr.getReason())));
                    return;
                }else {
                    response(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                    return;
                }
            }
            if (req[0].equalsIgnoreCase("/renew")){
                String[] args = req[1].split("&");
                if (args.length == 3){
                    String qq = "";
                    String code = "";
                    String token = "";
                    for (String arg : args) {
                        if (arg.contains("qq=")){
                            qq = arg.replaceFirst("qq=","");
                        }
                        if (arg.contains("key=")){
                            code = arg.replaceFirst("key=","");
                        }
                        if (arg.contains("token=")){
                            token = arg.replaceFirst("token=","");
                        }
                    }
                    if (!token.equals("")){
                        if (!verifyToken(token)){
                            try {
                                handleResponse(exchange,403,"");
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        try {
                            handleResponse(exchange,403,"");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    if (qq.equals("")||code.equals("")){
                        try {
                            handleResponse(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (code.length() != 36){
                        try {
                            handleResponse(exchange,400,gson.toJson(new Status(false,"Wrong invite code")));
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!qq.matches(qqRegex)){
                        try {
                            handleResponse(exchange,400,gson.toJson(new Status(false,"Wrong QQ")));
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ReplyRenew rr = Controller.renew(Long.parseLong(qq),code);
                    try {
                        handleResponse(exchange,200,gson.toJson(new Status(true,rr.getStatus())));
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    try {
                        handleResponse(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            if (req[0].equalsIgnoreCase("/login")){
                List<RequestParameter<?>> parameters = Arrays.asList(
                        new StringParameter("qq"),
                        new StringParameter("pwd"),
                        new StringParameter("token"));
                FastParameterFindList list = RequestProcess.process(req[1],parameters);
                if (!list.checkNull()){
                    String qq = (String) list.getParameter("qq").getValue();
                    String pwd = (String) list.getParameter("pwd").getValue();
                    String token = (String) list.getParameter("token").getValue();
                    if (!token.equals("")){
                        if (!verifyToken(token)){
                            response(exchange,403,"");
                            return;
                        }
                    }else {
                        response(exchange,403,"");
                        return;
                    }
                    if (qq.equals("")||pwd.equals("")){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                        return;
                    }
                    User user = Controller.getUser(Long.parseLong(qq));
                    if (user == null){
                        response(exchange,400,gson.toJson(new Status(false,"Wrong QQ")));
                        return;
                    }
                    try {
                        if (!user.password.equals(EncryptUtils.generateMD5(user.name+"|"+pwd))){
                            response(exchange,400,gson.toJson(new Status(false,"Wrong password")));
                            return;
                        }
                    } catch (NoSuchAlgorithmException e) {
                        response(exchange,400,gson.toJson(new Status(false,"Server exception")));
                        e.printStackTrace();
                        Log.exp(e);
                        return;
                    }
                    UserToken token1 = new UserToken(Long.parseLong(qq));
                    tokens.add(token1);
                    response(exchange,200,gson.toJson(new Status(true,new Gson().toJson(token1.getToken()))));
                    return;
                }else {
                    response(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                    return;
                }
            }
            if(req[0].equalsIgnoreCase("/info")){
                List<RequestParameter<?>> parameters = Arrays.asList(
                        new StringParameter("user"));
                FastParameterFindList list = RequestProcess.process(req[1],parameters);
                if (!list.checkNull()){
                    String userToken = (String) list.getParameter("user").getValue();
                    UserToken userToken1 = getToken(userToken);
                    if (userToken1 == null){
                        response(exchange,403,"Wrong token");
                        return;
                    }
                    if (userToken1.isExpired()){
                        response(exchange,403,"Token expired");
                        tokens.remove(userToken1);
                        return;
                    }
                    User user = Controller.getUser(userToken1.getBelongTo());
                    if (user == null){
                        response(exchange,400,gson.toJson(new Status(false,"Unknown user")));
                        return;
                    }
                    response(exchange,200,gson.toJson(new Status(true,new Gson().toJson(user))));
                    return;
                }else {
                    response(exchange,400,gson.toJson(new Status(false,"Wrong prams")));
                    return;
                }
            }

            response(exchange,400,gson.toJson(new Status(false,"Wrong request")));
            return;
        }else {
            response(exchange,400,gson.toJson(new Status(false,"Wrong request")));
            return;
        }
    }
    private UserToken getToken(String token){
        for (UserToken userToken : tokens) {
            if (userToken.getToken().equals(token)){
                return userToken;
            }
        }
        return null;
    }
    private boolean verifyToken(String token){
        //https://www.google.com/recaptcha/api/siteverify
        Log.info("Token: "+token);

        String ret = doPost(Verify.reCaptcha_API+"?secret="+Verify.reCaptcha_Secret+"&response="+token,"");
        if (ret.equals("")){
            return false;
        }
        reCaptchaResponse response = gson.fromJson(ret,reCaptchaResponse.class);
        System.out.println(ret);
        return response.success;
    }
    private void response(HttpExchange httpExchange,int code,String responsetext){
        try {
            handleResponse(httpExchange,code,responsetext);
        } catch (Exception e) {
            e.printStackTrace();
            Log.exp(e);
        }
    }
    private void handleResponse(HttpExchange httpExchange,int code, String responsetext) throws Exception {
        //生成html
        Log.info("Response "+code+ " "+responsetext);
        StringBuilder responseContent = new StringBuilder();
        responseContent.append(responsetext);
        String responseContentStr = responseContent.toString();
        byte[] responseContentByte = responseContentStr.getBytes(StandardCharsets.UTF_8);

        //设置响应头，必须在sendResponseHeaders方法之前设置！
        httpExchange.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Credentials","true");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin","*");
        //设置响应码和响应体长度，必须在getResponseBody方法之前调用！
        httpExchange.sendResponseHeaders(code, responseContentByte.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(responseContentByte);
        out.flush();
        out.close();
    }
    public String doPost(String URL,String json){
        OutputStreamWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        HttpURLConnection conn = null;
        try{
            URL url = new URL(URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            //发送POST请求必须设置为true
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //设置连接超时时间和读取超时时间
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            //获取输出流
            out = new OutputStreamWriter(conn.getOutputStream());
            String jsonStr = json;
            out.write(jsonStr);
            out.flush();
            out.close();
            //取得输入流，并使用Reader读取
            if (200 == conn.getResponseCode()){
                in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                while ((line = in.readLine()) != null){
                    result.append(line);
                    System.out.println(line);
                }
            }else{
                System.out.println("ResponseCode is an error code:" + conn.getResponseCode());
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
        return result.toString();
    }
}

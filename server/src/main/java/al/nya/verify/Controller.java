package al.nya.verify;

import al.nya.verify.Data.KeyCode;
import al.nya.verify.Data.KeyCodes;
import al.nya.verify.Data.User;
import al.nya.verify.Data.Users;
import al.nya.verify.Data.api.Status;
import al.nya.verify.Data.commands.*;
import al.nya.verify.utils.EncryptUtils;
import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Controller {
    public static Users usersData;
    private static KeyCodes keyCodesData;
    public static File users = new File("./Users.json");
    private static File keycodes = new File("./keycodes.json");
    public static String login(String userName,String encryptedData){
        return "";
    }
    public static Status clearHWID(Long qq){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == qq){
                    user.HWIDv1 = "";
                    user.HWIDv2 = "";
                    user.lastHWIDChange = new Date(1, Calendar.NOVEMBER,10);
                    Log.info("Clear "+qq+" HwID");
                    return new Status(true,"");
                }
            }
        }
        return new Status(false,"Unknown User");
    }
    public static void initData(){
        if (!users.exists()){
            Users users1 = new Users();
            String s = new Gson().toJson(users1);
            try {
                FileOutputStream fos = new FileOutputStream(users);
                fos.write(s.getBytes(StandardCharsets.UTF_8));
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            initData();
            return;
        }
        if (!keycodes.exists()){
            KeyCodes codes = new KeyCodes();
            String s = new Gson().toJson(codes);
            try {
                FileOutputStream fos = new FileOutputStream(keycodes);
                fos.write(s.getBytes(StandardCharsets.UTF_8));
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            initData();
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(users);
            byte[] bytes = new byte[fis.available()];
            fis.read(bytes);
            usersData = new Gson().fromJson(new String(bytes),Users.class);
            fis.close();
            fis = new FileInputStream(keycodes);
            bytes = new byte[fis.available()];
            fis.read(bytes);
            keyCodesData = new Gson().fromJson(new String(bytes),KeyCodes.class);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (usersData == null){
            usersData = new Users();
        }
        if (keyCodesData == null){
            keyCodesData = new KeyCodes();
        }
    }
    public static void addKeyCodes(List<KeyCode> list){
        synchronized (keyCodesData){
            keyCodesData.codes.addAll(list);
            save(keyCodesData,keycodes);
            Log.info("Save "+list.size()+" KeyCode(s)");
        }
    }
    public static ReplyForcePush forcePush(CommandForcePush forcePush){
        if (verifyKeyCode(forcePush.id,forcePush.keyCode)){
            return new ReplyForcePush(forcePush.id,"Succ");
        }else {
            return new ReplyForcePush(forcePush.id,"Wrong Key");
        }
    }
    public static boolean verifyKeyCode(long id,String key){
        synchronized (keyCodesData){
            for (KeyCode code : keyCodesData.codes) {
                if (code.getKey().equals(key)) {
                    Log.info("Verify user " + id + " use KeyCode " + code.getKey());
                    return true;
                }
            }
            Log.info("Verify fail ID:"+id+" KeyCode:"+key);
            return false;
        }
    }
    public static void changeHWID(User user,String hwidV1,String hwidV2){
        synchronized (usersData){
            user.HWIDv1 = hwidV1;
            user.HWIDv2 = hwidV2;
            user.lastHWIDChange = new Date();
            save(usersData,users);
        }
    }
    public static void registerUser(long id,KeyCode keyCode){
        Calendar month = Calendar.getInstance();
        month.add(Calendar.MONTH,1);
        User user = new User("","",id,keyCode,"Release",
                keyCode.getType() == 0 ?
                        new Date(2099, Calendar.DECEMBER,30) :
                keyCode.getType() == 2 ?
                        month.getTime() : new Date(1979, Calendar.DECEMBER,30),false);
        synchronized (usersData){
            usersData.users.add(user);
            save(usersData,users);
        }
    }
    public static void remove(long id){
        synchronized (usersData){
            usersData.users.removeIf(user -> user.qq == id);
            save(usersData,users);
        }
    }
    public static ReplyRegister register(long id,String name,String password,String keycode){
        synchronized (usersData){
            KeyCode key = null;
            synchronized (keyCodesData){
                for (KeyCode code : keyCodesData.codes) {
                    if (code.getKey().equals(keycode)){
                        key = code;
                    }
                }
            }
            if (key == null){
                Log.info(id+" Fail to register:Wrong KeyCode");
                return new ReplyRegister(id,false,"Wrong invite code");
            }
            registerUser(id,key);
            for (User user : usersData.users) {
                if (user.qq == id){
                    if (!user.name.equals("") && !user.password.equals("")){
                        Log.info(id+" Fail to register:Already registered");
                        return new ReplyRegister(id,false,"You are already registered");
                    }
                }
                if (user.name.equals(name)){
                    Log.info(id+" Fail to register:Username not avaliable");
                    return new ReplyRegister(id,false,"Username already taken");
                }
            }
            boolean change = false;
            for (User user : usersData.users) {
                if (user.qq == id){
                    user.name = name;
                    try {
                        user.password = EncryptUtils.generateMD5(user.name+"|"+password);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        Log.info(id+" Fail to register:Exception");
                        return new ReplyRegister(id,false,e.getMessage());
                    }
                    change = true;
                }
            }
            if (!change){
                Log.info(id+" Fail to register:Connect find user");
                return new ReplyRegister(id,false,"Unknown error");
            }
            synchronized (keyCodesData){
                keyCodesData.codes.remove(key);
                save(keyCodesData,keycodes);
            }
            save(usersData,users);
            Log.info(id+" register success");
            return new ReplyRegister(id,true,"Registration success");
        }
    }
    public static ReplyRegister register(long id,String name,String password){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == id){
                    if (!user.name.equals("") && !user.password.equals("")){
                        Log.info(id+" Fail to register:Already registered");
                        return new ReplyRegister(id,false,"你已经注册过了");
                    }
                }
                if (user.name.equals(name)){
                    Log.info(id+" Fail to register:Username not avaliable");
                    return new ReplyRegister(id,false,"用户名已被占用");
                }
            }
            boolean change = false;
            for (User user : usersData.users) {
                if (user.qq == id){
                    user.name = name;
                    try {
                        user.password = EncryptUtils.generateMD5(user.name+"|"+password);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        Log.info(id+" Fail to register:Exception");
                        return new ReplyRegister(id,false,e.getMessage());
                    }
                    change = true;
                }
            }
            if (!change){
                Log.info(id+" Fail to register:Connect find user");
                return new ReplyRegister(id,false,"找不到用户");
            }
            save(usersData,users);
            Log.info(id+" register success");
            return new ReplyRegister(id,true,"");
        }
    }
    public static void enable(long id){
        Calendar month = Calendar.getInstance();
        month.add(Calendar.MONTH,1);
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == id){
                    user.registerTo = user.inviteCode.getType() == 1 ?
                            new Date(2099, Calendar.DECEMBER,30) :
                            user.inviteCode.getType() == 0 ?
                                    new Date(2099, Calendar.DECEMBER,30) :
                                    user.inviteCode.getType() == 2 ?
                                    month.getTime() : new Date(1979, Calendar.DECEMBER,30);
                }
            }
            save(usersData,users);
        }
    }
    public static ReplyRenew renew(long id, String keyCode){
        Calendar month = Calendar.getInstance();
        month.add(Calendar.MONTH,1);
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == id){
                    if (user.registerTo.compareTo(new Date()) < 0){
                        synchronized (keyCodesData){
                            for (KeyCode code : keyCodesData.codes) {
                                if (code.getKey().equals(keyCode)) {
                                    keyCodesData.codes.remove(code);
                                    save(keyCodesData, keycodes);
                                    user.inviteCode = code;
                                    enable(id);
                                    return new ReplyRenew(id,"Renew succ");
                                }
                            }
                            return new ReplyRenew(id,"Unknown KeyCode "+keyCode);
                        }
                    }else {
                        return new ReplyRenew(id,"Account not outdated");
                    }
                }
            }
            save(usersData,users);
        }
        return new ReplyRenew(id,"Unknown User");
    }
    public static boolean isPast15days(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-15);
        return date.compareTo(calendar.getTime()) < 0;
    }
    public static ReplyKeyCode genKeyCode(int count,int type){
        synchronized (keyCodesData){
            List<KeyCode> keycodes = new ArrayList<KeyCode>();
            while (keycodes.size() != count){
                keycodes.add(new KeyCode(UUID.randomUUID().toString(),type));
            }
            Controller.addKeyCodes(keycodes);
            List<String> c = new ArrayList<String>();
            for (KeyCode keycode : keycodes) {
                c.add(keycode.getKey());
            }
            save(keyCodesData,Controller.keycodes);
            ReplyKeyCode replyKeyCode = new ReplyKeyCode(0,c);
            return replyKeyCode;
        }
    }
    public static void changeRank(long id,String rank){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == id) {
                    user.rank = rank;
                    System.out.println(user.rank);
                    break;
                }
            }
            save(usersData,users);
        }
    }
    public static User getUser(long qq){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == qq){
                    return user;
                }
            }
        }
        return null;
    }
    public static void revoke(String key){
        synchronized (keyCodesData){
            KeyCode keyCode  = null;
            for (KeyCode code : keyCodesData.codes) {
                if (code.getKey().equals(key)){
                    keyCode = code;
                    break;
                }
            }
            if (keyCode != null){
                keyCodesData.codes.remove(keyCode);
            }
            save(keyCodesData,keycodes);
        }
    }
    public static ReplyBan ban(long qq, String reason){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == qq){
                    user.ban = true;
                    user.banReason = reason;
                    Log.info("Successfully ban user "+qq+" Reason:"+user.banReason);
                    save(usersData,users);
                    return new ReplyBan(user.qq,true,"");
                }
            }
            Log.error("Fail ban user "+qq+" Reason:Unknown User");
            return new ReplyBan(qq,false,"Unknown User");
        }
    }
    public static ReplyBan unban(long qq){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == qq){
                    user.ban = false;
                    user.banReason = "";
                    Log.info("Successfully unban user "+qq);
                    save(usersData,users);
                    return new ReplyBan(user.qq,true,"");
                }
            }
            Log.error("Fail unban user "+qq+" Reason:Unknown User");
            return new ReplyBan(qq,false,"Unknown User");
        }
    }
    public static Status changePassword(Long qq,String newPassword){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.qq == qq){
                    try {
                        user.password = EncryptUtils.generateMD5(user.name+"|"+newPassword);
                    } catch (NoSuchAlgorithmException e) {
                        Log.exp(e);
                        Log.error("Fail change password exception catch");
                        return new Status(false,"Server exception");
                    }
                    Log.info("Successfully change password");
                    save(usersData,users);
                    return new Status(true,"");
                }
            }
            Log.error("Fail change password unknown user "+qq);
            return new Status(false,"Unknown User");
        }
    }
    public static User getUser(String name){
        synchronized (usersData){
            for (User user : usersData.users) {
                if (user.name.equals(name)){
                    return user;
                }
            }
            return null;
        }
    }
    public static void save(Object json,File target){
        try {
            FileOutputStream fos = new FileOutputStream(target);
            fos.write(new Gson().toJson(json).getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

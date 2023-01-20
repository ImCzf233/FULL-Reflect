package al.nya.verify;

import java.util.Date;

public class Log {
    public static void info(String s){
        Date date = new Date();
        System.out.println("\033[32m[INFO]["+date+"] "+s+"\033[0m");
    }
    public static void exp(Exception e){
        System.out.print("\033[33m[EXCEPTION] ");
        e.printStackTrace();
    }
    public static void error(String e){
        System.out.println("\033[31m[ERROR] "+e+"\033[0m");
    }
}

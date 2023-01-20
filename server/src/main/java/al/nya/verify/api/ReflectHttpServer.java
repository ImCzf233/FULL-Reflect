package al.nya.verify.api;

import al.nya.verify.Log;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ReflectHttpServer{
    private static HttpServer server;
    public static void init(){
        try {
            server = HttpServer.create(new InetSocketAddress(6660), 0);
            server.createContext("/", new ReflectHttpHandler());
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            Log.info("Api Start");
        } catch (IOException e) {
            e.printStackTrace();
            Log.exp(e);
        }
    }
    public static void stop(){
        server.stop(0);
        Log.info("Api Stop");
    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by closestudios on 10/4/15.
 */
public class ProxyServer {

    static HashMap<String, CachedData> cache;
    static boolean cacheLock = false;
    static long CACHE_SEC_LIMIT = 60000;
    static boolean logLock = false;

    public static void main(String[] args) throws IOException {

        try {
            loadCache();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        ServerSocket server = null;
        boolean serverRunning = true;

        int port = 9090; // Default
        try { // Try to get args
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {

        }

        try {
            server = new ServerSocket(port);
            System.out.println("Started Server Listening to Port: " + port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(-1);
        }

        while (serverRunning) {
            Socket acceptedSocket = server.accept();
            new ProxyThread(acceptedSocket, acceptedSocket.getRemoteSocketAddress()).start();
        }
        server.close();

    }

    public static void loadCache() throws IOException, ClassNotFoundException {
        File file = new File("cache.bytes");
        if(file.exists()) {
            System.out.println("Loaded Cache!");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            HashMap<String, CachedData> c = (HashMap<String, CachedData>) s.readObject();
            s.close();
            cache = c;
        } else {
            System.out.println("New Cache!");
            cache = new HashMap<>();
        }
    }

    public static boolean addToCache(String key, byte[] data) throws IOException {
        if(cacheLock) {
            return false;
        }
        cacheLock = true;
        cache.put(key, new CachedData(data));
        File file = new File("cache.bytes");
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(cache);
        s.close();
        cacheLock = false;
        return true;
    }

    public static boolean removeCache(String key) throws IOException {
        if(cacheLock) {
            return false;
        }
        cacheLock = true;
        cache.remove(key);
        File file = new File("cache.bytes");
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(cache);
        s.close();
        return true;
    }

    public static boolean logRequest(String browserIP, String url, int size) throws IOException, ClassNotFoundException {
        if(logLock) {
            return false;
        }
        System.out.println("Logging!");
        logLock = true;
        //Date: browserIP URL size
        String log = "";
        File file = new File("proxy.log");
        if(file.exists()) {
            System.out.println("Loaded Log!");
            FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f);
            String c = (String) s.readObject();
            s.close();
            log = c;
        }
        String newLog = "\n" + new Date().toGMTString() + ": " + browserIP + " " + url + " " + size;
        System.out.println("Log: " + newLog);
        log += newLog;

        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(log);
        s.close();

        logLock = false;
        return true;
    }

    public static HashMap<String, CachedData> getCache() {
        return cache;
    }

    public static class CachedData implements Serializable {

        public byte[] data;
        public long when;

        public CachedData() {

        }

        public CachedData(byte[] data) {
            this.data = data;
            when = new Date().getTime();
        }

        public boolean stillValid() {
            long hasBeen = new Date().getTime() - when;
            System.out.println("Cache Last: " + hasBeen);
            return hasBeen  < CACHE_SEC_LIMIT;
        }
    }


}

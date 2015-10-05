import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by closestudios on 10/4/15.
 */
public class ProxyThread extends Thread{

    private Socket socket = null;
    private SocketAddress clientAddress = null;

    public ProxyThread(Socket socket, SocketAddress address) {
        super("ProxyThread");
        this.socket = socket;
        this.clientAddress = address;
    }


    public void run() {

        try {
            // Print out Client
            System.out.println("Client Connected: " + clientAddress.toString());

            // Set up data streams
            OutputStream outToClient = socket.getOutputStream();
            InputStream inFromClient = socket.getInputStream();


            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            boolean dataOver = false;
            while(!dataOver) {
                int length = inFromClient.read(data, 0, data.length);
                if(length != -1) {
                    buffer.write(data, 0, length);
                }
                if(length == -1 || inFromClient.available() == 0) {
                    dataOver = true;
                }
            }

            buffer.flush();
            byte[] requestFromClient = buffer.toByteArray();
            String requestFromClientString = new String(requestFromClient);
            System.out.println("Request Data From Client: " + requestFromClientString);
            String[] lines = requestFromClientString.split(System.getProperty("line.separator"));

            // Get Request
            Request request = new Request();
            for(int i = 0;i < lines.length;i++) {

                String inputLine = lines[i];
                System.out.println("Read Request Line: " + inputLine);
                StringTokenizer tok = new StringTokenizer(inputLine);

                if(tok.hasMoreTokens()) {
                    String token = tok.nextToken();
                    if (token != null && token.equals("GET")) {
                        request.setURL(tok.nextToken());
                        System.out.println("Found GET");
                    } else {
                        if(tok.hasMoreTokens()) {
                            request.setHeaders(token, tok.nextToken());
                        }
                    }
                }

            }

            if(request.isValidRequest()) {

                if(isCached(request)) {
                    System.out.println("Use Cache!");
                    outToClient.write(getCache(request));
                    outToClient.flush();
                } else {

                    // Create socket to server
                    System.out.println("Host Name: " + request.getURL());
                    InetAddress address = InetAddress.getByName(request.getURL());
                    String ipAddress = address.getHostAddress();
                    System.out.println("IP Address: " + ipAddress);

                    Socket serverSocket = new Socket(address, 80);

                    OutputStream outToServer = serverSocket.getOutputStream();
                    InputStream inFromServer = serverSocket.getInputStream();

                    outToServer.write(requestFromClient);


                    ByteArrayOutputStream returnDataFromServer = new ByteArrayOutputStream();
                    byte[] dataFrom = new byte[10240];
                    int bytes_read;
                    while ((bytes_read = inFromServer.read(dataFrom)) != -1) {
                        System.out.println("Return Data From Server: " + bytes_read);
                        System.out.println("Return String From Server: " + new String(dataFrom));
                        outToClient.write(dataFrom, 0, bytes_read);
                        outToClient.flush();
                        returnDataFromServer.write(dataFrom, 0, bytes_read);
                    }

                    returnDataFromServer.flush();
                    cacheData(returnDataFromServer.toByteArray(), request);

                    System.out.println("Closing Server Socket");
                    serverSocket.close();
                }

            }


            System.out.println("Closing Socket");
            if (socket != null) {
                socket.close();
            }


        } catch (Exception e) {
            System.out.println("Exception Here: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cacheData(byte[] data, Request request) throws InterruptedException {

        System.out.println("Cache Data: " + data.length + ", For Request: " + request.url);

        try {
            while(!ProxyServer.addToCache(request.url, data)) {
                Thread.sleep(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isCached(Request request) throws InterruptedException {
        if(ProxyServer.getCache().containsKey(request.url)) {
            if(ProxyServer.getCache().get(request.url).stillValid()) {
                return true;
            } else {
                System.out.println("Clear Cache Data For Request: " + request.url);
                try {
                    while(!ProxyServer.removeCache(request.url)) {
                        Thread.sleep(10);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        } else {
            return false;
        }
    }

    private byte[] getCache(Request request) {
        return ProxyServer.getCache().get(request.url).data;
    }

    public class Request {

        public String url;
        public HashMap<String, String> headers = new HashMap<>();

        public Request() {

        }

        public void setURL(String url) {
            this.url = url;
        }

        public boolean isValidRequest() {
            return url != null;
        }

        public void setHeaders(String key, String data) {
            headers.put(key, data);
        }

        public String getURL() throws MalformedURLException {
            return new URL(url).getHost();
        }
    }

}

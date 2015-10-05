import java.io.*;
import java.net.*;
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
                System.out.println("Read: " + inputLine);
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
                // Create socket to server

                System.out.println("Host Name: " + request.getURL());
                InetAddress address = InetAddress.getByName(request.getURL());
                String ipAddress = address.getHostAddress();
                System.out.println("IP Address: " + ipAddress);

                Socket serverSocket = new Socket(address,80);

                OutputStream outToServer = serverSocket.getOutputStream();
                InputStream inFromServer = serverSocket.getInputStream();

                outToServer.write(requestFromClient);

                byte[] dataFrom = new byte[1024];
                int bytes_read;
                while ((bytes_read = inFromServer.read(dataFrom)) != -1) {
                    System.out.println("Return Data From Server: " + bytes_read);
                    outToClient.write(dataFrom, 0, bytes_read);
                    outToClient.flush();
                }

                serverSocket.close();

            }


            if (socket != null) {
                socket.close();
            }


        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

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

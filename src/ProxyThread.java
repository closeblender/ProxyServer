import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
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
            int nRead;
            byte[] data = new byte[1024];

            System.out.println("Reading Data: " + inFromClient.read(data, 0, data.length));
            System.out.println("Reading Data2: " + inFromClient.read(data, 0, data.length));
            System.out.println("Reading Data3: " + inFromClient.read(data, 0, data.length));


            buffer.flush();
            byte[] requestFromClient = buffer.toByteArray();
            System.out.println("Read Data: " + requestFromClient.length);

            /*


            // Get Request
            String inputLine = "";
            Request request = new Request();
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Read: " + inputLine);
                StringTokenizer tok = new StringTokenizer(inputLine);

                if(tok.nextToken().equals("GET")) {
                    request.setURL(tok.nextToken());
                    System.out.println("Found GET");
                }

            }

            if(request.isValidRequest()) {
                // Create socket to server

                Socket serverSocket = new Socket(request.getURL,80);

                OutputStream outToServer = serverSocket.getOutputStream();
                InputStream inFromServer = serverSocket.getInputStream();

                outToServer.write(requestFromClient);


            }

*/


            if (socket != null) {
                socket.close();
            }


        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public class Request {

        public String getURL;
        public HashMap<String, String> headers = new HashMap<>();

        public Request() {

        }

        public void setURL(String url) {
            getURL = url;
        }

        public boolean isValidRequest() {
            return getURL != null;
        }
    }

}

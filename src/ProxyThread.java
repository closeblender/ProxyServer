import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

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

            // Get all the bytes from the client
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inFromClient.read(data, 0, data.length)) != -1) {
                System.out.println("Reading");
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            byte[] inputFromClient = buffer.toByteArray();

            System.out.println("In From Client Size: " + inputFromClient.length);

            String requestFromClient = new String(inputFromClient);

            System.out.println("Request: " + requestFromClient);


            if (socket != null) {
                socket.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

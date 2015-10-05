import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by closestudios on 10/4/15.
 */
public class ProxyServer {



    public static void main(String[] args) throws IOException {

        ServerSocket server = null;
        boolean serverRunning = true;

        int port = 9090; // Default
        try { // Try to get args
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            //use default
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

}

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



    }


}

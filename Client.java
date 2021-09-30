import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 *
 * @author mathieu.fabre
 */

public class Client extends Thread{

    //adresse du serveur
    private final String HOST = "127.0.0.1";
    //port du serveur
    private final int PORT = 6699;

    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    public Client(ClientUI ui, String ip, int port, String username) {
        try {
            selector = Selector.open();
            // connecte le client au serveur
            socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            //  Register CHANNEL to Selector, register a read event
            socketChannel.register(selector, SelectionKey.OP_READ);
            this.username = username;
            System.out.println(username + " connecté!");
            ClientUI.setStatus("En ligne");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //méthode qui envoie un message au server
    public void addMessage(String msg) {
        msg = username + ": " + msg;
        try {
            socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //méthode qui lit les message recu par le serveur
    private void readMsg() {
        try {
            int count = selector.select();
            if (count > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        
                        String msg = new String(buffer.array());
                        System.out.println(msg.trim());
                        ClientUI.appendMessage(msg);
                    }
                }
                iterator.remove();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() { 
        while (true) {
            readMsg();
        }
    }

        
}

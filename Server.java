import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * Processus serveur qui ecoute les connexion entrantes,
 * les messages entrant et les rediffuse au clients connectes
 *
 * @author mathieu.fabre
 */

public class Server extends Thread{
   
    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

	private static final String HOST = "127.0.0.1";
    private static final int PORT = 6699;

    //constructeur de la class Server qui ouvre le serveur sur le port PORT et l'ip HOST
    public Server(ServerUI serv, String ip, int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            
            serverSocketChannel.socket().bind(new InetSocketAddress(HOST, PORT));
            
            serverSocketChannel.configureBlocking(false);
            
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //met le status à Joignable
			ServerUI.setStatus("Joingnable");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //méthode run du thread server
    public void run() {
        ServerUI.log("Serveur prêt, \n Gestion des messages: \n");
        try {
            while (true) {
                int count = selector.select();
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    SelectionKey key = null;
                    //tant que iterator n'est pas vide
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        //si le client est acceptable on le connecte au serveur
                        if (key.isAcceptable()) {
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            
                            socketChannel.configureBlocking(false);
                            
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            
							ServerUI.log(socketChannel.getRemoteAddress () + " connecté\n");
                        }
                        if (key.isReadable()) {
                            readData(key);
                        }
                        iterator.remove();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //méthode qui lit les messages envoyé par un client et broadcast ce message à tous les autres clients
    private void readData(SelectionKey key) {
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int read = channel.read(byteBuffer);
            if (read > 0) {
                String msg = new String(byteBuffer.array());
                ServerUI.log("FORM " + msg);
                //broadcast le messaege sur le channel
                broadcast(msg, channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //si une exeption est levé le client est déconnecté
            try {
                ServerUI.log(channel.getRemoteAddress() + "déconecté ..");
                key.cancel();
                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    //méthode qui envoie un message à toutes les personnes connecté sur ce channel
    private void broadcast(String msg, SocketChannel channel) throws IOException {
        ServerUI.log("-> transmissions des données ...");
        //pour chaque personne connecté
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            // sauf la perssonne qui a envoyé le message
            if (targetChannel instanceof SocketChannel && targetChannel != channel) {
                SocketChannel dest = (SocketChannel) targetChannel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                //  écrit le message dans le channel
                dest.write(buffer);
                ServerUI.log("envoyé à: " + dest.getRemoteAddress()+ "\n");
            }
        }
    }

}
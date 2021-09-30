import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

/**
 * Interface graphique du client
 *
 * @author mathieu.fabre
 */
public class ClientUI extends Application implements EventHandler {

    private TextField ip;
    private TextField port;
    private TextField nickname;
    private Button connect;
    private Button disconnect;
    private static TextArea textArea;
    private TextField input;
    private static Label status;

    /**
     * Le thread client
     */
    private Client client;

    /**
     * Indique si le client tourne
     */
    private boolean running = false;

    public void start(Stage stage) throws Exception {

        // Border pane et scene
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);

        // ZOne haute pour la connection
        ToolBar toolBar = new ToolBar();
        ip = new TextField("127.0.0.1");
        port = new TextField("6699");
        nickname = new TextField("pseudo");
        connect = new Button("Connect");
        connect.setOnAction(this);
        disconnect = new Button("Disconnect");
        disconnect.setOnAction(this);
        toolBar.getItems().addAll(ip, port, nickname, connect, disconnect);
        borderPane.setTop(toolBar);

        // Zone centrale de log de tchat
        textArea = new TextArea();
        borderPane.setCenter(textArea);

        // ZOne basse pour la xone de texte et le statut
        VBox bottomBox = new VBox();
        status = new Label("Hors ligne");
        input = new TextField();
        input.addEventFilter(KeyEvent.KEY_RELEASED, this);
        bottomBox.getChildren().addAll(input, status);
        borderPane.setBottom(bottomBox);

        // Statut initial deconnecte
        setDisconnectedState();

        stage.setTitle("Client de tchat");
        stage.show();
    }

    /**
     * Mets l'IHM dans le staut deconnecte
     */
    public void setDisconnectedState() {
        ip.setDisable(false);
        port.setDisable(false);
        connect.setDisable(false);
        disconnect.setDisable(true);
        input.setDisable(true);
    }

    public void setConnectedState() {
        ip.setDisable(true);
        port.setDisable(true);
        connect.setDisable(true);
        disconnect.setDisable(false);
        input.setDisable(false);
    }

    /**
     * Indique si le client tourne
     *
     * @return
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Ajout de message dans le log
     *
     * @param message
     */
    public static void appendMessage(String message) {
        textArea.appendText(message);
    }

    /**
     * Change le ;essage de statut
     *
     * @param message
     */
    public static void setStatus(String message) {
        status.setText(message);
    }

    /**
     * Connexion au serveur
     * @throws InterruptedException
     * @throws IOException
     * @throws NumberFormatException
     */
    public void connectToServer() throws NumberFormatException, IOException, InterruptedException {

        if (ip.getText().trim().length() == 0) {
            setStatus("Veuillez entrer une adresse IP valide");
            return;
        }

        if (port.getText().trim().length() == 0) {
            setStatus("Veuillez entrer un port valide");
            return;
        }

        if (nickname.getText().trim().length() == 0) {
            setStatus("Veuillez entrer un nickname valide");
            return;
        }

        // Demarrage de la connexion au serveur
        client = new Client(this, ip.getText(), Integer.parseInt(port.getText()), nickname.getText());
        client.start();

        // Changement de l etat du client
        running = true;

        // Changement d etat de l'IHM
        setConnectedState();
    }

    /**
     * Deconnexion
     * on passe le statut a false et on attends
     * que le thread se deconnecte
     */
    public void disconnectFromServer() {
        this.running = false;
    }

    /**
     * Prise en charge des events
     *
     * @param event
     */
    public void handle(Event event) {

        if (event.getSource() == connect) {
            try {
                connectToServer();
            } catch (NumberFormatException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == disconnect) {
            disconnectFromServer();
        } else if (event.getSource() == input) {
            try {
                processEnter((KeyEvent)event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Envoi le message si l utilisateur
     * appui sur la touche entree
     *
     * @param event
     * @throws IOException
     */
    public void processEnter(KeyEvent event) throws IOException {

        // Envoi du texte si on appui sur entree et que le contenu n est pas vide
        if (event.getCode() == KeyCode.ENTER && input.getText().trim().length() > 0) {
            client.addMessage(input.getText() + System.getProperty("line.separator"));
            appendMessage("Moi: " + input.getText() + "\n");
            input.setText("");
        }
    }

    /**
     * Demarrage du client
     *
     * @param args
     */
    public static void main(String [] args) {
        launch(args);
    }
}

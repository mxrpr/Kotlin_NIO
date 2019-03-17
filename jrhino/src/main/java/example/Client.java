package example;

import com.mix.rhino.ClientNetworkService;
import com.mix.rhino.IErrorReceiver;
import com.mix.rhino.IMessageReceiver;

import java.io.IOException;
import java.nio.channels.Channel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple client implementation for NetworkService
 *
 * @author Ferenc Tollas
 */
public final class Client implements IMessageReceiver, IErrorReceiver{
    private ClientNetworkService clientNetworkService = null;
    private Logger logger = Logger.getLogger("Client");

    private Client(){}

    private void initialize() throws IOException {
        this.clientNetworkService = new ClientNetworkService("127.0.0.1", 4545);
//        this.clientNetworkService.start();
        this.clientNetworkService.registerErrorReceiver(this);
        this.clientNetworkService.registerReceiver(this);
    }

    private void sendTestMessage(){
        logger.log(Level.INFO, "Sending message");
        this.clientNetworkService.sendMessage("Hello from client");
        logger.log(Level.INFO, "Sending message done");
    }

    private void terminate(){
        this.clientNetworkService.terminate();
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.initialize();
            client.sendTestMessage();
            client.terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkErrorOccurred(String errorMessage) {
        System.out.println("Error:" + errorMessage);
    }

    @Override
    public void messageArrived(Channel channel, String message) {
        System.out.println("Message arrived:" + message);
    }
}

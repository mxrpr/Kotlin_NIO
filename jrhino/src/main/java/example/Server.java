package example;

import com.mix.rhino.IErrorReceiver;
import com.mix.rhino.IMessageReceiver;
import com.mix.rhino.NetworkService;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;

/**
 * Simple example of the NetworkService implementation usage
 *
 * @author Ferenc Tollas
 */
public final class Server implements IMessageReceiver, IErrorReceiver{

    private  NetworkService networkService = null;

    private Server(){
    }

    private void init() throws IOException {
        this.networkService = new NetworkService(4545);
        this.networkService.registerReceiver(this);
        this.networkService.registerErrorReceiver(this);
//        this.networkService.start();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                networkService.terminate();
            }
        });
    }

    public static void main(String[] args) {
        Server server = new  Server();
        try {
            server.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void networkErrorOccurred(String errorMessage) {
        System.out.println("Network error:"  + errorMessage);
    }

    @Override
    public void messageArrived(Channel channel, String message) {
        try {
            System.out.println("Message arrived from " + ((SocketChannel)channel).getRemoteAddress().toString() + " message:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Terminates the network connection
     */
    public void terminate(){
        this.networkService.terminate();
    }

}

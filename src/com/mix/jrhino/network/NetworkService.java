package com.mix.jrhino.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NetworkService implementation is responsible for
 * creating the server socket and listen for the
 * clients.
 *
 * @author Ferenc Tollas
 */
public final class NetworkService extends ClientNetworkService {

    private int portToListen = 6666;
    private Selector selector = null;
    private ServerSocketChannel serverChannel = null;

    /**
     * Constructor.
     *
     * @param portToListen Port used to listen
     * @throws IOException
     */
    public NetworkService(final int portToListen) throws IOException{
        this.portToListen = portToListen;
        this.initialize();
    }

    /**
     * Initialize the NetworkService
     *
     * @throws IOException
     */
    private void initialize() throws IOException{
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(this.portToListen));
        this.serverChannel.configureBlocking(false);
        this.serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    public void run(){
        // TODO remove in final version
        System.out.println("Running server NIO..");
        try {
            while (this.terminate == false) {
                int i = this.selector.select();

                if (i == 0) {
                    continue;
                }

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        // accept connection
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                    }

                    if (key.isWritable()){
                        if (!this.messageSenderQueue.isEmpty()){
                            String msg = this.messageSenderQueue.poll();
                            this.writeToChannel(key, msg);
                        }
                    }

                    if (key.isReadable()) {
                        this.readChannel(key);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: in case of exception, then server should not terminate, the error must be handled
            this.errorReceiver.networkErrorOccurred(e.getMessage());
            e.printStackTrace();
        }
    }

}

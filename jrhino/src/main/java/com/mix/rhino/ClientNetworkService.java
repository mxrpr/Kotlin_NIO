package com.mix.rhino;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * ClientNetworkService class is responsible to keep the connection
 * with the server
 *
 * @author  Ferenc Tollas
 */
public class ClientNetworkService  {

    private String serverAddress = null;
    private int serverPort = 6666;
    boolean terminate = false;
    private Selector selector = null;
    private SocketChannel channel = null;
    BlockingDeque<String> messageSenderQueue;
    IMessageReceiver messageReceiver = null;
    private Hashtable<SocketChannel, ByteBuffer> buffers = null;
    IErrorReceiver errorReceiver = null;

    /**
     * Default constructor is responsible to initialize
     * the buffers and the queues.
     *
     * @throws IOException
     */
    protected ClientNetworkService() throws IOException{
        this.buffers = new Hashtable<>();
        this.messageSenderQueue = new LinkedBlockingDeque<>();
    }

    public int getMessageQueueSize() {
        return this.messageSenderQueue.size();
    }
    /**
     * Constructor, initializes the buffers and queues
     *
     * @param serverAddress Address of the server. The client will try to connect to this server
     * @param serverPort  Port used by the server
     *
     * @throws IOException
     */
    public ClientNetworkService(final String serverAddress, final int serverPort) throws IOException{
        this();
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.initialize();
    }

    /**
     * Registers message receivers. In case a message is received, then the
     * message receiver will be notifies with the new message
     *
     * @param messageReceiver The message receiver
     * @see IMessageReceiver
     */
    public void registerReceiver(final IMessageReceiver messageReceiver){
        this.messageReceiver = messageReceiver;
//        this.messageSenderWithQueue.setReceiver(this.messageReceiver);
    }

    public void registerErrorReceiver(final IErrorReceiver errorReceiver){
        this.errorReceiver = errorReceiver;
    }

    /**
     * Initialize the channel
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.connect(new InetSocketAddress(this.serverAddress, this.serverPort));
        this.selector = Selector.open();

        this.channel.register(this.selector, SelectionKey.OP_CONNECT);
    }

    /**
     * Terminates the network connection
     */
    public void terminate(){
        this.terminate = true;
//        this.messageSenderWithQueue.terminate();
    }

    /**
     * Sends message to the connected server
     *
     * @param message String
     */
    public synchronized void sendMessage(final String message){
        this.messageSenderQueue.add(message);
    }

    public void run(){
        try {
            while (!this.terminate) {
                int i = this.selector.select();
                if (i == 0)
                    continue;
                Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    SocketChannel channel = (SocketChannel) key.channel();
                    keys.remove();
                    if (key.isConnectable()) {
                        if (channel.isConnectionPending()) channel.finishConnect();
                        channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    }
                    if (key.isReadable()) {
                        this.readChannel(key);
                    }
                    if (key.isWritable()) {
                        synchronized (this){
                            if (!this.messageSenderQueue.isEmpty()) {
                                String msg = this.messageSenderQueue.poll();
                                this.writeToChannel(key, msg);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { // IOException, CancelledKeyException..
            e.printStackTrace();
            this.errorReceiver.networkErrorOccurred(e.getMessage());
        }
        finally{
            // close channel
            try {
                System.out.println("Closing channel...");
                channel.close();
                this.selector.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Read message from channel
     *
     * @param key SelectionKey
     * @throws IOException
     * @see SelectionKey
     */
    void readChannel(SelectionKey key) throws IOException{
        SocketChannel socketChannel = (SocketChannel)key.channel();
        ByteBuffer readBuffer = this.buffers.get(socketChannel);
        if (readBuffer == null){
            readBuffer = ByteBuffer.allocate(500);
            this.buffers.put(socketChannel, readBuffer);
        }
        readBuffer.clear();

        int count = socketChannel.read(readBuffer);
        if (count == -1) {
            this.closeChannel(key);
            return;
        }
        byte[] data = new byte[count];
        System.arraycopy(readBuffer.array(), 0, data, 0, count);
        this.messageReceiver.messageArrived(socketChannel, new String(data, StandardCharsets.UTF_8 ));
        readBuffer.clear();
    }

    /**
     * Write to channel information
     *
     * @param key
     * @param message Message
     * @throws IOException
     */
    void writeToChannel(SelectionKey key, final String message) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        SocketChannel channel = (SocketChannel) key.channel();
        channel.write(buffer);
    }

    /**
     * Close a given channel
     *
     * @param key SelectionKey
     * @throws IOException
     * @see SelectionKey
     */
    private void closeChannel(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel)key.channel();
        socketChannel.close();
        key.cancel();
        this.buffers.remove(socketChannel);
    }
}

package com.mix.jrhino.network;

import java.nio.channels.Channel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Class which is a spacial layer between the
 * network service and the registered message received (which implements IMessageReceiver)
 *
 * Message sender is used by the network layer to notify the registered listeners about
 * the received messages. The implementation forces the async notification model, to allow
 * the networking to be able to send /receive high volume of messages from the clients.
 *
 * @author  Ferenc Tollas
 */
public class MessageSenderWithQueue extends Thread{

    private IMessageReceiver messageReceiver = null;
    private Queue<Pair> queue = null;
    private boolean terminate = false;

    class Pair{
        Channel channel;
        String message;

        Pair(Channel channel, String m){
            this.channel = channel;
            this.message = m;
        }
    }

    public MessageSenderWithQueue(){
        this.queue = new ConcurrentLinkedDeque<Pair>();
    }

    /**
     * Sets the message receiver
     *
     * @param messageReceiver
     * @see IMessageReceiver
     */
    public void setReceiver(final IMessageReceiver messageReceiver){
        this.messageReceiver = messageReceiver;
    }


    /**
     * Message processing
     */
    public void run() {
        while (this.terminate == false) {
            synchronized (this.queue) {
                while ( this.queue.isEmpty()) {
                    if (this.terminate == true)
                        break;
                    try {
                        this.queue.wait();
                    } catch (InterruptedException e) {}
                }
            }
            if (!this.queue.isEmpty()){
                Pair pair = this.queue.poll();
                // notify the listener
                if (this.messageReceiver != null) {
                    this.messageReceiver.messageArrived(pair.channel, pair.message);
                }
            }
        }
    }

    /**
     * Adds message to the queue
     *
     * @param channel The Channel on which the message was received
     * @param data Message string
     */
    protected void addMessage(final Channel channel, final String data) {
        this.queue.add(new Pair(channel, data));

        synchronized (this.queue){
            this.queue.notifyAll();
        }
    }

    /**
     * Terminates the message processing
     */
    public void terminate(){
        this.terminate = true;
        synchronized (this.queue){
            this.queue.notifyAll();
        }
    }
}

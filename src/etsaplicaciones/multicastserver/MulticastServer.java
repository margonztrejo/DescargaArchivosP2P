/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.multicastserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class MulticastServer implements Runnable {
    private final String message;
    private final String ip;
    private final int port;
    private final int sendSecondsInterval;
    private Thread t;
    
    public MulticastServer(String ip, int port) {
        this.message = ip + "," + port;
        this.sendSecondsInterval = 5;
        this.ip = ip;
        this.port = port;
    }
    
    public void startSending() {
        this.t = new Thread(this);
        this.t.start();
    }
    
    private void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(ipAddress);
            byte[] msg = message.getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
            socket.send(packet);
            System.out.println("Sending: " + message);
        }
    }

    @Override
    public void run() {
        while(true) {
            try {
                sendUDPMessage(this.message, this.ip, this.port);
                TimeUnit.SECONDS.sleep(this.sendSecondsInterval);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(MulticastServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.multicastClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class MulticastClient implements Runnable {
    private final String ip;
    private final int port;
    private Thread t;
    
    public MulticastClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    
    public void startListening() {
        this.t = new Thread(this);
        this.t.start();
    }  

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        try (MulticastSocket socket = new MulticastSocket(this.port)) {
            InetAddress group = InetAddress.getByName(this.ip);
            socket.joinGroup(group);
            
            while(true) {
                System.out.println("Waiting for multicast message...");
                
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                
                System.out.println("[Multicast UDP message received]" + msg);
            }
        } catch (IOException ex) {
            Logger.getLogger(MulticastClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

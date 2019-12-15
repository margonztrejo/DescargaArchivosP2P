/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.serverfinderclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marco
 */
public class ServerFinderClient extends Thread{
    private static BufferedReader input = null;
    private final String IP = "228.1.1.1";
    
    @Override
    public void run() {
        try{
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName(IP),1234);
            Selector selector  = Selector.open();
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(addr);
            sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            input = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                if(selector.select() > 0){
                    Boolean doneStatus= processReadySet(selector.selectedKeys());
                    if(doneStatus)
                        break;
                }
            }
        }catch(Exception e){
        }
    }
    
    public static Boolean processReadySet(Set readySet) throws IOException{
        SelectionKey key = null;
        Iterator iterator = null;
        iterator = readySet.iterator();
        
        while(iterator.hasNext()){
            key = (SelectionKey) iterator.next();
            iterator.remove();
        }
        
        if(key.isConnectable()){
            Boolean connected = proccessConnect(key);
            if(!connected){
                return true;
            }
        }
        
        if(key.isReadable()){
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer bb = ByteBuffer.allocate(1024);
            sc.read(bb);
            String result = new String(bb.array()).trim();
            System.out.println("Message received from Server: " + result + " Message length= " + result.length());
        }
        
        if(key.isWritable()){
            System.out.print("Type a message (type quit to stop): ");
            String msg = input.readLine();
            
            if(msg.equalsIgnoreCase("quit")){
                return true;
            }
            
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer bb = ByteBuffer.wrap(msg.getBytes());
            sc.write(bb);
        }
        return false;
    }
    
    public static Boolean proccessConnect(SelectionKey key){
        SocketChannel sc = (SocketChannel)key.channel();
        try{
            while(sc.isConnectionPending()){
                sc.finishConnect();
            }
        }catch(IOException e){
            key.cancel();
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

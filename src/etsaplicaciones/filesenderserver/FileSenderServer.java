/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filesenderserver;

import etsaplicaciones.searchserver.ServerAvailable;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class FileSenderServer implements Runnable {
    
    public FileSenderServer(ServerAvailable node) {
        this.node = node;
        portDownload = node.port + 100;
    }
    
    public void startListening() {
        this.t = new Thread(this);
        this.t.start();
    }

    @Override
    public void run() {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        ServerSocket servsock = null;
        Socket cliSock = null;
        
        try {
            servsock = new ServerSocket(portDownload);
            while (true) {
                System.out.println("Waiting...");
                try {
                    cliSock = servsock.accept();
                    System.out.println("Accepted connection: " + cliSock);
                    
                    // Get Parameters
                    BufferedReader br = new BufferedReader(new InputStreamReader(cliSock.getInputStream()));
                    String [] params = br.readLine().split(",");
//                    int port = Integer.valueOf(params[0]);
                    String fileName = params[0];
                    int totalParts = Integer.valueOf(params[1]);
                    int numPart = Integer.valueOf(params[2]);
                    System.out.println("Total parts: " + totalParts);
                    System.out.println("Num part: " + numPart);
                    
                    // Send file
                    File f = new File("C:\\ets\\" + this.node.port + "\\" + fileName);
                    if (f.exists()) {
                        int numBytes =  (int)f.length() / totalParts;
                        int initByte = numBytes * numPart;
                        System.out.println("Num bytes: " + numBytes);
                        byte [] mybytearray  = new byte[(int)f.length()];
                        fis = new FileInputStream(f);
                        bis = new BufferedInputStream(fis);
                        bis.read(mybytearray, initByte, numBytes);
                        os = cliSock.getOutputStream();
                        System.out.println("Sending " + fileName + "(" + mybytearray.length + " bytes)");
                        os.write(mybytearray, 0, mybytearray.length);
                        os.flush();
                        System.out.println("Done.");
                    }
                }
                finally {
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (cliSock != null) cliSock.close();
                }
            }
        }
        catch (IOException ex) {
            Logger.getLogger(FileSenderServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
          if (servsock != null) try {
              servsock.close();
          } catch (IOException ex) {
              Logger.getLogger(FileSenderServer.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
    }
    
    private final ServerAvailable node;
    private Thread t;
    private int portDownload;
}

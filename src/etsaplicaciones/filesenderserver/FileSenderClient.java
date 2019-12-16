/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filesenderserver;

import etsaplicaciones.searchserver.ServerAvailable;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class FileSenderClient implements Runnable {
    private final ServerAvailable node;
    private String fileName;
    private int port;
    private int part;
    private int totalParts;
    private Thread t;
    PartOfFileDownloadListener listener;
    
    public FileSenderClient(ServerAvailable node, PartOfFileDownloadListener listener) {
        this.node = node;
        this.listener = listener;
    }
    
    public void downloadFile(String fileName, int port, int part, int totalParts) {
        this.fileName = fileName;
        this.port = port;
        this.part = part;
        this.totalParts = totalParts;
        this.t = new Thread(this);
        this.t.start();
    }

    @Override
    public void run() {
        int bytesRead;
        int current = 0;
        Socket cliSock = null;
        try {
            cliSock = new Socket("localhost", port);
            System.out.println("Connecting...");

            PrintWriter outServ = new PrintWriter(cliSock.getOutputStream(), true);
            outServ.println(fileName + "," + totalParts + "," + part);

            byte [] mybytearray  = new byte [2000000];
            InputStream is = cliSock.getInputStream();
            bytesRead = is.read(mybytearray, 0, mybytearray.length);
            current = bytesRead;
            
            do {
                bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                  if(bytesRead >= 0)
                      current += bytesRead;
            } while(bytesRead > -1);
            
            int bytesForLoop = current;
            byte [] bytesToSave = new byte[current];
            
            for(int i = 0; i < bytesForLoop; i ++){
                bytesToSave[i] = mybytearray[i];
            }

            System.out.println("File " + this.fileName + " downloaded (" + current + " bytes read)");
            listener.downloaded(port, bytesToSave);
        } catch (IOException ex) {
            Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                cliSock.close();
            } catch (IOException ex) {
                Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

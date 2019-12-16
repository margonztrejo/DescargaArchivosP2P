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
    private final ServerAvailable nextNode;
    private String fileName;
    private int [] ports;
    private Thread t;
    
    public FileSenderClient(ServerAvailable node, ServerAvailable nextNode) {
        this.node = node;
        this.nextNode = nextNode;
    }
    
    public void downloadFile(String fileName, int [] ports) {
        this.fileName = fileName;
        this.ports = ports;
        
        this.t = new Thread(this);
        this.t.start();
    }

    @Override
    public void run() {
        int bytesRead;
        int current = 0;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        Socket [] cliSocks = new Socket[this.ports.length];
        
        for (int i = 0; i < this.ports.length; i++) {
            try {
                cliSocks[i] = new Socket("localhost", this.ports[i]);
                System.out.println("Connecting...");

                // Send file name
                PrintWriter outServ = new PrintWriter(cliSocks[i].getOutputStream(), true);
                outServ.println(this.fileName + "," + this.ports.length + "," + i);

                // Receive file
                byte [] mybytearray  = new byte [65535];
                InputStream is = cliSocks[i].getInputStream();
                fos = new FileOutputStream("C:\\ets\\" + this.node.port + "\\" + this.fileName);
                bos = new BufferedOutputStream(fos);
                bytesRead = is.read(mybytearray, 0, mybytearray.length);
                current = bytesRead;

                do {
                    bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                      if(bytesRead >= 0)
                          current += bytesRead;
                } while(bytesRead > -1);

                bos.write(mybytearray, 0 ,current);
                bos.flush();
                System.out.println("File " + this.fileName + " downloaded (" + current + " bytes read)");
            } catch (IOException ex) {
                Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
            }        finally {
                try {
                    if (fos != null) fos.close();
                    if (bos != null) bos.close();
                    if (cliSocks[i] != null) cliSocks[i].close();
                } catch (IOException ex) {
                    Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

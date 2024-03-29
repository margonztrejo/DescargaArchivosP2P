/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filefinderserver;

import etsaplicaciones.searchserver.ServerAvailable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Marco
 */
public class FileFinderClient implements Runnable {
    private final ServerAvailable node;
    private String fileName;
    private Thread t;
    private final IFindFileResponse listener;
    private int portSource;
    
    public FileFinderClient(int portSource, ServerAvailable node, IFindFileResponse listener) {
        this.portSource = portSource;
        this.node = node;
        this.listener = listener;
    }
    
    public void askForFile(String fileName) throws IOException {
        this.fileName = fileName;
        
        this.t = new Thread(this);
        this.t.start();
    }
    
    @Override
    public void run() {
        try
        {
            System.out.println("Conectando a " + node.port + "...");
            System.out.println("Conectando a " + node.ip + "...");
            Socket cliSock = new Socket(node.ip, node.port);
            
            //Flujo de datos hacia el servidor
            PrintWriter outServ = new PrintWriter(cliSock.getOutputStream(), true);

            //Se obtiene el flujo entrante desde el servidor
            BufferedReader br = new BufferedReader(new InputStreamReader(cliSock.getInputStream()));
            System.out.println(br.readLine());

            outServ.println(this.fileName + "," + this.portSource);
            String msgServ = br.readLine();
            System.out.println("Server response: " + msgServ);
            
            this.listener.findFileResponse(msgServ);
            
            cliSock.close();//Fin de la conexión
            System.out.println("Client: Socket close");
        }
        catch (Exception e)
        {
            System.out.println("Client: " + e);
        }
    }
}

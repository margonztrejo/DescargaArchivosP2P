/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filefinderserver;

import etsaplicaciones.ETSAplicaciones;
import etsaplicaciones.searchserver.ServerAvailable;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class FileFinderServer implements Runnable {
    private final int port;
    private final ServerAvailable nextNode;
    private Thread t;
    private ServerSocket servSock;
    private PrintWriter outCli;
    
    public FileFinderServer(int port, ServerAvailable nextNode) {
        this.port = port;
        this.nextNode = nextNode;
        createDirectoryIfNotExist();
    }
    
    private void createDirectoryIfNotExist(){
        File file = new File("C:\\ets\\" + this.port);
        file.mkdirs();
    }
    
    public void startListening() throws IOException {
        this.t = new Thread(this);
        this.t.start();
    }
    
    private void askForFileToNextNode(String fileName) {
        FileFinderClient ffc = new FileFinderClient(this.nextNode, (String message) -> {
            if (!message.isEmpty()) {
                File f = new File("C:\\ets\\" + this.port + "\\" + fileName);
                if (f.exists())
                    message += "," + String.valueOf(this.port);
            }
            
            this.outCli.println(message);
            
            try {
                this.stop();
            } catch (IOException ex) {
                Logger.getLogger(FileFinderServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        try {
            ffc.askForFile("asdf.txt");
        } catch (IOException ex) {
            Logger.getLogger(ETSAplicaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void stop() throws IOException {
        this.servSock.close();
    }
    
    @Override
    public void run() {
        try
        {
            this.servSock = new ServerSocket(this.port);
            System.out.println("Esperando...");
            
            //Accept comienza el socket y espera una conexión desde un cliente
            Socket cliSock = servSock.accept();

            System.out.println("Cliente en línea");
            
            // Se obtiene el flujo de salida del cliente para enviarle mensajes
            this.outCli = new PrintWriter(cliSock.getOutputStream(), true);

            // Se le envía un mensaje al cliente usando su flujo de salida
            this.outCli.println("Petición recibida y aceptada");

            // Se obtiene el flujo entrante desde el cliente
            BufferedReader br = new BufferedReader(new InputStreamReader(cliSock.getInputStream()));

            // Se lee mensaje desde el cliente
            String [] params = br.readLine().split(",");
            String fileName = params[0];
            int portCli = Integer.valueOf(params[1]);
            
            if (portCli == this.port) {
                this.askForFileToNextNode(fileName);
            } else {
                this.stop();
            }

            System.out.println("Fin de la conexión");
        }
        catch (Exception e)
        {
            System.out.println("Server: " + e.getMessage());
            System.out.println(e);
        }
    }
}

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
import java.io.ObjectOutputStream;
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
    private ServerAvailable nextNode;
    private Thread t;
    private ServerSocket servSock; 
    private Socket clientSocket;
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
    
    public void setNewNextNode(ServerAvailable nextNode){
        this.nextNode = nextNode;
    }
    
    private void askForFileToNextNode(String fileName, int portSource) {
        FileFinderClient ffc = new FileFinderClient(portSource, this.nextNode, (String message) -> {
            
            if(iHaveTheFile(fileName)){
                message += "," + getPortForDownload();
            }
                
            this.outCli.println(message);
            
            try {
                this.stop();
            } catch (IOException ex) {
                Logger.getLogger(FileFinderServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        try {
            ffc.askForFile(fileName);
        } catch (IOException ex) {
            Logger.getLogger(ETSAplicaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void stop() throws IOException {
        this.servSock.close();
    }
    
    private Boolean iHaveTheFile(String fileName){
        File f = new File("C:\\ets\\" + this.port + "\\" + fileName);
        return f.exists();
    }
    
    private int getPortForDownload(){
        return this.port + 100;
    }
    
    @Override
    public void run() {
        try
        {
            while(true){
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
                String message = br.readLine();
                String [] params = message.split(",");
                String fileName = params[0];
                int portCli = Integer.valueOf(params[1]);

                if (portCli == this.nextNode.port) {
                    if(iHaveTheFile(fileName)){
                        this.outCli.println(getPortForDownload()+"");
                    }else{
                        this.outCli.println("");
                        
                    }
                    this.stop();
                } else {
                    this.askForFileToNextNode(fileName, portCli);
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Server: " + e.getMessage());
        }
    }
}

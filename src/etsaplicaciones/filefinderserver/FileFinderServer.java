/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filefinderserver;

import etsaplicaciones.ETSAplicaciones;
import etsaplicaciones.ShowEventListener;
import etsaplicaciones.searchserver.ServerAvailable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class FileFinderServer implements Runnable {
    
    public FileFinderServer(int port, ServerAvailable nextNode, ShowEventListener listener, String myIp) {
        this.myIp = myIp;
        this.eventListener = listener;
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
            eventListener.showEvent("Respuesta del nodo con puerto" + nextNode.port + ": " + message + "\n");
            if(iHaveTheFile(fileName)){
                if(message.isEmpty()){
                    message += getResponse(fileName);
                }else{
                    message += "," + getResponse(fileName);
                }
                eventListener.showEvent("El nodo del puerto " +  portSource + " solicitó el archivo " + fileName + ". Se encuentra disponible"  + "\n");
            }else{
                eventListener.showEvent("El nodo del puerto " +  portSource + " solicitó el archivo " + fileName + ". No se encuentra disponible"  + "\n");
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
    
    public static byte[] obtenerChecksum(String nombreArchivo) throws Exception {
        InputStream fis = new FileInputStream(nombreArchivo);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        // Leer el archivo pedazo por pedazo
        do {
            // Leer datos y ponerlos dentro del búfer
            numRead = fis.read(buffer);
            // Si se leyó algo, se actualiza el MessageDigest
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        // Devolver el arreglo de bytes
        return complete.digest();
    }

    public static String obtenerMD5ComoString(String nombreArchivo) throws Exception {
        // Convertir el arreglo de bytes a cadena
        byte[] b = obtenerChecksum(nombreArchivo);
        StringBuilder resultado = new StringBuilder();

        for (byte unByte : b) {
            resultado.append(Integer.toString((unByte & 0xff) + 0x100, 16).substring(1));
        }
        return resultado.toString();
    }
    
    private String getResponse(String fileName){
        String response = "";
        try{
            response += getPortForDownload();
            response += ":" + myIp;
            
            String pathFile = "C:\\ets\\" + this.port + "\\" + fileName;
            String checksum = FileFinderServer.obtenerMD5ComoString(pathFile);
            response += ":" + checksum;
            
        }catch(Exception e){
        
        }
        return response;
    }
    
    /*
    @Override
    public void run(){
        try{
            InetAddress host = InetAddress.getLocalHost();
            Selector selector = Selector.open();

            ServerSocketChannel serverSocketChannel  = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            SelectionKey key = null;
            
            while(true){
                if(selector.select() <= 0)
                    continue;
            
                Set<SelectionKey> selectedKey = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKey.iterator();
            
                while(iterator.hasNext()){
                    key = (SelectionKey) iterator.next();
                    iterator.remove();
                    if(key.isAcceptable()){
                        SocketChannel sc = serverSocketChannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.OP_WRITE);
                        System.out.println("Connection Accepted: " + sc.getLocalAddress() + "\n");
                    }
                    if(key.isReadable()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer bb = ByteBuffer.allocate(1024);
                        sc.read(bb);
                        String result = new String(bb.array()).trim();
                        System.out.println("Message received: " + result + " Message length= " + result.length());
                    }
                    if(key.isWritable()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        String message = "Petición recibida y aceptada";
                        ByteBuffer buffer = ByteBuffer.allocate(message.length());
                        buffer.put(message.getBytes());
                        sc.write(buffer);
                        sc.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
            
        }catch(Exception e){
            
        }
    }*/
    
    
    @Override
    public void run() {
        try {
            this.servSock = new ServerSocket(this.port);
        } catch (IOException ex) {
            Logger.getLogger(FileFinderServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(true){
            try
            {
                if (this.servSock.isClosed()) {
                    this.servSock = new ServerSocket(this.port);
                }
                
                System.out.println("Esperando en puerto " + this.port + "...");
                
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
                        this.outCli.println(getResponse(fileName));
                        eventListener.showEvent("El nodo del puerto " +  portCli + " solicitó el archivo " + fileName + ". Se encuentra disponible" + "\n");
                    }else{
                        this.outCli.println("");
                        eventListener.showEvent("El nodo del puerto " +  portCli + " solicitó el archivo " + fileName + ". No se encuentra disponible" + "\n");
                    }
                } else {
                    eventListener.showEvent("Se le ha preguntado al nodo con puerto " + nextNode.port + " si tiene el archivo" + "\n");
                    this.askForFileToNextNode(fileName, portCli);
                }
            } catch (Exception e) {
                System.out.println("FFServer: " + e.getMessage());
            }
        }
    }
    
    private final int port;
    private ServerAvailable nextNode;
    private Thread t;
    private ServerSocket servSock; 
    private Socket clientSocket;
    private PrintWriter outCli;
    private ShowEventListener eventListener;
    private String myIp;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.searchserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Marco
 */
public class SearchServer extends Thread {
    private final int port; //Puerto para la conexión
    protected String msgServ; //Mensajes entrantes (recibidos) en el servidor
    protected ServerSocket servSock; //Socket del servidor
    protected Socket cliSock; //Socket del cliente
    protected DataOutputStream salidaServidor, salidaCliente; //Flujo de datos de salida
       
    public SearchServer(int port) {
        this.port = port;
    }
    
    public void searchFile(String fileName) throws IOException {
        this.servSock = new ServerSocket(this.port);
        this.cliSock = new Socket();
    }
    
    @Override
    public void run() {
        try
        {
            System.out.println("Esperando...");
            this.cliSock = this.servSock.accept(); //Accept comienza el socket y espera una conexión desde un cliente

            System.out.println("Cliente en línea");
            // Se obtiene el flujo de salida del cliente para enviarle mensajes
            salidaCliente = new DataOutputStream(this.cliSock.getOutputStream());

            //Se le envía un mensaje al cliente usando su flujo de salida
            salidaCliente.writeUTF("Petición recibida y aceptada");

            //Se obtiene el flujo entrante desde el cliente
            BufferedReader entrada = new BufferedReader(new InputStreamReader(this.cliSock.getInputStream()));

            //Mientras haya mensajes desde el cliente
            while((msgServ = entrada.readLine()) != null) {
                System.out.println(msgServ);
            }

            System.out.println("Fin de la conexión");
            this.servSock.close();//Se finaliza la conexión con el cliente
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
}

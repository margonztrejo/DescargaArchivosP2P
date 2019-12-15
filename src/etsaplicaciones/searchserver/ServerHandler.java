/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.searchserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Marco
 */
public class ServerHandler {
    
    public ServerHandler(int myPort, String myIP){
        setNewServerAvailable(myPort, myIP);
        //Aquí se van a lanzar los dos hilos
    }
    
    public void setNewServerAvailable(int port, String ip){
        String ID = generateID(port, ip);
        ServerAvailable server = new ServerAvailable(ID, port, ip);
        putNewServer(server);
        listener.ListHasBeenUpdated();
    }
    
    private void putNewServer(ServerAvailable server){
        servers.add(server);
        servers.sort((ServerAvailable o1, ServerAvailable o2) -> {
            if(o1.port > o2.port)
                return 1;
            else
                return -1;
        });
    }
    
    private String generateID(int port, String ip){
        return ip + port;
    }
    
    private ArrayList<ServerAvailable> servers;
    private IServerAvailable listener;
    
}

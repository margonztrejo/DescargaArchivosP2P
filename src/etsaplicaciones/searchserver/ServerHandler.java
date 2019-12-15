/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.searchserver;

import etsaplicaciones.multicastClient.INodeAdded;
import etsaplicaciones.multicastClient.MulticastClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Marco
 */
public class ServerHandler implements INodeAdded{
    
    public ServerHandler(int portGroup, int myPort, String myIP, IServerAvailable listener){
        this.listener = listener;
        setNewServerAvailable(myPort, myIP, true);
        
        MulticastClient multicastClient = new MulticastClient(myIP, portGroup, this);
        multicastClient.startListening();
        
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                checkIfServersAreStillAvailable();
                obtainPreviousAndNext();
                listener.ListHasBeenUpdated(servers, previous, next);
            }
        };
        
        Timer t = new Timer();
        t.schedule(timerTask, 0, 1000);
    }
    
    public void setNewServerAvailable(int port, String ip, Boolean isLocal){
        ServerAvailable server = new ServerAvailable(port, ip);
        if(isLocal){
            myID = server.ID;
        }
        putNewServer(server);
        obtainPreviousAndNext();
        listener.ListHasBeenUpdated(servers, previous, next);
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
    
    private void obtainPreviousAndNext(){
        int maxIndex = servers.size() - 1;
        if(servers.isEmpty())
            return;
        
        if(servers.size() == 1){
            previous = servers.get(0);
            next = servers.get(0);
        }else{
            for(int i = 0; i < servers.size(); i++){
                if(servers.get(i).ID == null ? myID == null : servers.get(i).ID.equals(myID)){
                    if(i == 0){
                        previous = servers.get(maxIndex);
                        next = servers.get(i + 1);
                    }else if(i == maxIndex){
                        previous = servers.get(i - 1);
                        next = servers.get(0);
                    }else{
                        previous = servers.get(i - 1);
                        next = servers.get(i + 1);
                    }
                }
            }
        }
    }
    
    public ServerAvailable getPrevious(){
        return previous;
    }
    
    public ServerAvailable getNext(){
        return next;
    }
    
    public ArrayList<ServerAvailable> getServers(){
        return servers;
    }
    
    private void checkIfServersAreStillAvailable(){
        ArrayList<ServerAvailable> delete = new ArrayList<ServerAvailable>();
        for(int i = 0; i < servers.size(); i++){
            if(servers.get(i).getTimer() <= 0){
                delete.add(servers.get(i));
            }
        }
        for(int i = 0; i < delete.size(); i++){
            servers.remove(delete.get(i));
        }
    }
    
    @Override
    public void nodeAdded(ServerAvailable serverAvailable) {
        for(int i = 0; i < servers.size(); i++){
            if(servers.get(i).ID == null ? serverAvailable.ID == null : servers.get(i).ID.equals(serverAvailable.ID)){
                servers.get(i).resetTimer();
                return;
            }
        }
        setNewServerAvailable(serverAvailable.port, serverAvailable.ip, false);
    }
    
    private ArrayList<ServerAvailable> servers = new ArrayList<ServerAvailable>();
    private IServerAvailable listener;
    private String myID;
    private ServerAvailable previous = null;
    private ServerAvailable next = null;
    
}

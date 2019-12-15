/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.searchserver;

import java.util.TimerTask;
import java.util.Timer;

/**
 *
 * @author Marco
 */
public class ServerAvailable {
    
    public ServerAvailable(String ID, int port, String ip){
        this.ID = ID;
        this.port = port;
        this.ip = ip;
        this.timer = 11;
        
        TimerTask timerTask = new TimerTask(){
            @Override
            public void run() {
                timer -= 1;
            }
        };
        Timer t = new Timer();
        t.schedule(timerTask, 15,1000);
    }
    
    public void setID(String ID){
        this.ID = ID;
    }
    
    public String getID(){
        return ID;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public int getPort(){
        return port;
    }
    
    public void setIP(String ip){
        this.ip = ip;
    }
    
    public String getIP(){
        return ip;
    }
    
    public String ID;
    public int port;
    public String ip;
    public int timer;
    
  
}

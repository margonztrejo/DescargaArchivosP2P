/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones;

import etsaplicaciones.filesenderserver.FileSenderClient;
import etsaplicaciones.filesenderserver.FileSenderServer;
import etsaplicaciones.searchserver.ServerAvailable;

/**
 *
 * @author Marco
 */
public class ETSAplicaciones{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        initFrames();
        /*FileSenderServer fss = new FileSenderServer(new ServerAvailable(9000, "localhost"));
        fss.startListening();
        
        FileSenderClient fsc = new FileSenderClient(new ServerAvailable(9000, "localhost"), new ServerAvailable(9000, "localhost"));
        fsc.downloadFile("asdf.txt", new int[]{ 9000 });*/
    }
    
    private static void initFrames(){
        mainFrame = new MainFrame();
        initNodeFrame = new InitNodeFrame(new IAskForPort(){
            @Override
            public void PortHasBeenInitialized(int port) {
                mainFrame.setPort(port);
                initNodeFrame.setVisible(false);
                mainFrame.setVisible(true);
            }
            
        });
        
        mainFrame.setVisible(false);
        initNodeFrame.setVisible(true);
    }
   
    
    private static MainFrame mainFrame;
    private static InitNodeFrame initNodeFrame;
}

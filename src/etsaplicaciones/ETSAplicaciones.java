/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones;

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
    }
    
    private static void initFrames(){
        mainFrame = new MainFrame();
        initNodeFrame = new InitNodeFrame((int port) -> {
            mainFrame.setPort(port);
            initNodeFrame.setVisible(false);
            mainFrame.setVisible(true);
        });
        
        mainFrame.setVisible(false);
        initNodeFrame.setVisible(true);
    }
   
    
    private static MainFrame mainFrame;
    private static InitNodeFrame initNodeFrame;
}

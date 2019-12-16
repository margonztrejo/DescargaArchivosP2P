/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones;

import etsaplicaciones.filefinderserver.FileFinderClient;
import etsaplicaciones.filefinderserver.FileFinderServer;
import etsaplicaciones.filefinderserver.IFindFileResponse;
import etsaplicaciones.filesenderserver.FileSenderClient;
import etsaplicaciones.filesenderserver.FileSenderServer;
import etsaplicaciones.filesenderserver.PartOfFile;
import etsaplicaciones.filesenderserver.PartOfFileDownloadListener;
import etsaplicaciones.multicastserver.MulticastServer;
import etsaplicaciones.searchserver.IServerAvailable;
import etsaplicaciones.searchserver.ServerAvailable;
import etsaplicaciones.searchserver.ServerHandler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Marco
 */
public class MainFrame extends javax.swing.JFrame implements IServerAvailable, IFindFileResponse, PartOfFileDownloadListener, ShowEventListener {

    /**
     * Creates new form Main
     */
    public MainFrame() {
        initComponents();
    }
    
    public void setPort(int port){
        this.port = port;
        try{
            myIp = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e){
            System.out.println("Get IP: " + e.getMessage());
        }
        
        jLabel2.setText("" + port);
        initMulticast();
        initFileFinderServer();
        initFileSenderServer();
        showAllMyFiles();
    }
    
    private void showAllMyFiles(){
        if((new File("C:\\ets\\" + port).exists())){
            try (Stream<Path> walk = Files.walk(Paths.get("C:\\ets\\" + port))) {
            List<String> result = walk.filter(Files::isRegularFile)
                            .map(x -> x.getFileName().toString()).collect(Collectors.toList());
            
            String filesString = "";
            for(int i = 0; i < result.size(); i++){
                filesString += result.get(i) + "\n";
            }
            
            jTextPane4.setText(filesString);
	} catch (IOException e) {
            e.printStackTrace();
	}
        }
    }
    
    private void initFileFinderServer(){
        fileFinderServer = new FileFinderServer(port, next, this, myIp);
        try{
            fileFinderServer.startListening();
        }catch(Exception e){
        }
    }
    
    private void initFileSenderServer(){
        fileSenderServer = new FileSenderServer(new ServerAvailable(port, myIp));
        fileSenderServer.startListening();
    }
            
    private void askForFile(){
        jButton1.setEnabled(false);
        fileName = jTextField1.getText();
        
        if(iHaveTheFile(fileName)){
            notifyMessage("El archivo se encuentra disponible en esta carpeta");
            jButton1.setEnabled(true);
        }else{
            fileFinderClient = new FileFinderClient(port, next, this);
            try{
                fileFinderClient.askForFile(fileName);
            }catch(Exception e){
            }
        }
    }
    
    private Boolean iHaveTheFile(String fileName){
        File f = new File("C:\\ets\\" + this.port + "\\" + fileName);
        return f.exists();
    }
    
    private void downloadFile(){
        totalParts = 0;
        for(int i = 0; i < partsOfFile.size(); i++){
            if(partsOfFile.get(i).getOption() == option){
                totalParts++;
            }
        }
        jButton2.setEnabled(false);
        listOfDownloadClient = new ArrayList();
        for(int i = 0; i < partsOfFile.size(); i++){
            if(partsOfFile.get(i).getOption() == option){
                FileSenderClient fsc = new FileSenderClient(new ServerAvailable(port, myIp), this);
                listOfDownloadClient.add(fsc);
                fsc.downloadFile(fileName, partsOfFile.get(i).getPort(), partsOfFile.get(i).getPartNumber(), totalParts);
            }
        }
    }
    
    private void initMulticast(){
        MulticastServer multicastServer = new MulticastServer(ipGroup, port, portGroup);
        multicastServer.startSending();
        serverHandler = new ServerHandler(portGroup, ipGroup, port, myIp, this);
    }
    
    private String getServersString(ArrayList<ServerAvailable> servers){
        String value = "";
        for(int i = 0; i < servers.size(); i++){
            value += servers.get(i).ID + "\n";
        }
        return value;
    }
    
    private void notifyMessage(String message){
        this.message += message;
        jTextPane1.setText(this.message);
    }
    
    private void checkIfAllPartsDownloaded(){
        Boolean allDownloads = true;
        for(int i = 0; i < partsOfFile.size(); i++){
            if(partsOfFile.get(i).getOption() == option){
                allDownloads = allDownloads && partsOfFile.get(i).getDownloaded();
            }
        }
        if(allDownloads){
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            
            try{
                fos = new FileOutputStream("C:\\ets\\" + port + "\\" + this.fileName);
                bos = new BufferedOutputStream(fos);
                int totalLen = 0;
                for(int i = 0; i < partsOfFile.size(); i++){
                    if(partsOfFile.get(i).getOption() == option){
                        totalLen += partsOfFile.get(i).getPart().length;
                    }
                }
                byte [] file = new byte [totalLen];
                int index = 0;
                for(int i = 0; i < partsOfFile.size(); i++){
                    if(partsOfFile.get(i).getOption() == option){
                        byte [] b = partsOfFile.get(i).getPart();
                        for(int j = 0; j < b.length; j ++){
                            file[index] = b[j];
                            index++;
                        }
                    }
                }
                
                bos.write(file, 0 ,file.length);
                bos.flush();
                showAllMyFiles();
            } catch (IOException ex) {
                Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (fos != null) fos.close();
                    if (bos != null) bos.close();
                } catch (IOException ex) {
                    Logger.getLogger(FileSenderClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            jButton2.setEnabled(true);
        }
    }
    
    private void orderPartsOfFile(){
        if(partsOfFile.size() > 0){
            String portsString = "";
            partsOfFile.sort((PartOfFile p1, PartOfFile p2) -> {
                return p1.getMD5().compareTo(p2.getMD5());
            });


            String currentMD5 = partsOfFile.get(0).getMD5();
            int options = 1;
            int part = 1;
            partsOfFile.get(0).setPartNumber(part);
            partsOfFile.get(0).setOption(options);
            portsString += "Opción " + options + "\n";
            portsString += part + ".-" + partsOfFile.get(0).getIp().replace(".", "") + partsOfFile.get(0).getPort() + "\n";
            part++;
            for(int i = 1; i < partsOfFile.size(); i++){
                if(currentMD5 == null ? partsOfFile.get(i).getMD5() == null : currentMD5.equals(partsOfFile.get(i).getMD5())){
                    portsString += part + ".-" + partsOfFile.get(i).getIp().replace(".", "") + partsOfFile.get(i).getPort() + "\n";
                    partsOfFile.get(i).setPartNumber(part);
                    partsOfFile.get(i).setOption(options);
                    part++;
                }else{
                    part = 1;
                    options ++;
                    portsString += "Opción " + options + "\n";
                    portsString += part + ".-" + partsOfFile.get(i).getIp().replace(".", "") + partsOfFile.get(i).getPort() + "\n";
                    currentMD5 = partsOfFile.get(i).getMD5();
                    partsOfFile.get(i).setPartNumber(part);
                    partsOfFile.get(i).setOption(options);
                    part++;
                }
            }
            jTextPane3.setText(portsString);
        }
    }
    
    @Override
    public void showEvent(String event) {
        notifyMessage(event);
    }
    
    @Override
    public void downloaded(int port, byte [] partOfFile) {
        for(int i = 0; i < partsOfFile.size(); i ++){
            if(partsOfFile.get(i).getPort() == port){
                partsOfFile.get(i).setPart(partOfFile);
                partsOfFile.get(i).hasBeenDownloaded();
            }
        }
        checkIfAllPartsDownloaded();
    }
    
    @Override
    public void ListHasBeenUpdated(ArrayList<ServerAvailable> servers, ServerAvailable previous, ServerAvailable next) {
        if(this.previous != previous){
            if(this.previous != null){
                notifyMessage("El nodo previo ha cambiado de " + this.previous.ID + " a " + previous.ID + "\n");
            }
            this.previous = previous;
        }
        if(this.next != next){
            if(this.next != null){
                notifyMessage("El nodo siguiente ha cambiado de " + this.next.ID + " a " + next.ID + "\n");
            }
            this.next = next;
            if(fileFinderServer != null)
                fileFinderServer.setNewNextNode(next);
        }
        
        jTextPane2.setText(getServersString(servers));
        jLabel7.setText(previous.ID);
        jLabel9.setText(next.ID);
    }
    
    @Override
    public void findFileResponse(String message) {
        if(message.isEmpty()){
            jTextPane3.setText("Archivo no encontrado");
            jButton2.setEnabled(false);
        }else{
            partsOfFile = new ArrayList();
            String [] ports = message.split(",");
            for(int i = 0; i < ports.length; i++){
                String [] res = ports[i].split(":");
                partsOfFile.add(new PartOfFile(Integer.parseInt(res[0]), res[1], res[2], i + 1));
            }
            orderPartsOfFile();
            jButton2.setEnabled(true);
        }
        jButton1.setEnabled(true);
    }
   
    private int port = -1;
    private String ipGroup = "228.1.1.1";
    private String myIp;
    private int portGroup = 8020;
    private ServerHandler serverHandler;
    private FileFinderServer fileFinderServer;
    private FileFinderClient fileFinderClient;
    private FileSenderServer fileSenderServer;
    private ServerAvailable previous;
    private ServerAvailable next;
    private String fileName;
    private ArrayList<PartOfFile> partsOfFile;
    private ArrayList<FileSenderClient> listOfDownloadClient;
    private String message = "";
    private int totalParts;
    private int option;
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Título");

        jLabel1.setText("Puerto:");

        jLabel2.setText("XXXX");
        jLabel2.setName("lblPort"); // NOI18N
        jLabel2.setPreferredSize(new java.awt.Dimension(37, 16));

        jLabel3.setText("Eventos");

        jTextPane1.setEditable(false);
        jScrollPane1.setViewportView(jTextPane1);

        jLabel4.setText("Servidores");

        jTextPane2.setEditable(false);
        jScrollPane2.setViewportView(jTextPane2);

        jLabel5.setText("Buscar");
        jLabel5.setToolTipText("");

        jButton1.setText("Buscar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextPane3.setEditable(false);
        jScrollPane3.setViewportView(jTextPane3);

        jLabel6.setText("Nodo Anterior:");

        jLabel7.setText("????");

        jLabel8.setText("Nodo Siguiente:");

        jLabel9.setText("????");

        jButton2.setText("Descargar");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextPane4.setEditable(false);
        jScrollPane4.setViewportView(jTextPane4);

        jLabel10.setText("Mis archivos");

        jLabel11.setText("Elige archivo");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addGap(69, 69, 69)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel9)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel11)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton2))
                                    .addComponent(jScrollPane3)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton1)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane4)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addGap(0, 156, Short.MAX_VALUE)))))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jLabel11)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane4))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        askForFile();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        option = Integer.parseInt(jTextField2.getText());
        downloadFile();
    }//GEN-LAST:event_jButton2ActionPerformed

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JTextPane jTextPane4;
    // End of variables declaration//GEN-END:variables


}

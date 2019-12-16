/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.filesenderserver;

/**
 *
 * @author Marco
 */
public class PartOfFile {
    
    public PartOfFile(int port, int partNumber){
        this.port = port;
        downloaded = false;
        this.partNumber = partNumber;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public int getPort(){
        return port;
    }
    
    public void hasBeenDownloaded(){
        this.downloaded = true;
    }
    
    public Boolean getDownloaded(){
        return downloaded;
    }
    
    public void setPart(byte [] partOfFile){
        this.partOfFile = partOfFile;
    }
    
    public byte [] getPart(){
        return partOfFile;
    }
    
    public void setPartNumber(int partNumber){
        this.partNumber = partNumber;
    }
    
    public int getPartNumber(){
        return partNumber;
    }
    
    private int port;
    private Boolean downloaded;
    private byte [] partOfFile;
    private int partNumber;
}

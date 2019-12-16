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
public interface PartOfFileDownloadListener {
    public void downloaded(int port, byte [] partOfFile);
}

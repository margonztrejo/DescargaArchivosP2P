/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.multicastClient;

/**
 *
 * @author Marco
 */
public interface IMulticastMessageReceived {
    public void messageRecived(String ip, int port);
}

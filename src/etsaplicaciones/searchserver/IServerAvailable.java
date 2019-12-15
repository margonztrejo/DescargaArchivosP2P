/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package etsaplicaciones.searchserver;

import java.util.ArrayList;

/**
 *
 * @author Marco
 */
public interface IServerAvailable {
    public void ListHasBeenUpdated(ArrayList<ServerAvailable> servers, ServerAvailable previous, ServerAvailable next);
}

package org.academiadecodigo.whiledcards.client;

/**
 * @author albertoreis
 */
public interface ClientStatusListener {
    public void onLine(String username);
    public void offLine(String username);
}

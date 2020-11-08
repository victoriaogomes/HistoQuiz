package com.example.histoquiz.model;

/**
 *  Classe utilizada para armazenar as informações necessárias correspondentes a um amigo
 */
public class Friend {

    protected String UID;
    protected String name;


    /**
     * Método construtor da classe
     * @param name - nome do amigo
     * @param UID - identificador único desse amigo
     */
    public Friend(String name, String UID){
        this.name = name;
        this.UID = UID;
    }


    /**
     * Método que retorna o UID desse amigo
     * @return - UID
     */
    public String getUID() {
        return UID;
    }


    /**
     * Método utilizado para alterar o UID desse amigo
     * @param UID - novo UID
     */
    public void setUID(String UID) {
        this.UID = UID;
    }


    /**
     * Método utilizado para obter o nome desse amigo
     * @return - nome
     */
    public String getName() {
        return name;
    }


    /**
     * Método utilizado para modificar o nome desse amigo
     * @param name - novo nome
     */
    public void setName(String name) {
        this.name = name;
    }
}

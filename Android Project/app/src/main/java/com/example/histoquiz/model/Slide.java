package com.example.histoquiz.model;

import java.util.ArrayList;

/**
 * Classe utilizada para armazenar os dados relativos a uma lâmina
 */
public class Slide {

    protected String  name;
    protected int code;
    protected ArrayList<String> images;
    protected int system;

    /**
     * Método utilizado para obter o código relativo a essa lâmina
     * @return - código associado a essa lâmina
     */
    public int getCode() {
        return code;
    }


    /**
     * Método utilizado para alterar o código associado a essa lâmina
     * @param code - novo código
     */
    public void setCode(int code) {
        this.code = code;
    }


    /**
     * Método utilizado para obter as imagens associadas a essa lâmina
     * @return - arraylist contendo todas as imagens associadas a essa lâmina
     */
    public ArrayList<String> getImages() {
        return images;
    }


    /**
     * Método utilizado para alterar o conjunto de imagens associado a essa lâmina
     * @param images - arraylist contendo as novas imagens
     */
    public void setImages(ArrayList<String> images) {
        this.images = images;
    }


    /**
     * Método utilizado para obter o nome dessa lâmina
     * @return - nome da lâmina
     */
    public String getName() {
        return name;
    }


    /**
     * Método utilizado para alterar o nome dessa lâmina
     * @param name - novo nome
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Método utilizado para obter o inteiro que representa o código do sistema ao qual essa
     * lâmina esta relacionada
     * @return - código do sistema relacionado a essa lâmina
     */
    public int getSystem() {
        return system;
    }


    /**
     * Método utilizado para obter o inteiro que representa o código do sistema ao qual essa
     * lâmina esta relacionada
     * @param system - novo código do sistema relacionado a essa lâmina
     */
    public void setSystem(int system) {
        this.system = system;
    }
}

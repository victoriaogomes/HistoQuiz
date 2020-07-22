package com.example.histoquiz.model;

public class User {

    protected String nome, univers, anoIng, dataConta;


    public User(){

    }

    public User(String anoIng, String dataConta, String nome, String univers){
        this.anoIng = anoIng;
        this.dataConta = dataConta;
        this.nome = nome;
        this.univers = univers;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUnivers() {
        return univers;
    }

    public void setUnivers(String universidade) {
        this.univers = universidade;
    }

    public String getAnoIng() {
        return anoIng;
    }

    public void setAnoIng(String anoIngresso) {
        this.anoIng = anoIngresso;
    }
}

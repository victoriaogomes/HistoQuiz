package com.example.histoquiz.model;

/**
 * Classe utilizada para representar um usuário, contendo as principais informações pessoais relati-
 * vas a ele
 */
public class User {

    protected String nome, univers, anoIng, dataConta, registrationToken;

    /**
     * Construtor da classe que não recebe parâmetros
     */
    public User(){

    }


    /**
     * Construtor da classe que recebe como parâmetro todas as informações relativas a esse usuário
     * @param anoIng - ano de Ingresso desse usuário na faculdade
     * @param dataConta - data de criação dessa conta
     * @param nome - nome desse usuário
     * @param univers - universidade onde esse usuário estuda
     * @param registrationToken - token de registro desse usuário no firebase, usado para enviar
     *                            notificações especificamente para ele
     */
    public User(String anoIng, String dataConta, String nome, String univers, String registrationToken){
        this.anoIng = anoIng;
        this.dataConta = dataConta;
        this.nome = nome;
        this.univers = univers;
        this.registrationToken = registrationToken;
    }


    /**
     * Método utilizado para obter o nome desse usuário
     * @return - nome
     */
    public String getNome() {
        return nome;
    }


    /**
     * Método utilizado para alterar o nome desse usuário
     * @param nome - novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }


    /**
     * Método utilizado para obter a universidade desse usuário
     * @return - universidade
     */
    public String getUnivers() {
        return univers;
    }


    /**
     * Método utilizado para alterar a universidade desse usuário
     * @param universidade - nova universidade
     */
    public void setUnivers(String universidade) {
        this.univers = universidade;
    }


    /**
     * Método utilizado para obter o ano de ingressso na faculdade desse usuário
     * @return - ano de ingresso
     */
    public String getAnoIng() {
        return anoIng;
    }


    /**
     * Método utilizado para alterar o ano de ingressso na faculdade desse usuário
     * @param anoIngresso - novo ano de ingresso
     */
    public void setAnoIng(String anoIngresso) {
        this.anoIng = anoIngresso;
    }


    /**
     * Método utilizado para obter a data de criação da conta desse usuário
     * @return - data de criação da conta
     */
    public String getDataConta() {
        return dataConta;
    }


    /**
     * Método utilizado para alterar a data de criação da conta desse usuário
     * @param dataConta - nova data de criação da conta
     */
    public void setDataConta(String dataConta) {
        this.dataConta = dataConta;
    }


    /**
     * Método utilizado para obter o token de registro desse usuário no firebase
     * @return - RegistrationToken desse usuário
     */
    public String getRegistrationToken() {
        return registrationToken;
    }


    /**
     * Método utilizado para alterar o token de registro desse usuário no firebase
     * @param registrationToken - novo RegistrationToken
     */
    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }
}

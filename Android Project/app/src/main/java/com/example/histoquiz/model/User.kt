package com.example.histoquiz.model

/**
 * Classe utilizada para representar um usuário, contendo as principais informações pessoais relati-
 * vas a ele
 */
class User {
    /**
     * Método utilizado para obter o nome desse usuário
     * @return - nome
     */
    /**
     * Método utilizado para alterar o nome desse usuário
     * @param nome - novo nome
     */
    var nome: String? = null
    /**
     * Método utilizado para obter a universidade desse usuário
     * @return - universidade
     */
    /**
     * Método utilizado para alterar a universidade desse usuário
     * @param universidade - nova universidade
     */
    var univers: String? = null
    /**
     * Método utilizado para obter o ano de ingressso na faculdade desse usuário
     * @return - ano de ingresso
     */
    /**
     * Método utilizado para alterar o ano de ingressso na faculdade desse usuário
     * @param anoIngresso - novo ano de ingresso
     */
    var anoIng: String? = null
    /**
     * Método utilizado para obter a data de criação da conta desse usuário
     * @return - data de criação da conta
     */
    /**
     * Método utilizado para alterar a data de criação da conta desse usuário
     * @param dataConta - nova data de criação da conta
     */
    var dataConta: String? = null
    /**
     * Método utilizado para obter o token de registro desse usuário no firebase
     * @return - RegistrationToken desse usuário
     */
    /**
     * Método utilizado para alterar o token de registro desse usuário no firebase
     * @param registrationToken - novo RegistrationToken
     */
    var registrationToken: String? = null
    /**
     * Método utilizado para obter o nome da foto desse usuário no firebase
     * @return - photoName desse usuário
     */
    /**
     * Método utilizado para modificar o nome da foto desse usuário no firebase
     * @param  photoName - novo nome da foto desse usuário
     */
    var photoName: String? = null

    /**
     * Construtor da classe que não recebe parâmetros
     */
    constructor() {}

    /**
     * Construtor da classe que recebe como parâmetro todas as informações relativas a esse usuário
     * @param anoIng - ano de Ingresso desse usuário na faculdade
     * @param dataConta - data de criação dessa conta
     * @param nome - nome desse usuário
     * @param univers - universidade onde esse usuário estuda
     * @param registrationToken - token de registro desse usuário no firebase, usado para enviar
     * notificações especificamente para ele
     */
    constructor(anoIng: String?, dataConta: String?, nome: String?, univers: String?, registrationToken: String?) {
        this.anoIng = anoIng
        this.dataConta = dataConta
        this.nome = nome
        this.univers = univers
        this.registrationToken = registrationToken
    }
}
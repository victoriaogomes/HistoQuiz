package com.example.histoquiz.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import com.example.histoquiz.R
import com.google.android.material.textfield.TextInputLayout

class FormFieldValidator
/**
 * Método construtor da classe
 * @param context - contexto da activity que o criou
 */(var context: Context) {
    /**
     * Método utilizado para verificar se o campo recebido por parâmetro está preenchido ou vazio.
     * Caso esteja vazio, exibe um erro
     * @param field - parte do formulário que deve ser verificada
     * @return - retorna true caso esteja preenchido, e false caso esteja vazio
     */
    fun isFilled(field: TextInputLayout): Boolean {
        if (field.editText?.text.toString().isEmpty()) {
            showErrorInField(field)
            return false
        }
        return true
    }

    /**
     * Método para associar o campo exibido por parâmetro a um TextChangedListener, usado
     * para remover a mensagem de erro do campo quando o usuário começar a editá-lo
     * @param field - campo cuja edição deve ser monitorada
     */
    fun monitorField(field: TextInputLayout) {
        field.editText?.addTextChangedListener(object : TextWatcher {
            /**
             * Método chamado para notificar que um texto está prestes a ser modificado
             * @param s - sequência de letras dentro da qual haverá uma modificação
             * @param start - posição onde os caracteres começarão a ser modificados
             * @param count - quantidade de caracteres que será modificada
             * @param after - tamanho do novo texto que substituirá o atual
             */
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Antes do texto ser modificado, nada deve ser feito
            }

            /**
             * Método invocado no instante exato que o texto está sendo modificado, é usado
             * para definir se o form deve exibir um erro ou não
             * @param s - sequência de letras presente na caixa de pesquisa
             * @param start - posição a partir da qual se tem caracteres novos
             * @param before - tamanho que o texto tinha antes de ser modificado
             * @param count - quantidade de caracteres novos que foram digitados
             */
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (field.editText!!.text.toString().isEmpty()) {
                    showErrorInField(field)
                } else {
                    field.error = null
                }
            }

            /**
             * Método chamado para notificar que um texto foi alterado
             * @param s - texto que sofreu alteração em alguma posição
             */
            override fun afterTextChanged(s: Editable) {
                // Após o texto ser modificado, nada deve ser feito
            }
        })
    }

    /**
     * Método utilizado para exibir para o usuário que o campo recebido por parâmetro
     * está em branco e deve ser preenchido antes de prosseguir
     * @param campo - campo onde o erro deve ser exibido
     */
    fun showErrorInField(campo: TextInputLayout) {
        campo.setErrorIconTintList(ColorStateList.valueOf(Color.parseColor("#8B0000")))
        campo.setErrorTextColor(ColorStateList.valueOf(Color.parseColor("#8B0000")))
        campo.error = context.getString(R.string.obrigatorio)
    }
}
package com.example.histoquiz.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.histoquiz.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class FormFieldValidator {

    Context context;


    public FormFieldValidator(Context context){
        this.context = context;
    }


    /**
     * Método utilizado para verificar se o campo recebido por parâmetro está preenchido ou vazio.
     * Caso esteja vazio, exibe um erro
     * @param campo - parte do formulário que deve ser verificada
     * @return - retorna true caso esteja preenchido, e false caso esteja vazio
     */
    public boolean preenchido(TextInputLayout campo){
        if(Objects.requireNonNull(campo.getEditText()).getText().toString().isEmpty()){
            exibirErroNoCampo(campo);
            return false;
        }
        return true;
    }


    /**
     * Método para associar o campo exibido por parâmetro a um TextChangedListener, usado
     * para remover a mensagem de erro do campo quando o usuário começar a editá-lo
     * @param campo - campo cuja edição deve ser monitorada
     */
    public void monitorarCampo(final TextInputLayout campo){
        Objects.requireNonNull(campo.getEditText()).addTextChangedListener(new TextWatcher() {

            /**
             * Método chamado para notificar que um texto está prestes a ser modificado
             * @param s - sequência de letras dentro da qual haverá uma modificação
             * @param start - posição onde os caracteres começarão a ser modificados
             * @param count - quantidade de caracteres que será modificada
             * @param after - tamanho do novo texto que substituirá o atual
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(Objects.requireNonNull(campo.getEditText()).getText().toString().isEmpty()){
                    exibirErroNoCampo(campo);
                }
                else{
                    campo.setError(null);
                }
            }

            /**
             * Método chamado para notificar que um texto foi alterado
             * @param s - texto que sofreu alteração em alguma posição
             */
            @Override
            public void afterTextChanged(Editable s) {
                // Após o texto ser modificado, nada deve ser feito
            }
        });
    }


    /**
     * Método utilizado para exibir para o usuário que o campo recebido por parâmetro
     * está em branco e deve ser preenchido antes de prosseguir
     * @param campo - campo onde o erro deve ser exibido
     */
    public void exibirErroNoCampo(TextInputLayout campo){
        campo.setErrorIconTintList(ColorStateList.valueOf(Color.parseColor("#8B0000")));
        campo.setErrorTextColor(ColorStateList.valueOf(Color.parseColor("#8B0000")));
        campo.setError(context.getString(R.string.obrigatorio));
    }
}
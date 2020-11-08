package com.example.histoquiz.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.histoquiz.fragments.FriendsFragment;
import com.example.histoquiz.fragments.PerformanceFragment;
import com.example.histoquiz.fragments.ProfileFragment;

/**
 * Classe utilizada para exibir as informações relativas a conta desse usuário
 */
public class MyAccountAdapter extends FragmentStateAdapter {

    private final int numOfTabs;
    private final Context context;


    /**
     * Método construtor da classe
     * @param fm - FragmentManager da activity que instanciou essa classe
     * @param life - ciclo de vida determinado pela activity que instanciou essa classe
     * @param numOfTabs - número de tabs que haverão nessa tela
     * @param context - contexto passado pela activity que instanciou essa classe
     */
    public MyAccountAdapter(FragmentManager fm, Lifecycle life, int numOfTabs, Context context){
        super(fm, life);
        this.numOfTabs = numOfTabs;
        this.context = context;
    }


    /**
     * Método para criar o fragmento relativo a cada uma das aba de acordo com o código relacionado
     * a ela:
     *      - 0: aba perfil
     *      - 1: aba amigos/solicitações de amizade
     *      - 2: aba desempenho
     * @param position - posição da aba que será exibida (seu código)
     * @return - o fragmento que foi criado e que é responsável por manipular eventos nessa aba
     * específica que foi solicitada
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ProfileFragment();
            case 1:
                return new FriendsFragment(context);
            case 2:
                return new PerformanceFragment();
            default:
                return null;
        }
    }


    /**
     * Método responsável por retornar o número de tabs que a tela de minha conta possui
     * @return - quantidade de abas
     */
    @Override
    public int getItemCount() {
        return numOfTabs;
    }
}

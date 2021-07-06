package com.example.histoquiz.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.histoquiz.activities.MyAccountActivity
import com.example.histoquiz.fragments.FriendsFragment
import com.example.histoquiz.fragments.PerformanceFragment
import com.example.histoquiz.fragments.ProfileFragment

/**
 * Classe utilizada para exibir as informações relativas a conta desse usuário
 */
class MyAccountAdapter
/**
 * Método construtor da classe
 * @param fm - FragmentManager da activity que instanciou essa classe
 * @param life - ciclo de vida determinado pela activity que instanciou essa classe
 * @param numOfTabs - número de tabs que haverão nessa tela
 * @param context - contexto passado pela activity que instanciou essa classe
 */(fm: FragmentManager?, life: Lifecycle?, private val numOfTabs: Int, private val context: Context, var activity: MyAccountActivity) :
    FragmentStateAdapter(
        fm!!, life!!
    ) {
    /**
     * Método para criar o fragmento relativo a cada uma das aba de acordo com o código relacionado
     * a ela:
     * - 0: aba perfil
     * - 1: aba amigos/solicitações de amizade
     * - 2: aba desempenho
     * @param position - posição da aba que será exibida (seu código)
     * @return - o fragmento que foi criado e que é responsável por manipular eventos nessa aba
     * específica que foi solicitada
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileFragment(activity)
            1 -> FriendsFragment()
            2 -> PerformanceFragment()
            else -> ProfileFragment(activity)
        }
    }

    /**
     * Método responsável por retornar o número de tabs que a tela de minha conta possui
     * @return - quantidade de abas
     */
    override fun getItemCount(): Int {
        return numOfTabs
    }
}
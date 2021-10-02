package com.lenda.histoquiz.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lenda.histoquiz.databinding.FragmentPerformanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.math.ceil

/**
 * Classe responsável por exibir métricas relativas ao desempenho desse usuário no jogo
 */
class PerformanceFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    private var reproductorSis: Array<TextView>? = null
    private var digestSis: Array<TextView>? = null
    private var cardioSis: Array<TextView>? = null
    private var osteoSis: Array<TextView>? = null
    private var _screen: FragmentPerformanceBinding? = null
    private val screen get() = _screen!!

    /**
     * Método chamado no instante que um fragment desse tipo é instanciado, para que ele instancie
     * a view correspondente a ele na interface do usuário.
     * @param inflater - LayoutInflater que pode ser usado para exibir a view desse fragmento.
     * @param container - Se não for nulo, é a view pai à qual a view desse fragmento deve ser ane-
     * xada.
     * @param savedInstanceState - Se não for nulo, este fragmento está sendo reconstruído a partir
     * de um estado salvo anteriormente, conforme indicado nesse Bundle.
     * @return - retorna a view criada para esse fragmento.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _screen = FragmentPerformanceBinding.inflate(inflater, container, false)
        initGui()
        return screen.root
    }

    /**
     * Método utilizado para obter uma referência para os elementos da view que está sendo exibida,
     * que serão utilizados para mudar algumas de suas configurações. Além disso, inicializa algumas
     * variáveis que serão utilizadas.
     */
    fun initGui() {
        firestore = FirebaseFirestore.getInstance()
        reproductorSis = arrayOf(screen.erroSisRepro, screen.hitSisRepro)
        digestSis = arrayOf(screen.erroSisDiges, screen.hitSisDiges)
        cardioSis = arrayOf(screen.erroSisCardio, screen.hitSisCardio)
        osteoSis = arrayOf(screen.erroSisOsteo, screen.hitSisOsteo)
        screen.mainContent.visibility = View.INVISIBLE
        screen.progress.visibility  = View.VISIBLE
        getPerfomanceData()
    }

    /**
     * Método utilizado para obter do firebase os dados relativos a performance desse usuário nas
     * partidas que ele jogou
     */
    @Suppress("UNCHECKED_CAST")
    private fun getPerfomanceData() {
            firestore!!.document("desempenho/" + FirebaseAuth.getInstance().currentUser!!.uid).get().addOnSuccessListener {
                documentSnapshot: DocumentSnapshot ->
                setPerformanceData(
                    documentSnapshot["numPartidas"].toString(),
                    documentSnapshot["vitorias"].toString(),
                    documentSnapshot["sisCardiopulmonar"] as ArrayList<Long>?,
                    documentSnapshot["sisDigestorio"] as ArrayList<Long>?,
                    documentSnapshot["sisOsteomuscular"] as ArrayList<Long>?,
                    documentSnapshot["sisReprodutor"] as ArrayList<Long>?
                )
            }
        }

    /**
     * Método utilizado para exibir na interface as informações relativas ao desempenho do jogador
     * nas partidas que ele jogou
     * @param numMatches - número total de partidas jogadas pelo usuário
     * @param vitories - número de partidas que o usuário ganhou
     * @param sisCardio -dados relativos ao desempenho do jogador relativos ao sistema
     * Cardiopulmonar
     * @param sisDigest - dados relativos ao desempenho do jogador relativos ao sistema digestório
     * @param sisOsteo - dados relativos ao desempenho do jogador relativos ao sistema osteomuscular
     * @param sisRep - dados relativos ao desempenho do jogador relativos ao sistema reprodutor
     */
    @SuppressLint("SetTextI18n")
    fun setPerformanceData(numMatches: String, vitories: String, sisCardio: ArrayList<Long>?, sisDigest: ArrayList<Long>?,
                           sisOsteo: ArrayList<Long>?, sisRep: ArrayList<Long>?)
    {
        cardioSis?.get(0)?.text  = String.format(Locale.getDefault(), "%s", sisCardio!![0].toString())
        cardioSis?.get(1)?.text  = String.format(Locale.getDefault(), "%s", sisCardio[1].toString())
        digestSis?.get(0)?.text  = String.format(Locale.getDefault(), "%s", sisDigest!![0].toString())
        digestSis?.get(1)?.text  = String.format(Locale.getDefault(), "%s", sisDigest[1].toString())
        osteoSis?.get(0)?.text   = String.format(Locale.getDefault(), "%s", sisOsteo!![0].toString())
        osteoSis?.get(1)?.text   = String.format(Locale.getDefault(), "%s", sisOsteo[1].toString())
        reproductorSis?.get(0)?.text  = String.format(Locale.getDefault(), "%s", sisRep!![0].toString())
        reproductorSis?.get(1)?.text  = String.format(Locale.getDefault(), "%s", sisRep[1].toString())
        screen.playedMatches.text = numMatches
        if (numMatches.toInt() == 0) {
            screen.victoryProgress.progress = 0
            screen.defeatProgress.progress = 0
        } else {
            screen.victoryProgress.progress = ceil(vitories.toDouble() * 100 / numMatches.toDouble()).toInt()
            screen.defeatProgress.progress = 100 - screen.victoryProgress.progress
        }
        screen.victoryText.text = screen.victoryProgress.progress.toString() + "%"
        screen.defeatText.text = screen.defeatProgress.progress.toString() + "%"
        screen.mainContent.visibility = View.VISIBLE
        screen.progress.visibility  = View.GONE
    }
}
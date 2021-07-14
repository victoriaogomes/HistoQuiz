package com.example.histoquiz.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.databinding.ActivityMenuBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Classe utilizada para manipular os cliques recebidos na tela de menu do jogo, redirecionando o
 * usuário para as activities corretas relativas a cada interação
 */
class MenuActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var screen: ActivityMenuBinding

    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(screen.root)
        initGui()
        setInicialMenuOptions()
        hideSystemUI()
    }

    /**
     * Método chamado quando a janela atual da activity ganha ou perde o foco, é utilizado para es-
     * conder novamente a barra de status e a navigation bar.
     * @param hasFocus - booleano que indica se a janela desta atividade tem foco.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemUI()
    }

    /**
     * Método utilizado para fazer com que a barra de status e a navigation bar não sejam exibidas
     * na activity. Caso o usuário queira visualizá-las, ele deve realizar um movimento de arrastar
     * para cima (na navigation bar), ou para baixo (na status bar), o que fará com que elas apare-
     * çam por um momento e depois sumam novamente.
     */
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    /** Método utilizado para obter uma referência para os quatro botões presentes na view
     * relacionada a essa activity
     */
    fun initGui() {
        screen.signOutTXT.tag = "SIGN_OUT"
        screen.signOutTXT.setOnClickListener(this)
        screen.button1.setOnClickListener(this)
        screen.button2.setOnClickListener(this)
        screen.button3.setOnClickListener(this)
        screen.button4.setOnClickListener(this)
        screen.goBackBTN.setOnClickListener(this)
    }

    /**
     * Método utilizado para redirecionar os cliques recebidos nos botões da view relacionada
     * a essa activity para o método que lidará corretamente com ele
     * @param v - view onde o clique ocorreu
     */
    override fun onClick(v: View) {
        when (v.tag as String) {
            "MINHA_CONTA" -> myAccount()
            "JOGAR", "LOCAL_GAME" -> setPlayOptions()
            "REVISAR" -> setReviewOptions()
            "SOBRE_O_JOGO" -> setAboutOptions()
            "JOGAR_PC" -> playAgainstPC()
            "JOGAR_LOCAL" -> playLocalGame()
            "CONVIDAR_AMIGO" -> inviteFriend()
            "O_QUE_E" -> whatItIs()
            "COMO_JOGAR" -> howToPlay()
            "FALE_CONOSCO" -> contactUs()
            "SISTEMA_REPRODUTOR" -> showReview("Sistema Reprodutor")
            "SISTEMA_DIGESTORIO" -> showReview("Sistema Digestório")
            "SISTEMA_CARDIOPULMONAR" -> showReview("Sistema Cardiopulmonar")
            "SISTEMA_OSTEOMUSCULAR" -> showReview("Sistema Osteomuscular")
            "MENU_INICIAL" -> setInicialMenuOptions()
            "CRIAR_SALA" -> createGameRoom()
            "ENTRAR_SALA" -> joinGameRoom()
            "SIGN_OUT" -> {
                FirebaseAuth.getInstance().signOut()
                val troca = Intent(this@MenuActivity, SignInActivity::class.java)
                startActivity(troca)
            }
        }
    }

    /**
     * Método utilizado para trocar para a activity responsável por exibir os
     * detalhes da conta do usuário
     */
    private fun myAccount() {
        val troca = Intent(this@MenuActivity, MyAccountActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método que exibe ao jogador as opções de jogo disponíveis: jogar contra
     * o computador ou jogar online. Para isso, ele se aproveita dos botões do
     * menu: "jogar" vira "jogar online" e "revisar" vira "jogar contra o com-
     * putador". Os demais botões são desabilitados e ficam invisíveis
     */
    private fun setPlayOptions() {
        screen.button1.visibility = View.VISIBLE
        screen.button1.text = getString(R.string.local)
        screen.button1.tag = "JOGAR_LOCAL"
        screen.button1.isEnabled = true
        screen.button2.visibility = View.VISIBLE
        screen.button2.text
        screen.button2.text = getString(R.string.convidarAmigo)
        screen.button2.tag = "CONVIDAR_AMIGO"
        screen.button2.isEnabled = true
        screen.button3.visibility = View.VISIBLE
        screen.button3.text = getString(R.string.jogarPC)
        screen.button3.tag = "JOGAR_PC"
        screen.button3.isEnabled = true
        screen.button4.visibility = View.GONE
        screen.button4.isEnabled = false
        screen.goBackBTN.visibility = View.VISIBLE
        screen.goBackBTN.isEnabled = true
        screen.goBackBTN.tag = "MENU_INICIAL"
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que exibe
     */
    private fun setReviewOptions() {
        screen.button1.text = getString(R.string.sistemaReprodutor)
        screen.button1.tag = "SISTEMA_REPRODUTOR"
        screen.button2.text = getString(R.string.sistemaDigestorio)
        screen.button2.tag = "SISTEMA_DIGESTORIO"
        screen.button3.text = getString(R.string.sistemaCardiopulmonar)
        screen.button3.tag = "SISTEMA_CARDIOPULMONAR"
        screen.button4.text = getString(R.string.sistemaOsteomuscular)
        screen.button4.tag = "SISTEMA_OSTEOMUSCULAR"
        screen.goBackBTN.visibility = View.VISIBLE
        screen.goBackBTN.isEnabled = true
        screen.goBackBTN.tag = "MENU_INICIAL"
    }

    /**
     * Método que exibe ao jogador as opções sobre o jogo: o que é, como jogar e fale co-
     * nosco. Para isso, ele se aproveita dos botões do menu: "jogar", transformando-o em
     * "O que é"; "revisar", transformando-o em "como jogar"; e "sobre o jogo", transfor-
     * mando-o em "fale conosco"
     */
    private fun setAboutOptions() {
        screen.button1.visibility = View.GONE
        screen.button2.text = getString(R.string.oQue)
        screen.button2.tag = "O_QUE_E"
        screen.button3.text = getString(R.string.comoJogar)
        screen.button3.tag = "COMO_JOGAR"
        screen.button4.text = getString(R.string.faleConosco)
        screen.button4.tag = "FALE_CONOSCO"
        screen.goBackBTN.visibility = View.VISIBLE
        screen.goBackBTN.isEnabled = true
        screen.goBackBTN.tag = "MENU_INICIAL"
    }

    /**
     * Método utilizado para redirecionar o jogador diretamente para a tela de jogo,
     * já que ele irá jogar contra o próprio celular
     */
    private fun playAgainstPC() {
        val troca = Intent(this@MenuActivity, GameActivity::class.java)
        troca.putExtra("matchCreator", true)
        troca.putExtra("PCopponent", true)
        troca.putExtra("opponentUID", "0")
        startActivityForResult(troca, 999)
    }

    /**
     * Método utilizado para exibir ao jogador as opções existentes relativas a um jogo local: criar
     * uma sala ou entrar em uma já existente
     */
    private fun playLocalGame() {
        screen.button1.visibility = View.GONE
        screen.button1.isEnabled = false
        screen.button2.text = getString(R.string.criarSala)
        screen.button2.tag = "CRIAR_SALA"
        screen.button3.text = getString(R.string.entrarSala)
        screen.button3.tag = "ENTRAR_SALA"
        screen.button4.visibility = View.GONE
        screen.button4.isEnabled = false
        screen.goBackBTN.visibility = View.VISIBLE
        screen.goBackBTN.isEnabled = true
        screen.goBackBTN.tag = "LOCAL_GAME"
    }

    /**
     * Método utilizado para redirecionar o jogador para a tela de configurar as métricas
     * necessárias para a criação de uma partida online
     */
    private fun createGameRoom() {
        val troca = Intent(this@MenuActivity, ConfigLocalGameActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar o usuário a tela onde ele deve informar o código relati-
     * vo a sala do jogo no qual ele deseja entrar
     */
    private fun joinGameRoom() {
        val troca = Intent(this@MenuActivity, EnterLocalGameActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que exibe a tela
     * utilizada para convidar amigo para jogar
     */
    private fun inviteFriend() {
        val troca = Intent(this@MenuActivity, InviteFriendToPlayActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que descreve
     * o que o HistoQuiz é
     */
    private fun whatItIs() {
        val troca = Intent(this@MenuActivity, WhatItIsActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que descreve
     * como jogar o HistoQuiz
     */
    private fun howToPlay() {
        val troca = Intent(this@MenuActivity, RulesActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que permite
     * ao usuário enviar um email para os desenvolvedores do HistoQuiz
     */
    private fun contactUs() {
        val troca = Intent(this@MenuActivity, ContactActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para redirecionar dessa activity para a que exibe ao usuário
     * os dados para ele revisar sobre o sistema escolhido
     * @param kind - sistema do corpo humano que o usuário deseja revisar, a saber:
     * 1 - Sistema reprodutor
     * 2 - Sistema digestório
     * 3 - Sistema cardiopulmonar
     * 4 - Sistema osteomuscular
     */
    private fun showReview(kind: String) {
        val troca = Intent(this@MenuActivity, RevisionActivity::class.java)
        troca.putExtra("selectedSystem", kind)
        startActivity(troca)
    }

    /**
     * Método utilizado para setar tags para os botões presentes na view relacionada
     * a essa activity para a sua primeira execução, que exibe o menu principal
     */
    fun setInicialMenuOptions() {
        screen.button1.text = getString(R.string.minhaConta)
        screen.button1.tag = "MINHA_CONTA"
        screen.button1.visibility = View.VISIBLE
        screen.button1.isEnabled = true
        screen.button2.text = getString(R.string.jogar)
        screen.button2.tag = "JOGAR"
        screen.button2.visibility = View.VISIBLE
        screen.button2.isEnabled = true
        screen.button3.text = getString(R.string.revisar)
        screen.button3.tag = "REVISAR"
        screen.button3.visibility = View.VISIBLE
        screen.button3.isEnabled = true
        screen.button4.text = getString(R.string.sobreJogo)
        screen.button4.tag = "SOBRE_O_JOGO"
        screen.button4.visibility = View.VISIBLE
        screen.button4.isEnabled = true
        screen.goBackBTN.visibility = View.GONE
        screen.goBackBTN.isEnabled = false
    }

    /**
     * Sobrescreve o método que define o que deve ser feito quando o botão
     * pressionar do celular for pressionado. Como não quero que ele seja
     * utilizado, o método fica vazio
     */
    override fun onBackPressed() {
        // Não faz nada
    }
}
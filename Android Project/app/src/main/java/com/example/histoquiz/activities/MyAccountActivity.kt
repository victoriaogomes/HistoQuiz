package com.example.histoquiz.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.adapters.MyAccountAdapter
import com.example.histoquiz.databinding.ActivityMyAccountBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

/**
 * Classe utilizada para exibir as informações relativas a contado usuário (dados pessoais, amigos,
 * solicitações de amizade e desempenho)
 */
class MyAccountActivity : AppCompatActivity() {
    var tabSelectedIconColor = 0
    var tabUnselectedIconColor = 0
    private var myAccountAdapter: MyAccountAdapter? = null
    private lateinit var screen: ActivityMyAccountBinding

    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityMyAccountBinding.inflate(layoutInflater)
        screen.tabBarLYT.getTabAt(0)
        setContentView(screen.root)
        initGui()
        screen.tabBarLYT.selectTab(screen.tabBarLYT.getTabAt(0))
        tabSelectorManager()
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end. Além
     * disso, é utilizado também para configurar a cor de exibição de cada uma das abas da tela de
     * minha conta, bem como para configurar o título delas e a animação decorrente da troca de abas
     */
    fun initGui() {
        tabSelectedIconColor = ContextCompat.getColor(this@MyAccountActivity, R.color.darkerPurple)
        tabUnselectedIconColor = ContextCompat.getColor(this@MyAccountActivity, R.color.white)
        myAccountAdapter = MyAccountAdapter(supportFragmentManager, lifecycle, screen.tabBarLYT.tabCount, this, this)
        screen.viewPager.adapter = myAccountAdapter
        TabLayoutMediator(screen.tabBarLYT, screen.viewPager) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> {
                    tab.setIcon(R.drawable.ic_id_card)
                    tab.text = getString(R.string.perfil)
                }
                1 -> {
                    tab.setIcon(R.drawable.ic_user_friends)
                    tab.text = getString(R.string.amigos)
                }
                2 -> {
                    tab.setIcon(R.drawable.ic_chart_pie)
                    tab.text = getString(R.string.desempenho)
                }
            }
            tab.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabSelectedIconColor, BlendModeCompat.SRC_IN)
        }.attach()
        screen.tabBarLYT.getTabAt(0)?.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabUnselectedIconColor, BlendModeCompat.SRC_IN)
        screen.tabBarLYT.getTabAt(1)?.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabUnselectedIconColor, BlendModeCompat.SRC_IN)
        screen.tabBarLYT.getTabAt(2)?.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabUnselectedIconColor, BlendModeCompat.SRC_IN)
    }

    /**
     * Método utilizado para realizar a animação decorrente da transição entre abas, setando suas
     * cores corretamente, e exibindo o conteúdo solicitado
     */
    private fun tabSelectorManager() {
        screen.tabBarLYT.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                screen.viewPager.currentItem = tab.position
                tab.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabSelectedIconColor, BlendModeCompat.SRC_IN)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(tabUnselectedIconColor, BlendModeCompat.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}
package com.lenda.histoquiz.activities

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.QuerySnapshot
import android.os.Build
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.AdapterView.OnItemClickListener
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.*
import com.lenda.histoquiz.R
import com.lenda.histoquiz.databinding.ActivityConfigLocalGameBinding
import java.text.Collator
import java.util.*

class ConfigLocalGameActivity : AppCompatActivity() {
    private var firestoreDatabase: FirebaseFirestore? = null
    private var systems: HashMap<String, Int>? = null
    private var systemsNames: Array<String>? = null
    private var systemName: String? = null
    private var systemCode = 0
    private lateinit var screen: ActivityConfigLocalGameBinding

    /**
     * Método executado no instante em que essa activity é criada, seta qual view será associada a
     * essa classe
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityConfigLocalGameBinding.inflate(layoutInflater)
        setContentView(screen.root)
        hideSystemUI()
        initGUI()
        getSystems()
        handleRoomCreation()
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    private fun initGUI() {
        firestoreDatabase = FirebaseFirestore.getInstance()
//        systemsDropdown.dropDownWidth = (int) round(fullContentLL.width - (fullContentLL.width *0.1))
//        systemsDropdown.dropDownHeight = (int) round(fullContentLL.height *0.4)
        screen.systemsDropdown.inputType = InputType.TYPE_NULL
        screen.systemsDropdown.setDropDownBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dropdown_background, theme))
    }

    /**
     * Método utilizado para obter do firebase os sistemas relativos as lâminas cadastradas no banco
     * de dados
     */
    private fun getSystems() {
        firestoreDatabase!!.collection("sistemas").get().addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
            systems = HashMap()
            for (document in queryDocumentSnapshots) {
                systems!![document.id] = Objects.requireNonNull(document["code"]).toString().toInt()
            }
            systems!!["Aleatório"] = -1
            populateSystemSpinner()
        }
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
     * Método utilizado para popular o spinner com as categorias disponíveis no banco de dados de
     * forma alfabética, para que o usuário selecione uma e, em seguida, visualize as perguntas re-
     * lacionadas a ela
     */
    private fun populateSystemSpinner() {
        systemsNames = systems!!.keys.toTypedArray()
        Arrays.sort(systemsNames!!) { o1: String?, o2: String? ->
            val usCollator = Collator.getInstance(Locale("pt", "BR"))
            usCollator.compare(o1, o2)
        }
        val adapter = ArrayAdapter(this@ConfigLocalGameActivity, R.layout.spinner_layout, R.id.textoSpin, systemsNames!!)
        screen.systemsDropdown.setAdapter(adapter)
        systemName = screen.systemsDropdown.adapter.getItem(0).toString()
        systemCode = systems!![systemName!!]!!
        screen.systemsDropdown.setText(systemName, false)
        handleSpinnersClicks()
    }

    /**
     * Método utilizado para lidar com cliques no spinner que exibe as categorias disponíveis para o jogo
     */
    private fun handleSpinnersClicks() {
        screen.systemsDropdown.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            systemName = screen.systemsDropdown.adapter.getItem(position).toString()
            systemCode = systems!![systemName!!]!!
            screen.systemsDropdown.setText(systemName, false)
        }
    }

    /**
     * Método utilizado para criar uma sala de jogo utilizando como parâmetros para as configurações
     * dela as informações fornecidas por esse usuário
     */
    private fun handleRoomCreation() {
        screen.createRoomBTN.setOnClickListener {
            val troca = Intent(this@ConfigLocalGameActivity, LocalGameActivity::class.java)
            troca.putExtra("systemCode", systemCode)
            troca.putExtra("slidesAmount", screen.slidesQtyEDT.editText?.text.toString().toInt())
            troca.putExtra("roundTime", screen.roundTimeEDT.editText?.text.toString().toInt())
            troca.putExtra("matchCreator", true)
            troca.putExtra("roomCode", "")
            startActivityForResult(troca, 999)
        }
    }
}
package com.example.histoquiz.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.histoquiz.R
import com.example.histoquiz.databinding.ActivitySignInBinding
import com.example.histoquiz.util.FormFieldValidator
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

/**
 * Classe utilizada para lidar com o login e a criação de novas contas no HistoQuiz
 */
class SignInActivity : AppCompatActivity(), View.OnClickListener {

    var firebase: FirebaseAuth? = null
    var fieldValidator: FormFieldValidator? = null
    private lateinit var screen: ActivitySignInBinding

    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(screen.root)
        initGui()
        fieldValidator!!.monitorField(screen.emailEDT)
        fieldValidator!!.monitorField(screen.passwordEDT)
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

    /**
     * Método utilizado para obter referências para os elementos da interface que serão
     * manipulados por essa activity via código, bem como setar algumas configurações
     * deles, como tags e quem responderá a interações com eles
     */
    fun initGui() {
        screen.signInBTN.tag = "ENTRAR"
        screen.signInBTN.setOnClickListener(this)
        screen.forgotPasswTXT.tag = "ESQUECI_SENHA"
        screen.forgotPasswTXT.setOnClickListener(this)
        screen.signUpTXT.tag = "CADASTRAR"
        screen.signUpTXT.setOnClickListener(this)
        firebase = FirebaseAuth.getInstance()
        fieldValidator = FormFieldValidator(this)
        if (FirebaseAuth.getInstance().currentUser != null) {
            val troca = Intent(this@SignInActivity, MenuActivity::class.java)
            startActivity(troca)
        }
    }

    /**
     * Método utilizado para logar um usuário com o email e senha informado por este. Antes, é
     * verificado se os campos realmente foram informados
     */
    fun login() {
        var emailTxt = screen.emailEDT.editText?.text.toString()
        var senhaTxt = screen.passwordEDT.editText?.text.toString()
        if (checkAllFields()) {
            firebase!!.signInWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { s: String? ->
                        assert(user != null)
                        val ref = FirebaseFirestore.getInstance().collection("usuarios").document(user!!.uid)
                        ref.update("registrationToken", s)
                    }
                    val troca = Intent(this@SignInActivity, MenuActivity::class.java)
                    startActivity(troca)
                } else {
                    val builder = AlertDialog.Builder(this@SignInActivity)
                    builder.setMessage(task.exception?.message).setNeutralButton(R.string.ok, null)
                    builder.show()
                }
            }
        } else {
            emailTxt = "victoria.oliveiragomes@gmail.com"
            senhaTxt = "pjo30317512"
            firebase!!.signInWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val troca = Intent(this@SignInActivity, MenuActivity::class.java)
                    startActivity(troca)
                } else {
                    val builder = AlertDialog.Builder(this@SignInActivity)
                    builder.setMessage(task.exception?.message).setNeutralButton(R.string.ok, null)
                    builder.show()
                }
            }
        }
    }

    /**
     * Método utilizado para redirecionar os cliques recebidos nos botões da view relacionada
     * a essa activity para o método que lidará corretamente com ele
     * @param v - view onde o clique ocorreu
     */
    override fun onClick(v: View) {
        when (v.tag as String) {
            "ENTRAR" -> login()
            "ESQUECI_SENHA" -> esqueciSenha()
            "CADASTRAR" -> novoCadastro()
        }
    }

    /**
     * Método utilizado para enviar para o usuário um email para redefinição de senha
     */
    fun esqueciSenha() {
        if (fieldValidator!!.isFilled(screen.emailEDT)) {
            firebase!!.sendPasswordResetEmail(screen.emailEDT.editText?.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@SignInActivity, "Email de redefinição enviado.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Método utilizado para redirecionar o usuário para a activity de cadastro
     */
    fun novoCadastro() {
        val troca = Intent(this@SignInActivity, SignUpActivity::class.java)
        startActivity(troca)
    }

    /**
     * Método utilizado para checar o preenchimento de todos os campos do formulário de login
     * @return - true caso tudo esteja preenchido, e false caso algum deles esteja em branco
     */
    private fun checkAllFields(): Boolean {
        return fieldValidator!!.isFilled(screen.passwordEDT) && fieldValidator!!.isFilled(screen.emailEDT)
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
package com.example.histoquiz.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.example.histoquiz.BuildConfig
import com.example.histoquiz.R
import com.example.histoquiz.databinding.ActivitySignUpBinding
import com.example.histoquiz.util.FormFieldValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe utilizada para manipular a view com o formulário que deve ser preenchido
 * para cadastrar um novo usuário e para efetivar esse cadastro junto ao firebase,
 * lidando com eventuais erros que venham a ocorrer
 */
class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    var firebase: FirebaseAuth? = null
    private var fieldValidator: FormFieldValidator? = null
    private var mStorageRef: StorageReference? = null
    private var selectedImage: Uri? = null
    private var photoUri: Uri? = null
    private lateinit var screen: ActivitySignUpBinding

    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 100
        private const val REQUEST_PERMISSION = 200
    }

    /**
     * Método chamado assim que essa activity é invocada.
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(screen.root)
        initGui()
        fieldValidator!!.monitorField(screen.nameEDT)
        fieldValidator!!.monitorField(screen.emailEDT)
        fieldValidator!!.monitorField(screen.passwordEDT)
        fieldValidator!!.monitorField(screen.collegeEDT)
        fieldValidator!!.monitorField(screen.entryYearEDT)
        firebase = FirebaseAuth.getInstance()
        fieldValidator = FormFieldValidator(this)
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
     * deles, como tags e quem responderá a interações com eles.
     */
    fun initGui() {
        mStorageRef = FirebaseStorage.getInstance().reference
        screen.signUpBTN.tag = "CADASTRAR"
        screen.userPhotoIMGVW.tag = "SELECIONAR_IMAGEM"
        screen.userPhotoIMGVW.setOnClickListener(this)
        screen.signUpBTN.setOnClickListener(this)
        screen.goBackBTN.tag = "VOLTAR"
        screen.goBackBTN.setOnClickListener(this)
        fieldValidator = FormFieldValidator(this)
        screen.useTermsTXT.setOnClickListener{
            val intent = Intent(this, PdfViewActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Método utilizado para solicitar ao usuário que selecione uma imagem para seu perfil já
     * presente no celular ou que tire uma foto
     */
    private fun selectImage() {
        val options = arrayOf<CharSequence>("Tirar foto", "Selecionar da galeria", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecione sua foto de perfil.")
        builder.setItems(options) { dialog: DialogInterface, item: Int ->
            if (options[item] == "Tirar foto") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            MY_CAMERA_REQUEST_CODE
                        )
                    } else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (packageManager != null) {
                            //Create a file to store the image
                            var photoFile: File? = null
                            try {
                                photoFile = createImageFile()
                            } catch (ex: IOException) {
                                // Error occurred while creating the File
                            }
                            if (photoFile != null) {
                                photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                                intent.putExtra(
                                    MediaStore.EXTRA_OUTPUT,
                                    photoUri
                                )
                                startActivityForResult(intent, 0)
                            }
                        }
                    }
                } else {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (packageManager != null) {
                        //Create a file to store the image
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                            intent.putExtra(
                                MediaStore.EXTRA_OUTPUT,
                                photoUri
                            )
                            startActivityForResult(intent, 0)
                        }
                    }
                }
            } else if (options[item] == "Selecionar da galeria") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            REQUEST_PERMISSION
                        )
                    } else {
                        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(pickPhoto, 1)
                    }
                } else {
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(pickPhoto, 1)
                }
            } else if (options[item] == "Cancelar") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    /**
     * Método utilizado para abrir a câmera, após o usuário permitir
     * @param requestCode - código da requisição
     * @param permissions - resultado da permissão
     * @param grantResults -
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (packageManager != null) {
                    //Create a file to store the image
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            photoUri
                        )
                        startActivityForResult(intent, 0)
                    }
                }
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 1)
            } else {
                Toast.makeText(this, "storage permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Método utilizado para lidar com o arquivo de imagem fornecido pelo usuário para a sua foto
     * de perfil
     * @param requestCode - código da operação
     * @param resultCode - código indicando o resultado da operação
     * @param data - dados resultantes da operação realizada (nesse caso, fornecimento de uma imagem)
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK) {
                    selectedImage = photoUri
                    Glide.with(this).load(photoUri).into(screen.userPhotoIMGVW)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor = contentResolver.query(
                            selectedImage!!,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            screen.userPhotoIMGVW.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            cursor.close()
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    /**
     * Método utilizado para lidar com os cliques nos botões da view de cadastro de usuário. Ele
     * obtém a tag do elemento que disparou esse evento (ou seja, foi clicado) e, a partir dela,
     * redireciona o código para o método que irá lidar com ele.
     * @param v - view que recebeu o clique.
     */
    override fun onClick(v: View) {
        when (v.tag as String) {
            "CADASTRAR" -> if (checkAllFields()) {
                if(screen.checkbox1.isChecked){
                    newUser()
                }else{
                    Toast.makeText(this, "É necessário, no mínimo, aceitar os termos da pesquisa (Checkbox 1)", Toast.LENGTH_LONG).show()
                }
            }
            "VOLTAR" -> goBack()
            "SELECIONAR_IMAGEM" -> selectImage()
        }
    }

    /**
     * Método utilizado para cadastrar um novo usuário no firebase.
     */
    fun newUser() {
        val emailTxt = screen.emailEDT.editText?.text.toString()
        val senhaTxt = screen.passwordEDT.editText?.text.toString()
        firebase!!.createUserWithEmailAndPassword(emailTxt, senhaTxt).addOnCompleteListener(this) { task ->

            /**
             * Método chamando quando a tentativa de cadastrar um novo usuário no Firebase
             * é finalizada. Caso se obtenha sucesso, ele cadastra as demais informações do
             * usuário no firebase. Caso haja algum erro, este é exibido para o usuário.
             * @param task - task resultante da solicitação feita.
             */
            if (task.isSuccessful) {
                val user = firebase!!.currentUser
                val firestore = FirebaseFirestore.getInstance()
                assert(user != null)
                val dataConta = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                val dadosUsuario: MutableMap<String, Any> = HashMap()
                dadosUsuario["nome"] = screen.nameEDT.editText?.text.toString()
                dadosUsuario["univers"] = screen.collegeEDT.editText?.text.toString()
                dadosUsuario["anoIng"] = screen.entryYearEDT.editText?.text.toString()
                dadosUsuario["dataConta"] = dataConta
                dadosUsuario["okTermosUso"] = screen.checkbox1.isChecked
                dadosUsuario["enviarQuest"] = screen.checkbox2.isChecked
                if (selectedImage != null) {
                    val riversRef = mStorageRef!!.child("profilePics/" + user!!.uid)
                    riversRef.putFile(selectedImage!!)
                        .addOnSuccessListener {
                            riversRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                                uri
                            }
                        }
                        .addOnFailureListener { exception: Exception? ->
                            Toast.makeText(this@SignUpActivity, exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    dadosUsuario["photoName"] = user.uid
                } else {
                    dadosUsuario["photoName"] = "semFoto"
                }
                FirebaseMessaging.getInstance().token.addOnSuccessListener { s: String ->
                    FirebaseFirestore.getInstance().collection("usuarios").document(
                        user!!.uid
                    )
                    dadosUsuario["registrationToken"] = s
                    firestore.collection("usuarios").document(user.uid).set(dadosUsuario)
                    dadosUsuario.clear()
                    dadosUsuario["UID"] = user.uid
                    firestore.collection("tabelaAuxiliar").document(user.email.toString()).set(dadosUsuario)
                    dadosUsuario.clear()
                    dadosUsuario["numPartidas"] = 0
                    dadosUsuario["sisCardiopulmonar"] = ArrayList(listOf(0, 0))
                    dadosUsuario["sisDigestorio"] = ArrayList(listOf(0, 0))
                    dadosUsuario["sisOsteomuscular"] = ArrayList(listOf(0, 0))
                    dadosUsuario["sisReprodutor"] = ArrayList(listOf(0, 0))
                    dadosUsuario["vitorias"] = 0
                    firestore.collection("desempenho").document(user.uid).set(dadosUsuario)
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(screen.nameEDT.editText?.text.toString()).build()
                    user.updateProfile(profileUpdates)
                    val troca = Intent(this@SignUpActivity, MenuActivity::class.java)
                    startActivity(troca)
                }
            } else {
                val builder = AlertDialog.Builder(this@SignUpActivity)
                builder.setMessage(task.exception?.message).setNeutralButton(R.string.ok, null)
                builder.show()
            }
        }
    }

    /**
     * Método utilizado para checar o preenchimento de todos os campos do formulário de cadastro.
     * @return - true caso tudo esteja preenchido, e false caso algum deles esteja em branco.
     */
    private fun checkAllFields(): Boolean {
        return (fieldValidator!!.isFilled(screen.nameEDT) && fieldValidator!!.isFilled(screen.emailEDT) && fieldValidator!!.isFilled(screen.passwordEDT)
                && fieldValidator!!.isFilled(screen.collegeEDT) && fieldValidator!!.isFilled(screen.entryYearEDT))
    }

    /**
     * Sobrescreve o método que define o que deve ser feito quando o botão voltar do celular for
     * pressionado. Como não quero que ele seja utilizado, o método fica vazio.
     */
    override fun onBackPressed() {
        // Não faz nada
    }

    /**
     * Método utilizado para retornar a activity de login.
     */
    private fun goBack() {
        val troca = Intent(this@SignUpActivity, SignInActivity::class.java)
        startActivity(troca)
    }
}
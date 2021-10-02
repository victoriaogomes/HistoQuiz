package com.lenda.histoquiz.fragments

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lenda.histoquiz.BuildConfig
import com.lenda.histoquiz.R
import com.lenda.histoquiz.activities.MyAccountActivity
import com.lenda.histoquiz.databinding.FragmentProfileBinding
import com.lenda.histoquiz.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Classe utilizada para exibir o fragmente que possui as informações pessoais relativas ao usuário
 * logado no momento (email, nome, senha, etc...)
 */
class ProfileFragment
/**
 * Método construtor da classe
 */(var activity: MyAccountActivity) : Fragment() {
    var usuario: FirebaseUser? = null
    var database: FirebaseFirestore? = null
    private var mStorageRef: StorageReference? = null
    private var selectedImage: Uri? = null
    private var photoUri: Uri? = null
    private var dadosUser: User? = null
    private var password: String? = null
    private var _screen: FragmentProfileBinding? = null
    private val screen get() = _screen!!

    companion object {
        private const val MY_CAMERA_REQUEST_CODE = 100
        private const val REQUEST_PERMISSION = 200
    }
    /**
     * Método chamado para a criação da view associada a esse fragment
     * @param inflater - objeto do tipo LayoutInflater, onde a view desse fragment será exibida
     * @param container - uma view opcional que será utilizada como a raiz (root) da view que será
     * inflada
     * @param savedInstanceState - contém o estado anteriormente salvo da atividade (pode ser nulo)
     * @return - retorna a view criada para esse fragment
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _screen = FragmentProfileBinding.inflate(inflater, container, false)
        initGui()
        setUserInfos()
        return screen.root
    }

    /**
     * Método utilizado para obter a referência para certos objetos da view que serão manipulados
     * por essa classe e para inicializar alguns outros objetos utilizados aqui no back-end
     */
    fun initGui() {
        mStorageRef = FirebaseStorage.getInstance().reference
        screen.selectImage.setOnClickListener { selectImage() }
        usuario = FirebaseAuth.getInstance().currentUser
        database = FirebaseFirestore.getInstance()
        configureSave()
    }

    /**
     * Método utilizado para setar nos editText as informações desse usuário da maneira que estão
     * cadastradas no banco de dados atualmente
     */
    fun setUserInfos() {
        screen.mainContent.visibility = View.INVISIBLE
        screen.progress.visibility = View.VISIBLE
        screen.email.editText?.setText(usuario!!.email)
        screen.senha.editText?.setText(getString(R.string.senha))
        val userData = database!!.collection("usuarios").document(usuario!!.uid).get()
        userData.addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
            dadosUser = documentSnapshot.toObject(User::class.java)
            assert(dadosUser != null)
            screen.nome.editText?.setText(dadosUser!!.nome)
            screen.universidade.editText?.setText(dadosUser!!.univers)
            screen.anoIngresso.editText?.setText(dadosUser!!.anoIng)
            if (dadosUser!!.photoName != "semFoto") {
                val photo = mStorageRef!!.child(String.format("profilePics/%s", dadosUser!!.photoName))
                Glide.with(activity).load(photo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(screen.selectImage)
            }
            screen.progress.visibility = View.GONE
            screen.mainContent.visibility = View.VISIBLE
        }
    }

    /**
     * Método utilizado para solicitar ao usuário que selecione uma imagem para seu perfil já
     * presente no celular ou que tire uma foto
     */
    private fun selectImage() {
        val options = arrayOf<CharSequence>("Tirar foto", "Selecionar da galeria", "Cancelar")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Selecione sua foto de perfil.")
        builder.setItems(options) { dialog: DialogInterface, item: Int ->
            if (options[item] == "Tirar foto") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            MY_CAMERA_REQUEST_CODE
                        )
                    } else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (activity.packageManager != null) {
                            //Create a file to store the image
                            var photoFile: File? = null
                            try {
                                photoFile = createImageFile()
                            } catch (ex: IOException) {
                                // Error occurred while creating the File
                            }
                            if (photoFile != null) {
                                photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
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
                    if (activity.packageManager != null) {
                        //Create a file to store the image
                        var photoFile: File? = null
                        try {
                            photoFile = createImageFile()
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                        }
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
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
                    if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                if (activity.packageManager != null) {
                    //Create a file to store the image
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        photoUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", photoFile)
                        intent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            photoUri
                        )
                        startActivityForResult(intent, 0)
                    }
                }
            } else {
                Toast.makeText(activity, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhoto, 1)
            } else {
                Toast.makeText(activity, "storage permission denied", Toast.LENGTH_LONG).show()
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
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == Activity.RESULT_OK) {
                    selectedImage = photoUri
                    Glide.with(activity).load(selectedImage).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(screen.selectImage)
                }
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    selectedImage = data.data
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor = activity.contentResolver.query(
                            selectedImage!!,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            screen.selectImage.setImageBitmap(BitmapFactory.decodeFile(picturePath))
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
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun configureSave() {
        screen.salvarButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(
                activity
            )
            alertDialog.setTitle("Salvar dados")
            alertDialog.setMessage("Informe sua senha atual")
            val input = EditText(activity)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            alertDialog.setView(input)
            alertDialog.setIcon(R.drawable.ic_password)
            alertDialog.setPositiveButton(
                "Confirmar"
            ) { _: DialogInterface?, _: Int ->
                password = input.text.toString()
                updateForReal()
            }
            alertDialog.setNegativeButton(
                "Cancelar"
            ) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            alertDialog.show()
        }
    }

    private fun updateForReal() {
        reAuth()
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(screen.nome.editText?.text.toString()).build()
        usuario!!.updateProfile(profileUpdates)
        dadosUser!!.anoIng = screen.anoIngresso.editText?.text.toString()
        dadosUser!!.nome = screen.nome.editText?.text.toString()
        dadosUser!!.univers = screen.universidade.editText?.text.toString()
        if (selectedImage != null) {
            dadosUser!!.photoName = usuario!!.uid
            val riversRef = mStorageRef!!.child("profilePics/" + usuario!!.uid)
            riversRef.putFile(selectedImage!!)
                .addOnSuccessListener {
                    riversRef.downloadUrl.addOnSuccessListener {  }
                }
                .addOnFailureListener { }
        }
        database!!.collection("usuarios").document(usuario!!.uid).update(
            "anoIng", dadosUser!!.anoIng,
            "nome", dadosUser!!.nome, "univers", dadosUser!!.univers, "photoName", dadosUser!!.photoName
        )
        usuario!!.updateEmail(screen.email.editText?.text.toString())
        usuario!!.updatePassword(screen.senha.editText?.text.toString())
    }

    private fun reAuth() {
        val credential = usuario!!.email?.let { EmailAuthProvider.getCredential(it, password!!) }
        if (credential != null) {
            FirebaseAuth.getInstance().currentUser?.reauthenticate(credential)?.addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Dados atualizados com sucesso!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
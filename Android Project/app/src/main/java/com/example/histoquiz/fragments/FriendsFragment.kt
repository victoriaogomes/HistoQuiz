package com.example.histoquiz.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.histoquiz.R
import com.example.histoquiz.adapters.FriendsAdapter
import com.example.histoquiz.adapters.FriendshipRequestAdapter
import com.example.histoquiz.databinding.FragmentFriendsBinding
import com.example.histoquiz.model.Friend
import com.example.histoquiz.model.FriendRequest
import com.example.histoquiz.util.FormFieldValidator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

/**
 * Classe utilizada para manipular a view "Amigos" que é exibida em "Minha conta", mostrando os
 * amigos desse usuário, as solicitações de amizade dele e dando a opção para ele enviar convite
 * para adicionar outro usuário.
 */
class FriendsFragment
/**
 * Construtor da classe desse fragmento.
 */
    : Fragment() {
    private var friendsSource: ArrayList<Friend>? = null
    var friendRequestSource: ArrayList<FriendRequest>? = null
    private var aux1: MutableList<String>? = null
    private var aux2: MutableList<String>? = null
    private var recyclerViewLM: RecyclerView.LayoutManager? = null
    var adapter: FriendsAdapter? = null
    private var adapter2: FriendshipRequestAdapter? = null
    private var horizontalLayout1: LinearLayoutManager? = null
    private var horizontalLayout2: LinearLayoutManager? = null
    private var validateForm: FormFieldValidator? = null
    private var user: FirebaseUser? = null
    var firestore: FirebaseFirestore? = null
    private var params: LinearLayout.LayoutParams? = null
    private var _screen: FragmentFriendsBinding? = null
    private val screen get() = _screen!!
    var friendsList: RecyclerView? = null
    var friendshipRequestList: RecyclerView? = null

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
        _screen = FragmentFriendsBinding.inflate(inflater, container, false)
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
        validateForm = context?.let { FormFieldValidator(it) }
        validateForm!!.monitorField(screen.adicionarAmigos)
        recyclerViewLM = LinearLayoutManager(context)
        horizontalLayout1 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        horizontalLayout2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        user = FirebaseAuth.getInstance().currentUser
        configureSendFriendRequest()
        params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params!!.setMargins(10, 0, 10, 0)
        friends
        friendRequests
    }

    /**
     * Método utilizado para configurar como resposta ao botão de convidar amigo uma função que
     * salva no firebase o convite enviado para um usuário, para adicioná-lo a sua lista de amigos.
     */
    fun configureSendFriendRequest() {
        screen.sendFriendRequest.setOnClickListener {
            if (validateForm!!.isFilled(screen.adicionarAmigos)) {
                val friendUID = firestore!!.collection("tabelaAuxiliar").document(screen.adicionarAmigos.editText?.text.toString()).get()
                friendUID.addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    firestore!!.document("amizades/solicitacoes").collection(
                        documentSnapshot["UID"].toString()
                    ).document(user!!.uid).set(HashMap<String, Any>())
                }
            }
        }
    }

    /**
     * Método utilizado para obter do firebase os amigos desse usuário e, após obtê-los, setar o
     * adapter que será utilizado para exibi-los. Caso não hajam amigos para esse usuário, exibe
     * uma mensagem informando isso.
     */
    val friends: Unit
        get() {
            aux1 = ArrayList()
            friendsSource = ArrayList()
            val friends = firestore!!.document("amizades/amigos").collection(user!!.uid).get()
            friends.addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (!queryDocumentSnapshots.isEmpty) {
                    friendsList = context?.let { RecyclerView(it) }
                    friendsList?.layoutParams = params
                    friendsList?.layoutManager = recyclerViewLM
                    friendsList?.layoutManager = horizontalLayout1
                    screen.friendsPlace.removeViewAt(2)
                    screen.friendsPlace.addView(friendsList)
                    val usuarios = firestore!!.collection("usuarios")
                    for (documentSnapshot in queryDocumentSnapshots) {
                        (aux1 as ArrayList<String>).add(documentSnapshot.id)
                    }
                    usuarios.whereIn(FieldPath.documentId(), aux1 as ArrayList<String>).get().addOnSuccessListener { queryDocumentSnapshots1: QuerySnapshot ->
                        for (documentSnapshot in queryDocumentSnapshots1) {
                            var name = Objects.requireNonNull(documentSnapshot["nome"]).toString()
                            val separatedName = name.split(" ").toTypedArray()
                            name = if (separatedName.size > 1) {
                                separatedName[0] + " " + separatedName[1]
                            } else {
                                separatedName[0]
                            }
                            friendsSource!!.add(Friend(name, documentSnapshot.id))
                        }
                        adapter = FriendsAdapter(friendsSource!!, this)
                        friendsList!!.adapter = adapter
                    }
                } else {
                    val message = TextView(context)
                    message.text = activity?.resources?.getString(R.string.semAmg)
                    message.textSize = 20f
                    message.setTextColor(requireActivity().resources.getColor(R.color.white))
                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(20, 10, 10, 10)
                    message.layoutParams = params
                    message.gravity = Gravity.CENTER
                    message.setPadding(10, 20, 10, 20)
                    val tp = context?.let { ResourcesCompat.getFont(it, R.font.pinkchicken) }
                    message.typeface = tp
                    screen.friendsPlace.removeView(friendsList)
                    screen.friendsPlace.addView(message)
                }
            }
        }

    /**
     * Método utilizado para obter do firebase as solicitações de amizade cadastradas para esse
     * usuário e, após obtê-las, setar o adapter que será utilizado para exibi-las. Caso não hajam
     * solicitações, exibe um texto informando isso ao usuário
     */
    val friendRequests: Unit
        get() {
            aux2 = ArrayList()
            friendRequestSource = ArrayList()
            val friends = firestore!!.document("amizades/solicitacoes").collection(user!!.uid).get()
            friends.addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                if (!queryDocumentSnapshots.isEmpty) {
                    friendshipRequestList = context?.let { RecyclerView(it) }
                    friendshipRequestList!!.layoutParams = params
                    friendshipRequestList!!.layoutManager = recyclerViewLM
                    friendshipRequestList!!.layoutManager = horizontalLayout2
                    screen.friendsRequestsPlace.removeViewAt(2)
                    screen.friendsRequestsPlace.addView(friendshipRequestList)
                    val usuarios = firestore!!.collection("usuarios")
                    for (documentSnapshot in queryDocumentSnapshots) {
                        (aux2 as ArrayList<String>).add(documentSnapshot.id)
                    }
                    usuarios.whereIn(FieldPath.documentId(), aux2 as ArrayList<String>).get().addOnSuccessListener { queryDocumentSnapshots1: QuerySnapshot ->
                        for (documentSnapshot in queryDocumentSnapshots1) {
                            var name = Objects.requireNonNull(documentSnapshot["nome"]).toString()
                            val separatedName = name.split(" ").toTypedArray()
                            name = if (separatedName.size > 1) {
                                separatedName[0] + " " + separatedName[separatedName.size - 1]
                            } else {
                                separatedName[0]
                            }
                            friendRequestSource!!.add(FriendRequest(name, documentSnapshot.id))
                        }
                        adapter2 = FriendshipRequestAdapter(friendRequestSource!!, this)
                        friendshipRequestList!!.adapter = adapter2
                    }
                } else {
                    val message = TextView(context)
                    message.text = activity?.resources?.getString(R.string.semSolic)
                    message.textSize = 20f
                    message.setTextColor(requireActivity().resources.getColor(R.color.white))
                    val params2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params2.setMargins(20, 10, 10, 10)
                    message.layoutParams = params2
                    message.gravity = Gravity.CENTER
                    message.setPadding(10, 20, 10, 20)
                    val tp = context?.let { ResourcesCompat.getFont(it, R.font.pinkchicken) }
                    message.typeface = tp
                    screen.friendsRequestsPlace.removeView(friendshipRequestList)
                    screen.friendsRequestsPlace.addView(message)
                }
            }
        }
}
package com.example.histoquiz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.histoquiz.R
import com.example.histoquiz.adapters.FriendshipRequestAdapter.FriendshipRequestHolder
import com.example.histoquiz.fragments.FriendsFragment
import com.example.histoquiz.model.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Classe utilizada para auxiliar na exibição da lista de solicitações de amizade que o usuário
 * logado no momento possui
 */
class FriendshipRequestAdapter
/**
 * Método construtor da classe, recebe a lista de solicitações de amizade desse usuário que deve
 * ser exibida
 * @param list - lista de solicitações de amizade do usuário logado no momento
 * @param manager - fragmento que onde essa lista será exibida
 */(private val list: MutableList<FriendRequest>, private val manager: FriendsFragment) : RecyclerView.Adapter<FriendshipRequestHolder>(),
    CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    /**
     * Método chamado quando o RecyclerView precisa de um novo ViewHolder para listar mais um
     * item (nesse caso, mais uma solicitação de amizade)
     * @param parent - ViewGroup no qual a nova View será adicionada após ser vinculada a uma
     * posição do adapter.
     * @param viewType - O tipo da nova view
     * @return - um novo ViewHolder do tipo FriendshipRequestHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendshipRequestHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend, parent, false)
        return FriendshipRequestHolder(itemView)
    }

    /**
     * Método chamado pelo RecyclerView para exibir os dados na posição especificada. Esse método
     * deve atualizar o conteúdo do amigo exibido para refletir a solicitação de amizade presente
     * na posição especificada da lista.
     * @param holder - o usuário que deve ser atualizado para representar o conteúdo
     * @param position - A posição do item no conjunto de dados do adapter
     */
    override fun onBindViewHolder(holder: FriendshipRequestHolder, position: Int) {
        holder.textView.text = list[position].name
    }

    /**
     * Método que retorna o número de solicitações de amizade que o usuário logado atualmente possui
     * @return - número de solicitações de amizade
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Classe interna utilizada para exibir cada uma das solicitações de amizade desse usuário, e
     * lidar com os cliques no botão de aceitar ou recusar solicitação de amizade
     */
    inner class FriendshipRequestHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.textview)
        private var removeRequest: ImageButton = itemView.findViewById(R.id.recusarAmigo)
        private var acceptRequest: ImageButton = itemView.findViewById(R.id.aceitarAmigo)

        /**
         * Método utilizado para aceitar a solicitação de amizade de um usuário
         */
        private fun configureAcceptFriendship() {
            acceptRequest.setOnClickListener {
                for (request in list) {
                    if (request.name.contentEquals(textView.text)) {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.document("amizades/solicitacoes/" + (FirebaseAuth.getInstance().currentUser?.uid) + "/" + request.uid)
                            .delete()
                        FirebaseAuth.getInstance().currentUser?.let { it1 ->
                            firestore.document("amizades/amigos").collection(it1.uid)
                                .document(request.uid).set(
                                    HashMap<String, Any>()
                                )
                        }
                        FirebaseAuth.getInstance().currentUser?.let { it1 ->
                            firestore.document("amizades/amigos").collection(request.uid).document(it1.uid).set(HashMap<String, Any>())
                        }
                        list.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                        notifyItemRangeChanged(bindingAdapterPosition, list.size)
                        launch {
                            manager.getFriends()
                            manager.getFriendsRequest()
                        }
                        break
                    }
                }
            }
        }

        /**
         * Método utilizado para rejeitar uma solicitação de amizade feita por um usuário
         */
        private fun configureRejectFriendship() {
            removeRequest.setOnClickListener {
                for (request in list) {
                    if (request.name.contentEquals(textView.text)) {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.document("amizades/solicitacoes/" + (FirebaseAuth.getInstance().currentUser?.uid) + "/" + request.uid)
                            .delete()
                        list.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                        notifyItemRangeChanged(bindingAdapterPosition, list.size)
                        launch {
                            manager.getFriendsRequest()
                        }
                        break
                    }
                }
            }
        }

        init {
            configureAcceptFriendship()
            configureRejectFriendship()
        }
    }
}
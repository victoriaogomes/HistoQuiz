package com.example.histoquiz.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.histoquiz.R
import com.example.histoquiz.adapters.FriendsAdapter.FriendsHolder
import com.example.histoquiz.fragments.FriendsFragment
import com.example.histoquiz.model.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Classe utilizada para auxiliar na exibição da lista de amigos que o usuário logado no momento
 * possui
 */
class FriendsAdapter
/**
 * Método construtor da classe, recebe a lista de amigos desse usuário que deve ser exibida
 * @param list - lista de amigos do usuário logado no momento
 * @param manager - fragmento que onde essa lista será exibida
 */(private val list: MutableList<Friend>, private val manager: FriendsFragment) : RecyclerView.Adapter<FriendsHolder>(), CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    /**
     * Método chamado quando o RecyclerView precisa de um novo ViewHolder para listar mais um
     * item (nesse caso, mais um usuário)
     * @param parent - ViewGroup no qual a nova View será adicionada após ser vinculada a uma
     * posição do adapter.
     * @param viewType - O tipo da nova view
     * @return - um novo ViewHolder do tipo FriendsHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friend, parent, false)
        return FriendsHolder(itemView)
    }

    /**
     * Método chamado pelo RecyclerView para exibir os dados na posição especificada. Esse método
     * deve atualizar o conteúdo do amigo exibido para refletir o usuário na posição especificada da
     * lista.
     * @param holder - o usuário que deve ser atualizado para representar o conteúdo
     * @param position - A posição do item no conjunto de dados do adapter
     */
    override fun onBindViewHolder(holder: FriendsHolder, position: Int) {
        holder.textView.text = list[position].name
    }

    /**
     * Método que retorna o número de amigos que o usuário logado atualmente possui
     * @return - número de amigos
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Classe interna utilizada para exibir cada um dos amigos desse usuário, e lidar com os cliques
     * no botão de remover amigo
     */
    inner class FriendsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.textview)
        var accept: ImageButton = itemView.findViewById(R.id.aceitarAmigo)
        private var removeFriend: ImageButton

        /**
         * Método utilizado para remover um usuário da sua lista de amigos
         */
        private suspend fun configureRemoveFriend() {
            removeFriend.setOnClickListener {
                for (friend in list) {
                    if (friend.name.contentEquals(textView.text)) {
                        val firestore = FirebaseFirestore.getInstance()
                        firestore.document("amizades/amigos/" + FirebaseAuth.getInstance().currentUser?.uid + "/" + friend.uid)
                            .delete()
                        list.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                        notifyItemRangeChanged(bindingAdapterPosition, list.size)
                        launch {
                            manager.getFriends()
                        }
                        break
                    }
                }
            }
        }

        init {
            accept.visibility = View.GONE
            removeFriend = itemView.findViewById(R.id.recusarAmigo)
            launch {
                configureRemoveFriend()
            }
        }
    }
}
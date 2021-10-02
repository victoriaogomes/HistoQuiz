package com.lenda.histoquiz.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lenda.histoquiz.R
import com.lenda.histoquiz.activities.GameActivity
import com.lenda.histoquiz.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Classe utilizada para manipular o recebimento de notificações pelo FirebaseMessagingService
 */
class FirebaseService : FirebaseMessagingService() {
    /**
     * Método utilizado para tratar o recebimento de notificações quando o aplicativo
     * está sendo executado (em 1º plano)
     * @param remoteMessage - mensagem que foi recebida
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //super.onMessageReceived(remoteMessage);
        if (remoteMessage.notification != null) {
            Log.d("Histoquiz", remoteMessage.notification!!.title!!)
            Log.d("Histoquiz", remoteMessage.notification!!.body!!)
            remoteMessage.notification!!.title?.let {
                sendNotification(
                    it, remoteMessage.notification!!.body,
                    remoteMessage.data["opponentUID"]
                )
            }
        }
    }

    /**
     * Método responsável por gerar uma notificação na barra de notificações do android
     * @param title - título da notificação a ser gerada
     * @param msg - mensagem a ser exibida na notificação que será gerada
     */
    private fun sendNotification(title: String, msg: String?, opponentUID: String?) {
        val intent: Intent
        if (title == "Novo convite de jogo!") {
            intent = Intent(this, GameActivity::class.java) //GameActivity é a classe que será aberta ao usuário clicar na notificação
            intent.putExtra("matchCreator", false)
            intent.putExtra("opponentUID", opponentUID)
            intent.putExtra("PCopponent", false)
        } else {
            intent = Intent(this, SignInActivity::class.java) //SignInActivity é a classe que será aberta ao usuário clicar na notificação
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_microscope)
            .setContentTitle(title)
            .setContentText(msg)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(getString(R.string.default_notification_channel_id), "channel", NotificationManager.IMPORTANCE_DEFAULT)
            channel.vibrationPattern = longArrayOf(1000, 1000)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notification.build())
    }

    /**
     * Método chamado sempre que um novo token é criado para essa instância do aplicativo que se
     * encontra em execução. Caso haja um usuário logado, atualiza o token dele no firebase
     * @param s - novo token gerado
     */
    override fun onNewToken(s: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val ref = FirebaseFirestore.getInstance().collection("usuarios").document(user.uid)
            ref.update("registrationToken", s)
        }
        super.onNewToken(s)
        Log.d("Histoquiz", s)
    }
}
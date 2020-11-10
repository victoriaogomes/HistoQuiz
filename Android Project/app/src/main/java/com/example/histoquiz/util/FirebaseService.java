package com.example.histoquiz.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.histoquiz.R;
import com.example.histoquiz.activities.GameActivity;
import com.example.histoquiz.activities.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Objects;

/**
 * Classe utilizada para manipular o recebimento de notificações pelo FirebaseMessagingService
 */
public class FirebaseService extends FirebaseMessagingService {


    /**
     * Método utilizado para tratar o recebimento de notificações quando o aplicativo
     * está sendo executado (em 1º plano)
     * @param remoteMessage - mensagem que foi recebida
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        if(remoteMessage.getNotification() != null){
            Log.d("Histoquiz", remoteMessage.getNotification().getTitle());
            Log.d("Histoquiz", remoteMessage.getNotification().getBody());
            sendNotification(Objects.requireNonNull(remoteMessage.getNotification().getTitle()), remoteMessage.getNotification().getBody(),
                    remoteMessage.getData().get("opponentUID"));
        }
    }


    /**
     * Método responsável por gerar uma notificação na barra de notificações do android
     * @param title - título da notificação a ser gerada
     * @param msg - mensagem a ser exibida na notificação que será gerada
     */
    public void sendNotification(String title, String msg, String opponentUID){
        Intent intent;
        if(title.equals("Novo convite de jogo!")){
            intent = new Intent(this, GameActivity.class); //GameActivity é a classe que será aberta ao usuário clicar na notificação
            intent.putExtra("matchCreator", false);
            intent.putExtra("opponentUID", opponentUID);
            intent.putExtra("PCopponent", false);
        }
        else{
            intent = new Intent(this, SignInActivity.class); //SignInActivity é a classe que será aberta ao usuário clicar na notificação
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_microscope)
                .setContentTitle(title)
                .setContentText(msg)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setVibrate(new long[] {1000, 1000})
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(getString(R.string.default_notification_channel_id), "channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setVibrationPattern(new long[] {1000, 1000});
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notification.build());
    }


    /**
     * Método chamado sempre que um novo token é criado para essa instância do aplicativo que se
     * encontra em execução. Caso haja um usuário logado, atualiza o token dele no firebase
     * @param s - novo token gerado
     */
    @Override
    public void onNewToken(@NonNull String s) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user !=null) {
            DocumentReference ref = FirebaseFirestore.getInstance().collection("usuarios").document(user.getUid());
            ref.update("registrationToken", s);
        }
        super.onNewToken(s);
        Log.d("Histoquiz", s);
    }
}

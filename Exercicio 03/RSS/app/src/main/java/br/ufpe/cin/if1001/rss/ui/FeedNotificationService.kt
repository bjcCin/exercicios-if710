package br.ufpe.cin.if1001.rss.ui

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import br.ufpe.cin.if1001.rss.R

class FeedNotificationService: BroadcastReceiver() {

    //Broadcast que será disparado quando o service adicionar um novo item ao DB
    override fun onReceive(context: Context, intent: Intent) {
        val intent1 = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context.applicationContext, 0, intent1, 0)
        //Criando a notificação
        val mBuilder = NotificationCompat.Builder(context.applicationContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("RSS Feed")
                .setContentText("Chegou Notícias Novas!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        //Executando a mesma
        val notification = mBuilder.build()
        NotificationManagerCompat.from(context.applicationContext).notify(0, notification)
    }
}
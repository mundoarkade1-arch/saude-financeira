package br.com.saudefinanceira.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.saudefinanceira.MainActivity
import br.com.saudefinanceira.R

object Notifier {
    private const val CHANNEL_ID = "vencimentos"

    fun hasPermission(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID, "Vencimentos de contas", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Avisos de contas perto do vencimento"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    /** Retorna false se não há permissão para notificar. */
    @SuppressLint("MissingPermission")
    fun show(context: Context, id: Int, title: String, lines: List<String>): Boolean {
        if (!hasPermission(context) || lines.isEmpty()) return false
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(lines.first())
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (lines.size > 1) {
            val style = NotificationCompat.InboxStyle()
            lines.forEach { style.addLine(it) }
            builder.setStyle(style)
        } else {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(lines.first()))
        }
        NotificationManagerCompat.from(context).notify(id, builder.build())
        return true
    }

    fun showTest(context: Context): Boolean =
        show(context, 1001, "Lembrete de teste", listOf("Se você está vendo isto, os lembretes estão funcionando."))
}

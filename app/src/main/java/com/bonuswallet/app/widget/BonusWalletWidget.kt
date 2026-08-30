
package com.bonuswallet.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.bonuswallet.app.MainActivity
import com.bonuswallet.app.R
import com.bonuswallet.app.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BonusWalletWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val fav = db.cardDao().getAll().filter { it.isFavorite }.take(3)
                val views = RemoteViews(context.packageName, R.layout.widget_bonus)
                // Simple: show count
                val intent = Intent(context, MainActivity::class.java)
                val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                views.setOnClickPendingIntent(R.id.widget_root, pending)
                views.setTextViewText(R.id.widget_text, if(fav.isEmpty()) "BonusWallet" else fav.first().getDisplayOrgName())
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}


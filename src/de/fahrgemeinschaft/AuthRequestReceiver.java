package de.fahrgemeinschaft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import de.fahrgemeinschaft.util.Util;

public class AuthRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Notification notify = new NotificationCompat.Builder(ctx)
                .setContentIntent(PendingIntent.getActivity(
                        ctx, 42, Util.profileIntent(ctx), 0))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(ctx.getString(R.string.login_required))
                .setTicker(ctx.getString(R.string.login_required))
                .setContentText(ctx.getString(R.string.why_login))
                .build();
        NotificationManager n = (NotificationManager) ctx.getSystemService(
                Context.NOTIFICATION_SERVICE);
        n.cancel(42);
        n.notify(42, notify);
    }

}

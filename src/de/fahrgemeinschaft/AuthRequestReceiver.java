package de.fahrgemeinschaft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

public class AuthRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Notification notify = new NotificationCompat.Builder(ctx)
                .setContentIntent(PendingIntent.getActivity(ctx, 42,
                        new Intent(ctx, MainActivity.class).setData(Uri.parse(
                                "content://de.fahrgemeinschaft/profile")), 0))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(ctx.getString(R.string.login_required))
                .setContentText(ctx.getString(R.string.need_to_login))
                .setTicker(ctx.getString(R.string.need_to_login))
                .build();
        ((NotificationManager) ctx.getSystemService(
                Context.NOTIFICATION_SERVICE)).notify(42, notify);
        
    }

}

/**
 * Fahrgemeinschaft / Ridesharing App
 * Copyright (c) 2013 by it's authors.
 * Some rights reserved. See LICENSE..
 *
 */

package de.fahrgemeinschaft;

import org.teleportr.ConnectorService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.util.Util;

public class AuthRequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(ConnectorService.AUTH)) {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                    .remove(ProfileFragment.FIRSTNAME)
                    .remove(ProfileFragment.LASTNAME)
                    .remove(ProfileFragment.PASSWORD)
                    .remove(ConnectorService.AUTH)
                    .remove(CONTACT.USER)
                    .commit();
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
        } else if (intent.getAction().equals(ConnectorService.UPGRADE)) {
            Notification notify = new NotificationCompat.Builder(ctx)
                    .setContentIntent(PendingIntent.getActivity(
                            ctx, 42, new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "market://details?id=de.fahrgemeinschaft"))
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP), 0))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(ctx.getString(R.string.error))
                    .setTicker(ctx.getString(R.string.request_app_upgrade))
                    .setContentText(ctx.getString(R.string.request_app_upgrade))
                    .build();
            NotificationManager n = (NotificationManager) ctx.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            n.cancel(43);
            n.notify(43, notify);
        }
    }

}

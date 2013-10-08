package de.fahrgemeinschaft.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.ConnectorService;
import org.teleportr.Place;
import org.teleportr.Ride;
import org.teleportr.RidesProvider;
import org.teleportr.Ride.COLUMNS;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.view.View;
import android.widget.Toast;
import de.fahrgemeinschaft.ContactProvider.CONTACT;
import de.fahrgemeinschaft.FahrgemeinschaftConnector;
import de.fahrgemeinschaft.MainActivity;
import de.fahrgemeinschaft.R;

public class Util {

    private static final String MAILTO = "mailto:";
    private static final String SMS = "sms:";
    private static final String TEL = "tel:";



    public static long getNextDayMorning(long dep) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dep + 24 * 3600000); // plus one day
        c.set(Calendar.HOUR_OF_DAY, 0); // reset
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

    public static boolean handleRideAction(int id, Ride ride, Context ctx) {
        switch (id) {
        case R.id.toggle_active:
            if (ride.isActive()) {
                ride.deactivate().dirty().store(ctx);
            } else {
                ride.activate().dirty().store(ctx);
            }
            ctx.getContentResolver().update(RidesProvider
                    .getRidesUri(ctx), null, null, null);
            ctx.startService(new Intent(ctx, ConnectorService.class)
                    .setAction(ConnectorService.PUBLISH));
            return true;
        case R.id.delete:
            ride.delete();
            ctx.getContentResolver().update(RidesProvider
                    .getRidesUri(ctx), null, null, null);
            ctx.startService(new Intent(ctx, ConnectorService.class)
                    .setAction(ConnectorService.PUBLISH));
            return true;
        case R.id.edit:
            ctx.startActivity(new Intent(Intent.ACTION_EDIT,
                    RidesProvider.getRideUri(ctx, ride.getId())));
            return true;
        case R.id.duplicate:
            ctx.startActivity(new Intent(Intent.ACTION_EDIT, ride.duplicate()));
            return true;
        case R.id.duplicate_retour:
            ride.duplicate();
            List<Place> vias = ride.getVias();
            Place from = ride.getFrom();
            ride.removeVias();
            ride.from(ride.getTo());
            for (int j = vias.size() - 1; j >= 0; j--) {
                ride.via(vias.get(j));
            }
            ride.to(from);
            ctx.startActivity(new Intent(Intent.ACTION_EDIT, ride.store(ctx)));
            return true;
        case R.id.show_website:
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    FahrgemeinschaftConnector.FAHRGEMEINSCHAFT_DE
                    + "/tripdetails.php?trip=" + ride.getRef()
                    )).setClass(ctx, WebActivity.class));
            return true;
        case R.id.share:
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT,
                    "From " + ride.getFrom().getName()
                    + " to " + ride.getTo().getName());
            share.putExtra(Intent.EXTRA_TEXT, 
                    FahrgemeinschaftConnector.FAHRGEMEINSCHAFT_DE
                    + "/tripdetails.php?trip=" + ride.getRef());
            ctx.startActivity(Intent.createChooser(share,
                    ctx.getString(R.string.share)));
            return true;
        }
        return false;
    }

    public static boolean isVisible(String key, JSONObject details) {
        try {
            return details.getJSONObject(
                    FahrgemeinschaftConnector.PRIVACY)
                            .getInt(key) == 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void openContactOptionsChooserDialog(Context ctx, Cursor c) {
        String route = c.getString(COLUMNS.FROM_NAME)
                + " -> " + c.getString(COLUMNS.TO_NAME);
        Intent contact = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        contact.putExtra(Insert.NAME, route);
        Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://www.fahrgemeinschaft.de/" +
                "tripdetails.php?trip=" + c.getString(COLUMNS.REF)))
                        .setClass(ctx, WebActivity.class);
        ArrayList<Intent> intents = new ArrayList<Intent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean add(Intent intent) {
                if (intent != null)
                    return super.add(intent);
                else return false;
            }
        };
        JSONObject details = Ride.getDetails(c);
        String dingens;
        try {
            JSONObject privacy = details.getJSONObject("Privacy");
            if (details.has(CONTACT.MOBILE)) {
                dingens = details.getString(CONTACT.MOBILE);
                contact.putExtra(Insert.PHONE, dingens);
                intents.add(labeledIntent(callIntent(dingens),
                        R.drawable.icn_contact_handy, dingens, ctx));
                intents.add(labeledIntent(smsIntent(dingens, route),
                        R.drawable.icn_contact_sms, dingens, ctx));
            } else if (privacy.getInt(CONTACT.MOBILE) == 4) { // members
                intents.add(labeledIntent(profileIntent(ctx),
                        R.drawable.icn_contact_handy,
                        ctx.getString(R.string.login_required), ctx));
                intents.add(labeledIntent(profileIntent(ctx),
                        R.drawable.icn_contact_sms,
                        ctx.getString(R.string.login_required), ctx));
            } else if (privacy.getInt(CONTACT.MOBILE) == 0) { // request
                intents.add(labeledIntent(web,
                        R.drawable.icn_contact_handy,
                        ctx.getString(R.string.request_contact), ctx));
                intents.add(labeledIntent(web,
                        R.drawable.icn_contact_sms,
                        ctx.getString(R.string.request_contact), ctx));
                Toast.makeText(ctx, ctx.getString(R.string.why_request),
                        Toast.LENGTH_LONG).show();
            }
            if (details.has(CONTACT.LANDLINE)) {
                dingens = details.getString(CONTACT.LANDLINE);
                contact.putExtra(Insert.SECONDARY_PHONE, dingens);
                intents.add(labeledIntent(callIntent(dingens),
                        R.drawable.icn_contact_phone, dingens, ctx));
            } else if (privacy.getInt(CONTACT.LANDLINE) == 4) { // members
                intents.add(labeledIntent(profileIntent(ctx),
                        R.drawable.icn_contact_phone,
                        ctx.getString(R.string.login_required), ctx));
            } else if (privacy.getInt(CONTACT.LANDLINE) == 0) { // request
                intents.add(labeledIntent(web,
                        R.drawable.icn_contact_phone,
                        ctx.getString(R.string.request_contact), ctx));
                Toast.makeText(ctx, ctx.getString(R.string.why_request),
                        Toast.LENGTH_LONG).show();
            }
            if (details.has(CONTACT.EMAIL)) { // 'm'
                dingens = details.getString(CONTACT.EMAIL);
                contact.putExtra(Insert.EMAIL, dingens);
                intents.add(labeledIntent(mailIntent(dingens, route + "\n noch Platz frei?"),
                        R.drawable.icn_contact_email, dingens, ctx));
            } else if (privacy.getInt(CONTACT.EMAIL) == 4) { // members
                intents.add(labeledIntent(profileIntent(ctx),
                        R.drawable.icn_contact_email,
                        ctx.getString(R.string.login_required), ctx));
            } else if (privacy.getInt(CONTACT.EMAIL) == 0) { // request
                intents.add(labeledIntent(web,
                        R.drawable.icn_contact_email,
                        ctx.getString(R.string.request_contact), ctx));
                Toast.makeText(ctx, ctx.getString(R.string.why_request),
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (intents.size() == 0) {
            Toast.makeText(ctx, "private", Toast.LENGTH_SHORT).show();
        } else {
            if (PreferenceManager.getDefaultSharedPreferences(ctx)
                    .getBoolean("create_contact", false)) intents.add(contact);
            ctx.startActivity(Intent.createChooser(web,
                    ctx.getString(R.string.contact))
                        .putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            intents.toArray(new Parcelable[intents.size()])));
        }
    }

    public static LabeledIntent labeledIntent(Intent intent,
                int icon, String label, Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        ComponentName cmp = intent.resolveActivity(pm);
        if (cmp != null) {
            intent.setComponent(cmp);
            Intent resolved = new Intent();
            resolved.setData(intent.getData());
            resolved.setComponent(cmp);
            resolved.setAction(intent.getAction());
            if (intent.getExtras() != null)
                resolved.putExtras(intent.getExtras());
            return new LabeledIntent(intent, ctx.getPackageName(), label, icon);
        }
        return null;
    }

    private static Intent callIntent(String num) {
        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse(TEL + num));
        return call;
    }

    private static Intent smsIntent(String num, String text) {
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse(SMS + num));
        sms.putExtra("sms_body", "noch Platz? \n" + text);
        sms.addCategory(Intent.CATEGORY_DEFAULT);
//        sms.setType("vnd.android-dir/mms-sms");
        return sms;
    }

    public static Intent mailIntent(String a, String text) {
        Intent mail = new Intent(Intent.ACTION_SENDTO, Uri.parse(MAILTO+ a));
        mail.putExtra(Intent.EXTRA_TEXT, text);
        mail.putExtra(Intent.EXTRA_SUBJECT, "Fahrgemeinschaft");
//        mail.setType("plain/text");
        return mail;
    }



    public static Intent profileIntent(Context ctx) {
        return new Intent(ctx, MainActivity.class)
                .setData(Uri.parse("content://de.fahrgemeinschaft/profile"));
    }

    public static Intent aboutIntent(Context ctx) {
        return new Intent(ctx, MainActivity.class)
                .setData(Uri.parse("content://de.fahrgemeinschaft/about"));
    }



    public static void fixStreifenhoernchen(View view) {
        Drawable bg = view.getBackground();
        if (bg != null) {
            if (bg instanceof BitmapDrawable) {
                BitmapDrawable bmp = (BitmapDrawable) bg;
                bmp.mutate(); // make sure that we aren't sharing state anymore
                bmp.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            }
        }
    }
}

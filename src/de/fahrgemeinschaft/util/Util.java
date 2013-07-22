package de.fahrgemeinschaft.util;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.teleportr.Ride;
import org.teleportr.Ride.COLUMNS;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents.Insert;
import android.widget.Toast;
import de.fahrgemeinschaft.R;

public class Util {

    private static final String EMAIL = "EMail";
    private static final String MOBILE = "Mobile";
    private static final String LANDLINE = "Landline";

    public static void openContactOptionsChooserDialog(Context ctx, Cursor c) {
        Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://www.fahrgemeinschaft.de/tripdetails.php?trip="
                        + c.getString(COLUMNS.REF)))
                .setClass(ctx, WebActivity.class);
        String route = c.getString(COLUMNS.FROM_NAME)
                + " -> " + c.getString(COLUMNS.TO_NAME);
        Intent contact = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
        contact.putExtra(Insert.NAME, route);
        ArrayList<Intent> intents = new ArrayList<Intent>();
        JSONObject details = Ride.getDetails(c);
        String dingens;
        try {
            JSONObject privacy = details.getJSONObject("privacy");
            dingens = details.getString(MOBILE);
            if (dingens != null && privacy.getInt(MOBILE) == 1) {
                contact.putExtra(Insert.PHONE, dingens);
                Intent call = labeledIntent(callIntent(dingens),
                        R.drawable.ic_call, dingens, ctx);
                if (call != null) intents.add(call);
                Intent sms = labeledIntent(smsIntent(dingens, route),
                        R.drawable.ic_sms, dingens, ctx);
                if (sms != null) intents.add(sms);
            }
            dingens = details.getString(LANDLINE);
            if (dingens != null && privacy.getInt(LANDLINE) == 1) {
                dingens = dingens.substring(1);
                contact.putExtra(Insert.SECONDARY_PHONE, dingens);
                Intent call = labeledIntent(callIntent(dingens),
                        R.drawable.ic_dial, dingens, ctx);
                if (call != null) intents.add(call);
            }
            dingens = details.getString(EMAIL);
            if (dingens != null && privacy.getInt(EMAIL) == 1) {
                dingens = dingens.substring(1);
                contact.putExtra(Insert.EMAIL, dingens);
                Intent mail = labeledIntent(mailIntent(dingens, route),
                        R.drawable.ic_mail, dingens, ctx);
                if (mail != null) intents.add(mail);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (intents.size() == 0) {
            Toast.makeText(ctx, "private", Toast.LENGTH_SHORT).show();
        } else {
            if (PreferenceManager.getDefaultSharedPreferences(ctx)
                    .getBoolean("create_contact", false)) intents.add(contact);
            ctx.startActivity(Intent.createChooser(web, "Kontakt")
                    .putExtra(Intent.EXTRA_INITIAL_INTENTS,
                            intents.toArray(new Parcelable[intents.size()])));
        }
    }

    public static LabeledIntent labeledIntent(Intent intent,
                int icon, String label, Context ctx) {
        System.out.println("resolving " + label);
        PackageManager pm = ctx.getPackageManager();
        ComponentName cmp = intent.resolveActivity(pm);
        if (cmp != null) {
            System.out.println("found " + cmp);
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
        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
        return call;
    }

    private static Intent smsIntent(String num, String text) {
        Intent sms = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + num));
        sms.putExtra("sms_body", text + "\n noch Platz?");
        sms.addCategory(Intent.CATEGORY_DEFAULT);
//        sms.setType("vnd.android-dir/mms-sms");
        return sms;
    }

    private static Intent mailIntent(String a, String text) {
        Intent mail = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+ a));
        mail.putExtra(Intent.EXTRA_TEXT, text + "\n noch Platz frei?");
        mail.putExtra(Intent.EXTRA_SUBJECT, "Fahrgemeinschaft");
//        mail.setType("plain/text");
        return mail;
    }

}
package de.fahrgemeinschaft;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class ContactProvider extends ContentProvider {
    
    private static final String CONTACTS = "contacts";
    private static final int MAILS = 0;
    private SQLiteOpenHelper db;
    private Cursor c;

    @Override
    public boolean onCreate() {
        db = new SQLiteOpenHelper(getContext(), "contacts.db", null, 1) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE contacts (" +
                        "_id integer PRIMARY KEY AUTOINCREMENT," +
                        "user text, mail text, mobile text," +
                        "landline text, plate text);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            }
        };
        return false;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db.getWritableDatabase().insert(CONTACTS, null, values);
        return null;
    }

    private static final String SELECTMAILS = "SELECT _id, mail, " +
            "COUNT(mail) AS count FROM contacts " +
            "WHERE user = ? GROUP BY mail ORDER BY count DESC";
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        switch(uriMatcher.match(uri)) {
        case MAILS:
            
            return db.getReadableDatabase().rawQuery(SELECTMAILS,
                    new String[]{ uri.getPathSegments().get(1) });
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return db.getWritableDatabase().update(
                CONTACTS, values, "user IS NULL", null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.getReadableDatabase().delete(CONTACTS, "user=?",
                new String[]{ uri.getLastPathSegment() });
    }

    static UriMatcher uriMatcher = new UriMatcher(0);
    static {
        uriMatcher.addURI("de.fahrgemeinschaft.private", "users/*/mails", MAILS);
    }
}

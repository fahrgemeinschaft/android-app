package de.fahrgemeinschaft;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class ContactProvider extends ContentProvider {

    private static final String AUTHORITY = "de.fahrgemeinschaft.private";
    private static final int VERSION = 2;

    public static final class CONTACT {
        public static final String USER = "user";
        public static final String EMAIL = "Email";
        public static final String PLATE = "NumberPlate";
        public static final String MOBILE = "Mobile";
        public static final String LANDLINE = "Landline";
    }

    private static final String TABLE = "contacts";
    private static final int MAILS = 0;
    private SQLiteOpenHelper db;

    @Override
    public boolean onCreate() {
        db = new SQLiteOpenHelper(getContext(), "contacts.db", null, VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + TABLE + " (" +
                        "_id integer PRIMARY KEY AUTOINCREMENT," +
                        CONTACT.USER + " text, " +
                        CONTACT.EMAIL + " text, " +
                        CONTACT.MOBILE + " text, " +
                        CONTACT.LANDLINE + " text," +
                        CONTACT.PLATE + " text);");
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
        db.getWritableDatabase().insert(TABLE, null, values);
        return null;
    }

    private static final String SELECT_MAILS = "SELECT _id, " + CONTACT.EMAIL +
            ", " + "COUNT(" + CONTACT.EMAIL + ") AS count FROM contacts " +
            "WHERE " + CONTACT.USER +  " IS ? " +
            "GROUP BY " + CONTACT.EMAIL + " ORDER BY count DESC";

    @Override
    public Cursor query(Uri uri, String[] p, String s, String[] a, String o) {
        System.out.println("query " + uri);
        switch(uriMatcher.match(uri)) {
        case MAILS:
            
            return db.getReadableDatabase().rawQuery(SELECT_MAILS,
                    new String[]{ uri.getPathSegments().get(1) });
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return db.getWritableDatabase().update(
                TABLE, values, CONTACT.USER + " IS NULL", null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.getReadableDatabase().delete(TABLE, CONTACT.USER + " = ?",
                new String[]{ uri.getLastPathSegment() });
    }

    static UriMatcher uriMatcher = new UriMatcher(0);
    static {
        uriMatcher.addURI(AUTHORITY, "users/*/mails", MAILS);
    }
}

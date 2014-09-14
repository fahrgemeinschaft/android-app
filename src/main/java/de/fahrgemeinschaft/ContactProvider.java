package de.fahrgemeinschaft;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class ContactProvider extends ContentProvider {

    private static final String TABLE = "contacts";
    public static final String AUTHORITY = "de.fahrgemeinschaft.private";
    public static final String URI = "content://" + AUTHORITY + "/contacts";

    public static final class CONTACT {
        public static final String USER = "user";
        public static final String EMAIL = "Email";
        public static final String PLATE = "NumberPlate";
        public static final String MOBILE = "Mobile";
        public static final String LANDLINE = "Landline";
    }

    private static final int VERSION = 3;
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
                db.execSQL("DROP table contacts;");
                onCreate(db);
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
        if (values.containsKey(CONTACT.PLATE) 
                && values.getAsString(CONTACT.PLATE)
                .equals(FahrgemeinschaftConnector.BAHN)) {
            values.remove(CONTACT.PLATE);
        }
        db.getWritableDatabase().insert(TABLE, null, values);
        return null;
    }


    private static String SELECT(String something) {
        return "SELECT _id, " + something + "," +
                " COUNT(" + something + ") AS count FROM contacts" +
                " WHERE " + CONTACT.USER +  " IS ?" +
                " AND " + something + " LIKE ?" +
                " AND " + something + " IS NOT NULL" +
                " AND " + something + " IS NOT ''" +
                " GROUP BY " + something +
                " ORDER BY _id DESC";
    }

    private static final String SELECT_MAILS = SELECT(CONTACT.EMAIL);
    private static final String SELECT_MOBILES = SELECT(CONTACT.MOBILE);
    private static final String SELECT_LANDLINES = SELECT(CONTACT.LANDLINE);
    private static final String SELECT_PLATES = SELECT(CONTACT.PLATE);

    @Override
    public Cursor query(Uri uri, String[] p, String s, String[] a, String o) {
        String query = uri.getQueryParameter("q");
        if (query == null) query = "";
        query = query + "%";
        switch(uriMatcher.match(uri)) {
        case MAILS:
            return db.getReadableDatabase().rawQuery(SELECT_MAILS,
                    new String[]{ uri.getPathSegments().get(1), query });
        case MOBILES:
            return db.getReadableDatabase().rawQuery(SELECT_MOBILES,
                    new String[]{ uri.getPathSegments().get(1), query });
        case LANDLINES:
            return db.getReadableDatabase().rawQuery(SELECT_LANDLINES,
                    new String[]{ uri.getPathSegments().get(1), query });
        case PLATES:
            return db.getReadableDatabase().rawQuery(SELECT_PLATES,
                    new String[]{ uri.getPathSegments().get(1), query });
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

    private static final int MAILS = 1;
    private static final int MOBILES = 2;
    private static final int LANDLINES = 3;
    private static final int PLATES = 4;

    static UriMatcher uriMatcher = new UriMatcher(0);
    static {
        uriMatcher.addURI(AUTHORITY, "users/*/mails", MAILS);
        uriMatcher.addURI(AUTHORITY, "users/*/mobiles", MOBILES);
        uriMatcher.addURI(AUTHORITY, "users/*/landlines", LANDLINES);
        uriMatcher.addURI(AUTHORITY, "users/*/plates", PLATES);
    }
}

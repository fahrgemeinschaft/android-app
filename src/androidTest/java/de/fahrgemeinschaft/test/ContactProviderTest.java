package de.fahrgemeinschaft.test;

import de.fahrgemeinschaft.ContactProvider;
import de.fahrgemeinschaft.ContactProvider.CONTACT;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class ContactProviderTest extends ProviderTestCase2<ContactProvider> {

    public ContactProviderTest() {
        super(ContactProvider.class, "de.fahrgemeinschaft.test");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getMockContentResolver().addProvider(
                "de.fahrgemeinschaft.private", getProvider());
        ContentValues cv = new ContentValues();
        cv.put(CONTACT.EMAIL, "blablamail@gmx.net");
        cv.put(CONTACT.MOBILE, "123");
        cv.put(CONTACT.PLATE, "ABC");
        cv.put(CONTACT.USER, "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
        cv.put(CONTACT.EMAIL, "blablamail@gmx.net");
        cv.put(CONTACT.MOBILE, "123");
        cv.put(CONTACT.PLATE, "XYZ");
        cv.put(CONTACT.USER, "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
        cv.put(CONTACT.EMAIL, "afoo@bar.net");
        cv.put(CONTACT.MOBILE, "456");
        cv.put(CONTACT.PLATE, "XYZ");
        cv.put(CONTACT.USER, "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
    }

    public void testGetMails() {
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mails?q="),
                null, null, null, null);
        assertEquals(2, mails.getCount());
        mails.moveToFirst();
        assertEquals("count", 1, mails.getInt(2));
        assertEquals("afoo@bar.net", mails.getString(1));
        mails.moveToNext();
        assertEquals("count", 2, mails.getInt(2));
        assertEquals("blablamail@gmx.net", mails.getString(1));
    }

    public void testAutocompleteMails() {
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mails?q=af"),
                null, null, null, null);
        assertEquals(1, mails.getCount());
        mails.moveToFirst();
        assertEquals("count", 1, mails.getInt(2));
        assertEquals("afoo@bar.net", mails.getString(1));
    }

    public void testGetMobiles() {
        Cursor mobiles = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mobiles"),
                null, null, null, null);
        assertEquals(2, mobiles.getCount());
        mobiles.moveToFirst();
        assertEquals("456", mobiles.getString(1));
        mobiles.moveToNext();
        assertEquals("123", mobiles.getString(1));
    }

    public void testEmptyValues() {
        ContentValues cv = new ContentValues();
        cv.put(CONTACT.EMAIL, "");
        cv.put(CONTACT.USER, "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mails"),
                null, null, null, null);
        assertEquals(2, mails.getCount());
        Cursor mobiles = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mobiles"),
                null, null, null, null);
        assertEquals(2, mobiles.getCount());
    }

    public void testBahn() {
        ContentValues cv = new ContentValues();
        cv.put(CONTACT.PLATE, "Bahn");
        cv.put(CONTACT.USER, "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
        Cursor plates = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mails"),
                null, null, null, null);
        assertEquals(2, plates.getCount());
    }

    public void testDelete() {
        getMockContentResolver().delete(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo"),
                null, null);
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/foo/mails"),
                null, null, null, null);
        assertEquals(0, mails.getCount());
    }

    public void testStoreContactsBeforeLogin() {
        ContentValues cv = new ContentValues();
        cv.put(CONTACT.EMAIL, "fg@sonnenstreifen.de");
        cv.put(CONTACT.MOBILE, "123456789");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv);
        // reassign all empty contacts to now logged in user..
        cv.clear();
        cv.put(CONTACT.USER, "bar");
        getMockContentResolver().update(Uri.parse(
                "content://de.fahrgemeinschaft.private/contacts"), cv, null, null);
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.private/users/bar/mails"),
                null, null, null, null);
        assertEquals(1, mails.getCount());
        mails.moveToFirst();
        assertEquals("fg@sonnenstreifen.de", mails.getString(1));
    }
}

package de.fahrgemeinschaft.test;

import de.fahrgemeinschaft.ContactProvider;
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
        getProvider();
        ContentValues cv = new ContentValues();
        cv.put("mail", "afoo@bar.net");
        cv.put("mobile", "123456789");
        cv.put("user", "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv);
        cv.put("mail", "blablamail@gmx.net");
        cv.put("mobile", "123456789");
        cv.put("user", "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv);
        cv.put("mail", "blablamail@gmx.net");
        cv.put("mobile", "123456789");
        cv.put("user", "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv);
    }

    public void testGetMails() {
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.test/users/foo/mails"),
                null, null, null, null);
        assertEquals(2, mails.getCount());
        mails.moveToFirst();
        assertEquals("blablamail@gmx.net", mails.getString(1));
        mails.moveToNext();
        assertEquals("afoo@bar.net", mails.getString(1));
    }

    public void testDelete() {
        getMockContentResolver().delete(Uri.parse(
                "content://de.fahrgemeinschaft.test/users/foo"),
                null, null);
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.test/users/foo/mails"),
                null, null, null, null);
        assertEquals(0, mails.getCount());
    }

    public void testStoreContactsBeforeLogin() {
        ContentValues cv = new ContentValues();
        cv.put("mail", "fg@sonnenstreifen.de");
        cv.put("mobile", "123456789");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv);
        // reassign all empty contacts to now logged in user..
        cv.clear();
        cv.put("user", "bar");
        getMockContentResolver().update(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv, null, null);
        Cursor mails = getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.test/users/bar/mails"),
                null, null, null, null);
        assertEquals(1, mails.getCount());
        mails.moveToFirst();
        assertEquals("fg@sonnenstreifen.de", mails.getString(1));
    }
}

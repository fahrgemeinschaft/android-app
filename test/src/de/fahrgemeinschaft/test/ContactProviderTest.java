package de.fahrgemeinschaft.test;

import de.fahrgemeinschaft.ContactProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

public class ContactProviderTest extends ProviderTestCase2<ContactProvider> {

    public ContactProviderTest() {
        super(ContactProvider.class, "de.fahrgemeinschaft.test");
    }

    @Override
    protected void setUp() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put("mail", "blablamail@gmx.net");
        cv.put("mobile", "123456789");
        cv.put("user", "foo");
        getMockContentResolver().insert(Uri.parse(
                "content://de.fahrgemeinschaft.test/contacts"), cv);
        super.setUp();
    }

    public void testGetMails() {
        getMockContentResolver().query(Uri.parse(
                "content://de.fahrgemeinschaft.test/users/foo/mails"),
                null, null, null, null);
        // query mails
        // assert sort order
    }

    public void testStoreContactsBeforeLogin() {
        // reassign all empty contacts to now logged in user..
    }
}

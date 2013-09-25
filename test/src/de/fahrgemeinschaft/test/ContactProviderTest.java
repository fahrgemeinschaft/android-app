package de.fahrgemeinschaft.test;

import de.fahrgemeinschaft.ContactProvider;
import android.test.ProviderTestCase2;

public class ContactProviderTest extends ProviderTestCase2<ContactProvider> {

    public ContactProviderTest() {
        super(ContactProvider.class, "de.fahrgemeinschaft.test");
    }

}

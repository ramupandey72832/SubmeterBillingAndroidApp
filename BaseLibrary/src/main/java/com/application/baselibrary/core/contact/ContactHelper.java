package com.application.baselibrary.core.contact;



import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.database.ContentObserver;

import com.application.baselibrary.core.contact.model.Contact;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ContactHelper {

    private final ContentResolver resolver;
    private final Gson gson = new Gson();
    private ContactChangeListener listener;

    public ContactHelper(Context context) {
        this.resolver = context.getContentResolver();
    }

    public interface ContactChangeListener {
        void onContactsChanged(List<Contact> updatedContacts);
    }

    public void registerObserver(ContactChangeListener listener) {
        this.listener = listener;
        resolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                new ContactObserver(new Handler())
        );
    }

    public void unregisterObserver() {
        resolver.unregisterContentObserver(new ContactObserver(new Handler()));
    }

    public List<Contact> fetchContacts() {
        List<Contact> contacts = new ArrayList<>();
        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            try {
                int idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                int lastUpdatedIdx = cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
                int hasPhoneIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);

                while (cursor.moveToNext()) {
                    Contact contact = new Contact();
                    if (idIdx >= 0) contact.id = cursor.getString(idIdx);
                    if (nameIdx >= 0) contact.name = cursor.getString(nameIdx);
                    if (lastUpdatedIdx >= 0) contact.lastUpdated = cursor.getLong(lastUpdatedIdx);

                    // Phones
                    if (hasPhoneIdx >= 0 && cursor.getInt(hasPhoneIdx) > 0) {
                        Cursor phoneCursor = resolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{contact.id},
                                null
                        );
                        if (phoneCursor != null) {
                            try {
                                int phoneIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                while (phoneCursor.moveToNext()) {
                                    if (phoneIdx >= 0) contact.phones.add(phoneCursor.getString(phoneIdx));
                                }
                            } finally {
                                phoneCursor.close();
                            }
                        }
                    }

                    // Emails
                    Cursor emailCursor = resolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{contact.id},
                            null
                    );
                    if (emailCursor != null) {
                        try {
                            int emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
                            while (emailCursor.moveToNext()) {
                                if (emailIdx >= 0) contact.emails.add(emailCursor.getString(emailIdx));
                            }
                        } finally {
                            emailCursor.close();
                        }
                    }

                    contacts.add(contact);
                }
            } finally {
                cursor.close();
            }
        }
        return contacts;
    }

    public String toJson(List<Contact> contacts) {
        return gson.toJson(contacts);
    }

    private class ContactObserver extends ContentObserver {
        public ContactObserver(Handler handler) { super(handler); }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (listener != null) listener.onContactsChanged(fetchContacts());
        }
    }
}

package com.monmouth.contactpicker

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;

public class ContactPicker extends CordovaPlugin {

    private Context context;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext)
    {
        this.callbackContext = callbackContext;
        this.context = cordova.getActivity().getApplicationContext();

        if(action.equals("pickcontact")) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            cordova.startActivityForResult(this, intent, CHOOSE_CONTACT);
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Uri contactData = data.getData();
            ContentResolver resolver = context.getContentResolver();
            Cursor c =  resolver.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[] { id },
                    null);

            if (c.moveToFirst()) {
                try {
                    String contactId = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String phoneNumber = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA));

                    JSONObject contact = new JSONObject();
                    contact.put("contactId", contactId);
                    contact.put("displayName", name);
                    contact.put("phoneNumber", phoneNumber);
                    callbackContext.success(contact);

                } catch (Exception e) {
                    callbackContext.error("Parsing contact failed: " + e.getMessage());
                }

            } else {
                callbackContext.error("Contact was not available.");
            }

            c.close();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            callbackContext.error("No contact was selected.");
        }
    }

}

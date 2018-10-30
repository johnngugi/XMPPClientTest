package com.example.android.xmppclienttest.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.android.xmppclienttest.ApplicationContextProvider;
import com.example.android.xmppclienttest.R;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jxmpp.jid.parts.Resourcepart;

import java.util.UUID;

public class PreferenceUtilities {

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    static String getUniqueId(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID);
            editor.apply();
        }
        return uniqueID;
    }

    static boolean checkAccountCreated(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

        return uniqueID != null && !uniqueID.equals(CustomConnection.getDefaultUserName());
    }

    static boolean checkUserNameIsSet(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);

        SharedPreferences sharedPrefs = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

        return uniqueID != null && !uniqueID.equals(CustomConnection.getDefaultUserName());
    }

    static String getResourceName(Context context) {
        String resourceNameKey = context.getResources().getString(
                R.string.shared_preference_file);
        String userResourceJidKey = context.getResources().getString(R.string.user_resource_jid_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceNameKey, Context.MODE_PRIVATE);

        return sharedPref.getString(userResourceJidKey, CustomConnection.getDefaultUserResource());
    }

    static String setUserResourceName(Context context, AbstractXMPPConnection connection) {
        String resourceNameKey = context.getResources().getString(
                R.string.shared_preference_file);
        String userResourceJidKey = context.getResources().getString(R.string.user_resource_jid_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceNameKey, Context.MODE_PRIVATE);

        Resourcepart resourcepart = connection.getUser().getResourceOrNull();
        String userJidResource = resourcepart.toString();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(userResourceJidKey, resourcepart.toString());
        editor.apply();

        return userJidResource;
    }

    static boolean checkSubIdIsSet(Context context) {
        String resourceName = context.getResources().getString(
                R.string.shared_preference_file);
        String broadcastSubIdKey = context.getResources().getString(R.string.broadcast_sub_id_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceName, Context.MODE_PRIVATE);

        String defaultUserSub = CustomConnection.getDefaultUserSubscription();
        String userSubId = sharedPref.getString(broadcastSubIdKey, defaultUserSub);

        return !defaultUserSub.equals(userSubId);
    }

    static void saveSubscriptionId(String subId) {
        Context context = ApplicationContextProvider.getContext();

        String resourceName = context.getResources().getString(
                R.string.shared_preference_file);
        String broadcastSubIdKey = context.getResources().getString(R.string.broadcast_sub_id_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                resourceName, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(broadcastSubIdKey, subId);
        editor.apply();
    }

    public static String getSavedHostAddress(Context context) {
        String sharedPrefsFile = context.getResources().getString(
                R.string.shared_preference_file);
        String remoteHostAddressKey =
                context.getResources().getString(R.string.remote_host_address_key);

        SharedPreferences sharedPref = context.getSharedPreferences(
                sharedPrefsFile, Context.MODE_PRIVATE);

        String remoteHostAddress = sharedPref.getString(remoteHostAddressKey, null);

        if (remoteHostAddress == null) {
            String defaultRemoteHost = CustomConnection.getDefaultRemoteHostAddress();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(remoteHostAddressKey, defaultRemoteHost);
            editor.apply();
            return defaultRemoteHost;
        }
        return remoteHostAddress;
    }
}

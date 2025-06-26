package com.best;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {

    private static final String PREF_NAME   = "best_application";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN   = "token";
    private static final String KEY_TOKEN_TIMESTAMP = "tokenExpiresAt";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(int user_id,String token, long tokenExpiresAt) {
        prefs.edit().putInt(KEY_USER_ID, user_id).apply();
        prefs.edit().putString(KEY_TOKEN, token).apply();
        prefs.edit().putLong(KEY_TOKEN_TIMESTAMP, tokenExpiresAt).apply();
    }

    public int getUserId(){
        if (isTokenValid()) {
            return prefs.getInt(KEY_USER_ID, -1);
        } else {
            clear(); // Token expired
            return -1;
        }
    }

    public String getToken() {
        if (isTokenValid()) {
            return prefs.getString(KEY_TOKEN, null);
        } else {
            clear(); // Token expired
            return null;
        }
    }

    private boolean isTokenValid() {
        long maxTime     = prefs.getLong(KEY_TOKEN_TIMESTAMP, 0);
        long currentTime = System.currentTimeMillis() / 1000L;
        return currentTime < maxTime;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}

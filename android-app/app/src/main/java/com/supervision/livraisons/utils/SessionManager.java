package com.supervision.livraisons.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SupervisionSession";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_IDPERS = "idpers";
    private static final String KEY_NOM_COMPLET = "nom_complet";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_CODEPOSTE = "codeposte";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, int idpers, String nomComplet,
                            String login, String codeposte, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_IDPERS, idpers)
                .putString(KEY_NOM_COMPLET, nomComplet)
                .putString(KEY_LOGIN, login)
                .putString(KEY_CODEPOSTE, codeposte)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public int getIdpers() { return prefs.getInt(KEY_IDPERS, -1); }
    public String getNomComplet() { return prefs.getString(KEY_NOM_COMPLET, ""); }
    public String getLogin() { return prefs.getString(KEY_LOGIN, ""); }
    public String getCodeposte() { return prefs.getString(KEY_CODEPOSTE, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, ""); }

    public boolean isLivreur() { return "LIVREUR".equals(getRole()); }
    public boolean isControleur() { return "CONTROLEUR".equals(getRole()); }
}

package com.supervision.livraisons.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.supervision.livraisons.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UiUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Applique couleur + texte en fonction du statut de livraison
     */
    public static void applyStatutStyle(Context ctx, TextView tv, String etatliv) {
        if (etatliv == null) return;
        switch (etatliv) {
            case "EC":
                tv.setText("En Cours");
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.status_ec));
                tv.setBackgroundResource(R.drawable.bg_statut_ec);
                break;
            case "LI":
                tv.setText("Livré ✓");
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.status_li));
                tv.setBackgroundResource(R.drawable.bg_statut_li);
                break;
            case "AL":
                tv.setText("Ajourné");
                tv.setTextColor(ContextCompat.getColor(ctx, R.color.status_al));
                tv.setBackgroundResource(R.drawable.bg_statut_al);
                break;
        }
    }

    /**
     * Retourne le libellé du statut
     */
    public static String getStatutLibelle(String etatliv) {
        if (etatliv == null) return "Inconnu";
        switch (etatliv) {
            case "EC": return "En Cours";
            case "LI": return "Livré";
            case "AL": return "Ajourné";
            default: return etatliv;
        }
    }

    /**
     * Formater la date pour l'affichage
     */
    public static String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            if (isoDate.length() >= 10) {
                // Format ISO: 2026-04-25 ou 2026-04-25T14:30:00
                String datePart = isoDate.substring(0, 10);
                String[] parts = datePart.split("-");
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception e) {
            // ignore
        }
        return isoDate;
    }

    /**
     * Today's date formatted
     */
    public static String getTodayFormatted() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return LocalDate.now().format(DATE_FORMATTER);
        }
        return new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                .format(new java.util.Date());
    }

    /**
     * Format mode de paiement
     */
    public static String formatModePay(String modepay) {
        if ("avant_livraison".equals(modepay)) return "💳 Avant livraison";
        if ("apres_livraison".equals(modepay)) return "💰 Après livraison";
        return modepay != null ? modepay : "";
    }

    /**
     * Afficher/cacher une vue avec animation
     */
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}

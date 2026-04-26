package com.supervision.livraisons.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class FirebaseService {

    private boolean isInitialized = false;

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            new ClassPathResource("firebase-service-account.json").getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            isInitialized = true;
            System.out.println("✅ Firebase Admin SDK initialisé avec succès.");
        } catch (IOException e) {
            System.err.println("❌ Erreur d'initialisation Firebase : fichier 'firebase-service-account.json' introuvable dans src/main/resources/");
            System.err.println("⚠️ Les notifications Push ne seront pas envoyées mais l'application continuera de fonctionner.");
        }
    }

    public void sendNotification(String token, String title, String body) {
        if (!isInitialized || token == null || token.isEmpty()) {
            System.out.println("⏭️ Envoi notification ignoré (Firebase non initialisé ou token vide).");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("🚀 Notification envoyée avec succès : " + response);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification : " + e.getMessage());
        }
    }
}

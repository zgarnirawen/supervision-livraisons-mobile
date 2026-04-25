package com.supervision.livraisons.api;

import com.supervision.livraisons.BuildConfig;
import com.supervision.livraisons.utils.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;
    private static SessionManager sessionManager;

    public static void init(SessionManager sm) {
        sessionManager = sm;
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            // Intercepteur pour ajouter le JWT dans chaque requête
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json");

                if (sessionManager != null && sessionManager.getToken() != null) {
                    builder.header("Authorization", "Bearer " + sessionManager.getToken());
                }

                return chain.proceed(builder.method(original.method(), original.body()).build());
            };

            // Logging (debug seulement)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .callTimeout(20, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    // Reset (pour logout)
    public static void reset() {
        retrofit = null;
        apiService = null;
    }
}

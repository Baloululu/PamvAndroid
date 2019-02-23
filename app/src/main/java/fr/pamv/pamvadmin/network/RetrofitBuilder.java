package fr.pamv.pamvadmin.network;

import java.io.IOException;

import fr.pamv.pamvadmin.TokenManager;
import fr.pamv.pamvadmin.Utils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitBuilder {

    private static final String BASE_URL = Utils.URL_SITE + "api/";

    private final static OkHttpClient client = buildClient();
    private final static Retrofit retrofit = buildRetrofit(client);

    private static OkHttpClient buildClient(){

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request.Builder builder = request.newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Connection", "close");

                        request = builder.build();

                        return chain.proceed(request);
                    }
                });

        return builder.build();
    }

    private static Retrofit buildRetrofit(OkHttpClient client)
    {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build();
    }

    public static <T> T createService(Class<T> service)
    {
        return retrofit.create(service);
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static <T> T createServiceWithAuth(Class<T> service, final TokenManager tokenManager)
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient newClient = client.newBuilder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                Request.Builder builder = request.newBuilder();

                if (tokenManager.getToken().getAccessToken() != null)
                    builder.addHeader("Authorization", "Bearer " + tokenManager.getToken().getAccessToken());

                request = builder.build();
                return chain.proceed(request);
            }
        })
                .authenticator(CustomAuthenticator.getInstance(tokenManager))
                .addInterceptor(logging)
                .build();

        Retrofit newRetrofit = retrofit.newBuilder().client(newClient).build();

        return newRetrofit.create(service);
    }
}

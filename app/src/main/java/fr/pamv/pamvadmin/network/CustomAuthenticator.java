package fr.pamv.pamvadmin.network;

import java.io.IOException;

import fr.pamv.pamvadmin.TokenManager;
import fr.pamv.pamvadmin.entities.AccessToken;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class CustomAuthenticator implements Authenticator {

    private TokenManager tokenManager;
    private static CustomAuthenticator INSTANCE;
    private CustomAuthenticator(TokenManager tokenManager)
    {
        this.tokenManager = tokenManager;
    }

    static synchronized CustomAuthenticator getInstance(TokenManager tokenManager)
    {
        if (INSTANCE == null)
            INSTANCE = new CustomAuthenticator(tokenManager);
        return INSTANCE;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) > 1)
            return null;

        AccessToken token = tokenManager.getToken();

        ApiService service = RetrofitBuilder.createService(ApiService.class);

        Call<AccessToken> call = service.refresh(token.getRefreshToken());

        retrofit2.Response<AccessToken> res = call.execute();

        if (res.isSuccessful())
        {
            AccessToken newToken = res.body();
            tokenManager.saveToken(newToken);

            return response.request().newBuilder().header("Authorization", "Bearer " + res.body().getAccessToken()).build();
        }

        return null;
    }

    private int responseCount(Response response)
    {
        int result = 1;
        while ((response = response.priorResponse()) != null)
        {
            result++;
        }
        return result;
    }
}

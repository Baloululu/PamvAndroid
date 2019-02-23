package fr.pamv.pamvadmin.network;

import fr.pamv.pamvadmin.entities.AccessToken;
import fr.pamv.pamvadmin.entities.ArticleResponse;
import fr.pamv.pamvadmin.entities.CategorieResponse;
import fr.pamv.pamvadmin.entities.Categories;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface ApiService {

    @POST("login")
    @FormUrlEncoded
    Call<AccessToken> login(@Field("username") String username, @Field("password") String password);

    @GET("articles/{article}")
    Call<ArticleResponse> article(@Path("article") int id);

    @GET("{categorie}")
    Call<CategorieResponse> categorie(@Path("categorie") String categorie);

    @POST("refresh")
    @FormUrlEncoded
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @POST("logout")
    Call<Void> logout();

    @GET("articles/create")
    Call<Categories> categories();

    @DELETE("articles/{article}")
    Call<Void> delete(@Path("article") int id);

    @PUT("articles/{article}")
    @FormUrlEncoded
    Call<Void> update(@Path("article") int id,
                      @Field("title") String title,
                      @Field("content") String content,
                      @Field("intro") String intro,
                      @Field("category_id") int category_id);

    @POST("articles/{article}")
    Call<Void> updateImage(@Path("article") int id,
                           @Body RequestBody body);

    @POST("fcmToken")
    @FormUrlEncoded
    Call<Void> fcm (@Field("fcm_token") String token);
}

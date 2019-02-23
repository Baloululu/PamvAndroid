package fr.pamv.pamvadmin;

import android.util.Log;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.pamv.pamvadmin.entities.ApiError;
import fr.pamv.pamvadmin.network.RetrofitBuilder;
import okhttp3.ResponseBody;
import retrofit2.Converter;

public class Utils {

    public static String URL_SITE = "http://192.168.0.25/pamv/";
    private static final String TAG = "Utils";

    public static ApiError convertErrors(ResponseBody response)
    {
        Converter<ResponseBody, ApiError> converter = RetrofitBuilder.getRetrofit().responseBodyConverter(ApiError.class, new Annotation[0]);

        ApiError apiError = null;

        try {
            apiError = converter.convert(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apiError;
    }

    /**
     * Check if the server is local or not and set the right URL
     */
    //FIXME Use AsyncTask
    public static void setUrlSite()
    {
        try {
            //TODO Update with real address
            InetAddress address = InetAddress.getByName("192.168.0.25");
            if (address.isReachable(1000))
                URL_SITE = "http://192.168.0.25/pamv/";
            else
                URL_SITE = "http://192.168.0.25/pamv/";

        } catch (UnknownHostException e) {
            Log.e(TAG, "setUrlSite: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "setUrlSite: " + e.getMessage());
        }
    }
}

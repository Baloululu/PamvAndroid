package fr.pamv.pamvadmin;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.pamv.pamvadmin.entities.AccessToken;
import fr.pamv.pamvadmin.entities.ApiError;
import fr.pamv.pamvadmin.network.ApiService;
import fr.pamv.pamvadmin.network.RetrofitBuilder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @BindView(R.id.email)
    TextInputLayout tilUsername;

    @BindView(R.id.password)
    TextInputLayout tilPassword;

    @BindView(R.id.login_progress)
    ProgressBar progressBar;

    @BindView(R.id.login_form)
    LinearLayout form;

    private ApiService service;
    private Call<AccessToken> call;
    private AwesomeValidation validator;
    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

//        Utils.setUrlSite();

        service = RetrofitBuilder.createService(ApiService.class);
        validator = new AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        setupRules();

        if (tokenManager.getToken().getAccessToken() != null)
        {
            startActivity(new Intent(LoginActivity.this, ShowActivity.class));
            finish();
        }
    }

    @OnClick(R.id.login)
    void login(){

        String email = tilUsername.getEditText().getText().toString();
        String password = tilPassword.getEditText().getText().toString();

        tilUsername.setError(null);
        tilPassword.setError(null);

        validator.clear();

        if (validator.validate())
        {
            loading(true);

            call = service.login(email, password);
            call.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {

                    if (response.isSuccessful())
                    {
                        tokenManager.saveToken(response.body());
                        startActivity(new Intent(LoginActivity.this, ShowActivity.class));
                        finish();
                    }
                    else
                    {
                        if (response.code() == 422)
                            handleErrors(response.errorBody());
                        else
                            Toast.makeText(LoginActivity.this, R.string.error_user, Toast.LENGTH_LONG).show();

                        loading(false);
                    }
                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Impossible de contacter le serveur", Toast.LENGTH_LONG).show();
                    loading(false);
                }
            });
        }
    }

    private void loading(boolean status)
    {
        if(status)
        {
            progressBar.setVisibility(View.VISIBLE);
            form.setVisibility(View.GONE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            form.setVisibility(View.VISIBLE);
        }
    }

    private void handleErrors(ResponseBody response)
    {
        ApiError apiError = Utils.convertErrors(response);

        for (Map.Entry<String, List<String>> error : apiError.getErrors().entrySet())
        {
            if (error.getKey().equals("username"))
                tilUsername.setError(error.getValue().get(0));

            if (error.getKey().equals("password"))
                tilPassword.setError(error.getValue().get(0));
        }
    }

    public void setupRules(){
        validator.addValidation(this, R.id.password, RegexTemplate.NOT_EMPTY, R.string.error_incorrect_password);
        validator.addValidation(this, R.id.email, Patterns.EMAIL_ADDRESS, R.string.error_invalid_email);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null)
        {
            call.cancel();
            call = null;
        }
    }
}


package fr.pamv.pamvadmin;

import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.pamv.pamvadmin.entities.Article;
import fr.pamv.pamvadmin.network.ApiService;
import fr.pamv.pamvadmin.network.RetrofitBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ArticleFragment.OnFragmentInteractionListener {

    private static final String TAG = "ShowActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private ApiService service;
    private TokenManager tokenManager;

    private ArticleFragment pierresFragment;
    private ArticleFragment colliersFragment;
    private ArticleFragment braceletsFragment;
    private ArticleFragment bouclesFragment;
    private ArticleFragment clefsFragment;

    private int currentFragmentId;

    private static final String BUNDLE_FRAGMENT_ID = "FragmentId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        ButterKnife.bind(this);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        //FIXME Toolbar title not set when savedInstanceState is not null
        if (savedInstanceState == null)
            changeFragment(R.id.bracelets);
        else
            changeFragment(savedInstanceState.getInt(BUNDLE_FRAGMENT_ID));

        navigationView.setNavigationItemSelectedListener(this);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();

                        updateFcmToken(token);
                    }
                });

        Log.d(TAG, "onCreate: Create state");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.show_menu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        changeFragment(menuItem.getItemId());

        this.mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    private void startTransactionFragment(Fragment fragment, String category)
    {
        toolbar.setTitle(category);
        if (!fragment.isVisible())
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_layout, fragment).commit();
    }

    @Override
    public void onFragmentArticleInteraction(Article article) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    private void updateFcmToken(String token)
    {
        Call<Void> callFcm = service.fcm(token);
        callFcm.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    public void changeFragment(int id)
    {
        currentFragmentId = id;
        switch (id)
        {
            case R.id.bracelets:
                if (this.braceletsFragment == null)
                    this.braceletsFragment = ArticleFragment.newInstance("bracelets");
                startTransactionFragment(braceletsFragment, getString(R.string.bracelets));
                break;

            case R.id.pierres:
                if (this.pierresFragment == null)
                    this.pierresFragment = ArticleFragment.newInstance("pierres");
                startTransactionFragment(pierresFragment, getString(R.string.pierres));
                break;

            case R.id.colliers:
                if (this.colliersFragment == null)
                    this.colliersFragment = ArticleFragment.newInstance("colliers");
                startTransactionFragment(colliersFragment, getString(R.string.collier));
                break;

            case R.id.boucles:
                if (this.bouclesFragment== null)
                    this.bouclesFragment = ArticleFragment.newInstance("boucles");
                startTransactionFragment(bouclesFragment, getString(R.string.boucles));
                break;

            case R.id.cles:
                if (this.clefsFragment == null)
                    this.clefsFragment = ArticleFragment.newInstance("clefs");
                startTransactionFragment(clefsFragment, getString(R.string.cles));
                break;

            //TODO Comments
            case R.id.commentaires:
                break;

            case R.id.logout:
                Call<Void> callLogout = service.logout();
                callLogout.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        tokenManager.deleteToken();
                        startActivity(new Intent(ShowActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        outState.putInt(BUNDLE_FRAGMENT_ID, currentFragmentId);

        super.onSaveInstanceState(outState, outPersistentState);
    }
}

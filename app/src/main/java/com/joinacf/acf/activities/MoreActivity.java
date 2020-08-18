package com.joinacf.acf.activities;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ListAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.joinacf.acf.R;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.bottom_nav.BottomNavigationViewNew;
import com.joinacf.acf.fragments.CorruptionFragment;
import com.joinacf.acf.fragments.FindnFixFragment;
import com.joinacf.acf.fragments.HomeFragment;
import com.joinacf.acf.fragments.MoreGridFragment;
import com.joinacf.acf.fragments.SocialEvilFragment;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.databinding.ActivityMoreBinding;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

public class MoreActivity extends BaseActivity{

    ActivityMoreBinding dataBiding;
    APIRetrofitClient apiRetrofitClient;
    ArrayList<WallPostsModel.Result> lstWallPost;
    String strCategoryID = "";
    String strName = "";
    HomePageAdapter adapter;
    List<WallPostsModel> myProfileData;
    String strLoginType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_more);
        init();
    }

    private void  init() {
        getSupportActionBar().setTitle(strName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher_icon);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_theme));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            strCategoryID = b.getString("CatergoryID").toString();
            strName = b.getString("Name").toString();
            LoadAdapter();
        }
    }

    private void LoadAdapter()
    {
        showProgressDialog(MoreActivity.this);
        if(App.isNetworkAvailable())
            getWallPostDetails(strCategoryID,"-1");
        else{
            ChocoBar.builder().setView(dataBiding.mainLayout)
                    .setText("No Internet connection")
                    .setDuration(ChocoBar.LENGTH_INDEFINITE)
                    //.setActionText(android.R.string.ok)
                    .red()   // in built red ChocoBar
                    .show();
        }

        dataBiding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MoreActivity.this, NewComplaintActivity.class);
                intent.putExtra("Category",strName);
                startActivity(intent);
            }
        });
    }
    public void getWallPostDetails(String categoryID, String Days) {
        apiRetrofitClient = new APIRetrofitClient();
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        Call<WallPostsModel> call = api.getWallPostDetails(categoryID, Days);

        call.enqueue(new Callback<WallPostsModel>() {
            @Override
            public void onResponse(Call<WallPostsModel> call, Response<WallPostsModel> response) {
                if(response != null) {
                    WallPostsModel myWallData  = response.body();
                    if(myWallData != null) {
                        dataBiding.llNoData.setVisibility(View.GONE);
                        String status = myWallData.getStatus();
                        String msg = myWallData.getMessage();
                        if(msg.equalsIgnoreCase("SUCCESS")) {
                            lstWallPost = myWallData.getResult();
                            populateListView(lstWallPost);
                        }else
                        {
                            dataBiding.llNoData.setVisibility(View.VISIBLE);
                            hideProgressDialog(MoreActivity.this);
                        }
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(MoreActivity.this);
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(MoreActivity.this);
                }
            }

            @Override
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(MoreActivity.this);
            }
        });
        // return lstWallPost;
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        //dataBiding.lvMoreFeed.setAdapter(adapter);
        dataBiding.lvMoreFeed.setLayoutManager(new LinearLayoutManager(MoreActivity.this));
        dataBiding.lvMoreFeed.setItemAnimator(new DefaultItemAnimator());
        dataBiding.lvMoreFeed.setAdapter(new HomePageAdapter(MoreActivity.this,wallPostData));
        hideProgressDialog(MoreActivity.this);
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem action_search= menu.findItem(R.id.action_search);
        action_search.setVisible(false);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSubmitButtonEnabled(true);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(adapter != null)
                    adapter.getFilter().filter(newText.toString());
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:

                break;
            case R.id.myprofile:
                Intent intent = new Intent(MoreActivity.this, MyProfileActivity.class);
                startActivity(intent);
                break;

            case R.id.logout:
                //signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut()
    {
        try {
            strLoginType = getStringSharedPreference(MoreActivity.this, "LoginType");
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MoreActivity.this, gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(MoreActivity.this, "LoggedIn",false);
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(MoreActivity.this, NewLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(MoreActivity.this);
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(MoreActivity.this, "LoggedIn",false);

                Intent intent = new Intent(MoreActivity.this, NewLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
            putBooleanSharedPreference(MoreActivity.this, "FirstTime", false);
            putStringSharedPreference(MoreActivity.this, "LoginType", "");
            putStringSharedPreference(MoreActivity.this, "personName", "");
            putStringSharedPreference(MoreActivity.this, "personEmail", "");
            putStringSharedPreference(MoreActivity.this, "personId", "");
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }*/
}

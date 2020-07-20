package com.joinacf.acf.activities;

import androidx.databinding.DataBindingUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joinacf.acf.R;
import com.joinacf.acf.modelclasses.MyProfileModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.databinding.ActivityProfileBinding;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;

public class ProfileActivity extends BaseActivity {
    APIRetrofitClient apiRetrofitClient;
    ActivityProfileBinding binding;
    String strLoginType;
    ArrayList<MyProfileModel.Result> myProfileResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_profile);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        setActionBarTitle("My Profile");
        binding.profileImage.setImageResource(R.mipmap.ic_profileimage);
        init();
    }

    private void getProfileDetails() {
        Retrofit retrofit = apiRetrofitClient.getRetrofit(APIInterface.BASE_URL);
        APIInterface api = retrofit.create(APIInterface.class);
        String strPhotoURL = getStringSharedPreference(ProfileActivity.this, "personPhoto");
        Glide.with(this)
                .load(strPhotoURL)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.profileImage);

        String strMobileNo = getStringSharedPreference(ProfileActivity.this,"mobile");
        String strEmailId = getStringSharedPreference(ProfileActivity.this,"personEmail");
        System.out.println("Email::" + strEmailId);
        System.out.println("MobileNo::" + strMobileNo);
        Call<MyProfileModel> call = api.getProfileDetailsbyEmail(strEmailId);

        call.enqueue(new Callback<MyProfileModel>() {
            @Override
            public void onResponse(Call<MyProfileModel> call, Response<MyProfileModel> response) {
                hideProgressDialog(ProfileActivity.this);
                MyProfileModel myProfileData = response.body();
                if(myProfileData != null) {
                    String status = myProfileData.getStatus();
                    String msg = myProfileData.getMessage();
                    if (msg.equalsIgnoreCase("SUCCESS")) {
                        myProfileResult = myProfileData.getResult();
                        for (int i = 0; i < myProfileResult.size(); i++) {
                            binding.email.setText(myProfileResult.get(i).getEmail());
                            binding.name.setText(myProfileResult.get(i).getFullName());
                            if(!myProfileResult.get(i).getMobile().equalsIgnoreCase(""))
                                binding.mobileNo.setText(myProfileResult.get(i).getMobile());
                            else
                                binding.mobileNo.setText("XXXXXXXXXX");
                            if(myProfileResult.get(i).getGender().equalsIgnoreCase("M"))
                                binding.gender.setText("Male");
                            else if(myProfileResult.get(i).getGender().equalsIgnoreCase("F"))
                                binding.gender.setText("Female");
                            else
                                binding.gender.setText("");
                            putStringSharedPreference(ProfileActivity.this,"MemberID",myProfileResult.get(i).getMemberID());
                        }
                    } else {
                        showAlert(ProfileActivity.this,"Alert","No record with this Mobile Number","OK");
                        binding.email.setText("");
                        binding.name.setText("");
                        binding.mobileNo.setText("");
                        binding.gender.setText("");
                        putStringSharedPreference(ProfileActivity.this,"MemberID","");
                    }
                }
            }

            @Override
            public void onFailure(Call<MyProfileModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void init()
    {
        apiRetrofitClient = new APIRetrofitClient();
        showProgressDialog(ProfileActivity.this);
        getProfileDetails();
        strLoginType = getStringSharedPreference(ProfileActivity.this, "LoginType");

        binding.myPostings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MyPostingsActivity.class);
                startActivity(intent);
            }
        });

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void signOut()
    {
       /* if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }*/
        try {
            if (strLoginType.equalsIgnoreCase("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(ProfileActivity.this, gso);
                googleSignInClient.signOut();
                putBooleanSharedPreference(ProfileActivity.this, "LoggedIn",false);
                Toast.makeText(getApplicationContext(), "User Logged out successfully", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ProfileActivity.this, NewLoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                FacebookSdk.sdkInitialize(getApplicationContext());
                AppEventsLogger.activateApp(ProfileActivity.this);
                LoginManager.getInstance().logOut();
                putBooleanSharedPreference(ProfileActivity.this, "LoggedIn",false);

                Intent intent = new Intent(ProfileActivity.this, NewLoginActivity.class);
                startActivity(intent);
                finish();
            }
        }catch (Exception e)
        {
            Crashlytics.logException(e);
        }
    }
}

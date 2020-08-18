package com.joinacf.acf.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.joinacf.acf.R;
import com.joinacf.acf.adapters.HomePageAdapter;
import com.joinacf.acf.databinding.ActivityKnowYourActsBinding;
import com.joinacf.acf.modelclasses.KnowYourActModel;
import com.joinacf.acf.modelclasses.MyProfileModel;
import com.joinacf.acf.modelclasses.WallPostsModel;
import com.joinacf.acf.network.APIInterface;
import com.joinacf.acf.network.APIRetrofitClient;
import com.joinacf.acf.utilities.App;
import com.pd.chocobar.ChocoBar;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class KnowYourActsActivity extends BaseActivity implements View.OnClickListener {
    APIRetrofitClient apiRetrofitClient;
    String strCategoryID = "";
    String strName = "";
    ActivityKnowYourActsBinding dataBiding;
    ArrayList<WallPostsModel.Result> lstWallPost;
    HomePageAdapter adapter;
    int nRadioGroup = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBiding = DataBindingUtil.setContentView(this, R.layout.activity_know_your_acts);
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

        dataBiding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.rti) {
                    nRadioGroup = 0;
                } else if(checkedId == R.id.rts) {
                    nRadioGroup = 1;
                } else {
                    nRadioGroup = 2;
                }
            }
        });

        dataBiding.actTelugu.setOnClickListener(this);
        dataBiding.actEnglish.setOnClickListener(this);
        dataBiding.faq.setOnClickListener(this);
        dataBiding.howtoapply.setOnClickListener(this);

    }

    private void LoadAdapter()
    {
        showProgressDialog(KnowYourActsActivity.this);
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
                Intent intent = new Intent(KnowYourActsActivity.this, NewComplaintActivity.class);
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
                            hideProgressDialog(KnowYourActsActivity.this);
                        }
                    }else {
                        dataBiding.llNoData.setVisibility(View.VISIBLE);
                        hideProgressDialog(KnowYourActsActivity.this);
                    }
                }else {
                    dataBiding.llNoData.setVisibility(View.VISIBLE);
                    hideProgressDialog(KnowYourActsActivity.this);
                }
            }

            @Override
            public void onFailure(Call<WallPostsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                hideProgressDialog(KnowYourActsActivity.this);
            }
        });
    }

    private void populateListView(ArrayList<WallPostsModel.Result> wallPostData) {
        adapter = new HomePageAdapter(KnowYourActsActivity.this,wallPostData);
        dataBiding.lvMoreFeed.setLayoutManager(new LinearLayoutManager(KnowYourActsActivity.this));
        dataBiding.lvMoreFeed.setItemAnimator(new DefaultItemAnimator());
        dataBiding.lvMoreFeed.setNestedScrollingEnabled(false);
        dataBiding.lvMoreFeed.setAdapter(adapter);
        hideProgressDialog(KnowYourActsActivity.this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        switch (v.getId()){
            case R.id.act_english:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtieng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if(nRadioGroup == 1){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtseng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{http://api.ainext.in/cctel.pdf
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/cceng.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.act_telugu:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtitel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else if(nRadioGroup == 1){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtstel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/cctel.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.faq:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtifaq.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;

            case R.id.howtoapply:
                if(nRadioGroup == 0){
                    intent.setDataAndType(Uri.parse("http://api.ainext.in/rtiappl.pdf"), "application/pdf");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            }
        }
}

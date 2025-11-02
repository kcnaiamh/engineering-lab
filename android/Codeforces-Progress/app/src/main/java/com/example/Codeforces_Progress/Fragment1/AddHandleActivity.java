package com.example.Codeforces_Progress.Fragment1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Codeforces_Progress.APIInterfaces.ApiInterfaceGetUserInfo;
import com.example.Codeforces_Progress.R;
import com.example.Codeforces_Progress.SQLiteDataBase.DataBaseHelper;
import com.example.Codeforces_Progress.UserInfoClasses.ResultOfUserInfo;
import com.example.Codeforces_Progress.UserInfoClasses.UserInfo;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddHandleActivity extends AppCompatActivity {

    private static final String TAG = "AddHandleActivity",
                                BASE_URL = "https://codeforces.com/api/";
    private EditText editTextHandle; // taking input handle from the user
    private DataBaseHelper dataBaseHelper;
    private ApiInterfaceGetUserInfo apiInterfaceGUI;
    private String imageUrl, handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_handle);

        setLayoutSize();
        retrofit();

        dataBaseHelper = new DataBaseHelper(this);

        // submitting the input handle
        Button addButton = findViewById(R.id.addButtonId);
        editTextHandle = findViewById(R.id.addHandleEditTextId);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handle = editTextHandle.getText().toString();
                editTextHandle.getText().clear();
                handle = handle.replaceAll(" ", "");

                if (handle.length() > 0) {
                    Call<UserInfo> callUI = apiInterfaceGUI.getUserInfo(handle);
                    callUI.enqueue(new Callback<UserInfo>() {

                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                            try {
                                assert response.body() != null;
                                List<ResultOfUserInfo> results = response.body().getResultOfUserInfo();
                                ResultOfUserInfo result = results.get(0);
                                imageUrl = "https:" + result.getTitlePhoto();

                                long rowId = dataBaseHelper.insertHandle(handle, imageUrl);
                                if (rowId != -1) {
                                    toastMessage("Successfully added " + handle);
                                } else {
                                    toastMessage("Failed to add " + handle);
                                }
                            } catch (Exception e) {
                                toastMessage(handle + " not found!");
                                Log.d("BUGGUB", Objects.requireNonNull(e.getMessage()));
                            }
                        }

                        @Override
                        public void onFailure(Call<UserInfo> call, Throwable t) {
                            toastMessage(getString(R.string.warning_2));
                            Log.d("BUGGUB", TAG + ": " + t.getMessage());
                        }
                    });
                } else {
                    toastMessage(getString(R.string.request_1));
                }
            }
        });
    }

    private void setLayoutSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.25));
    }

    private void retrofit() {
        // API client library
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterfaceGUI = retrofit.create(ApiInterfaceGetUserInfo.class);
    }

    private void toastMessage(String message) {
        Toast.makeText(AddHandleActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
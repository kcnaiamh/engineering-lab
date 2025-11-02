package com.example.Codeforces_Progress.Fragment1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Codeforces_Progress.R;
import com.example.Codeforces_Progress.SQLiteDataBase.DataBaseHelper;

public class DeleteHandleActivity extends AppCompatActivity {

    private static final String TAG = "DeleteHandleActivity";
    EditText editTextHandle; // For taking input handle from the user
    Button deleteButton; // For deleting the input handle
    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_handle);

        setLayoutSize();

        dataBaseHelper = new DataBaseHelper(this);

        editTextHandle = findViewById(R.id.deleteHandleEditTextId);
        deleteButton = findViewById(R.id.deleteButtonId);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String handle = editTextHandle.getText().toString();
                editTextHandle.getText().clear();
                handle = handle.replaceAll(" ", "");

                if (handle.length() > 0) {
                    int val = dataBaseHelper.deleteHandle(handle);
                    if (val > 0) {
                        toastMessage("Deleted " + handle);
                    } else {
                        toastMessage(handle + " not found!");
                    }
                } else {
                    toastMessage("Please input a handle");
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

    void toastMessage(String message) {
        Toast.makeText(DeleteHandleActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
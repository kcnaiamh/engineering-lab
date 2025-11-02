package com.example.Codeforces_Progress.Fragment1;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.Codeforces_Progress.R;
import com.example.Codeforces_Progress.SQLiteDataBase.DataBaseHelper;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class Fragment1 extends Fragment {

    public Fragment1() {
        // Required empty public constructor
    }

    private List<String> handleNames = new ArrayList<>();
    private List<String> handleImages = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private DataBaseHelper dataBaseHelper;
    private FloatingActionButton addHandleButton, removeHandleButton;
    private HandleListAdapter handleListAdapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate(create) the layout(fragment_1) for this fragment(container)
        View view = inflater.inflate(R.layout.fragment_1, container, false);

        dataBaseHelper = new DataBaseHelper(getContext());

        addHandleButton = view.findViewById(R.id.addHandleButtonId);
        removeHandleButton = view.findViewById(R.id.removeHandleButtonId);
        recyclerView = view.findViewById(R.id.recyclerViewHandleId);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutId);

        getSavedHandlesInfo();
        setDataInRecyclerView();
        setAnimation(); // don't know why but without this animation works on startup :')

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // to remove deleted handles
                handleNames.clear();
                handleImages.clear();

                getSavedHandlesInfo();
                setDataInRecyclerView();
                setAnimation();

                swipeRefreshLayout.setRefreshing(false);
                toastMessage("Refreshed");
            }
        });

        handleListAdapter.setOnItemClickListener(new HandleListAdapter.ClickListener() {
            @Override
            public void OnItemClick(int position, View v) {
                Intent intent = new Intent(v.getContext(), DataActivity.class);
                intent.putExtra("tag", handleNames.get(position));
                startActivity(intent);
            }
        });

        addHandleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddHandleActivity.class));
            }
        });

        removeHandleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DeleteHandleActivity.class));
            }
        });

        return view;
    }

    private void getSavedHandlesInfo() {
        Cursor resultSet = dataBaseHelper.getAllHandleInfo();
        if (resultSet.getCount() != 0) {
            while (resultSet.moveToNext()) {
                handleNames.add(resultSet.getString(0));
                handleImages.add(resultSet.getString(1));
            }
        } else {
            toastMessage("Enter a Codeforces user handle");
        }
    }

    private void setDataInRecyclerView() {
        handleListAdapter = new HandleListAdapter(getContext(), handleNames, handleImages);
        recyclerView.setAdapter(handleListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setAnimation() {
        LayoutAnimationController layoutAnimationController =
                AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_fall_down);

        recyclerView.setLayoutAnimation(layoutAnimationController);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    private void toastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
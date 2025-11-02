package com.example.Codeforces_Progress.Fragment2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.Codeforces_Progress.APIInterfaces.ApiInterfaceProblemSet;
import com.example.Codeforces_Progress.ProblemSetClasses.ProblemSet;
import com.example.Codeforces_Progress.ProblemSetClasses.Problems;
import com.example.Codeforces_Progress.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Fragment2 extends Fragment {

    public Fragment2() {
        // Required empty public constructor
    }

    private static final String TAG = "BUGGUB";
    private static final String BASE_URL = "https://codeforces.com/api/";
    private RecyclerView recyclerView;
    private ProblemListAdapter problemListAdapter;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiInterfaceProblemSet apiInterfacePS;
    private List<String> problemNames = new ArrayList<>();
    private List<String> ClonedProblemNames = new ArrayList<>();
    private List<Integer> problemRating = new ArrayList<>();
    private List<List<String>> problemTags = new ArrayList<>();
    private HashMap<String, String> problemUrl = new HashMap<>();

    // arguments for passing as relative url
    String problemSetName = "";
    String tags = "";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate(create) the layout(fragment_2) for this fragment(container)
        View view = inflater.inflate(R.layout.fragment_2, container, false);

        setViews(view);
        setRetrofit();
        getProblemList();
        setRecyclerView();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onRefresh() {
                problemNames.clear();
                getProblemList();
                setRecyclerView();

                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getActivity(), "Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        problemListAdapter.setOnItemClickListener(new ProblemListAdapter.ClickListener() {
            @Override
            public void OnItemClick(int position, View v) {
                Intent intentProblemActivity = new Intent(v.getContext(), ProblemActivity.class);
                intentProblemActivity.putExtra("tag", problemUrl.get(problemNames.get(position)));
                startActivity(intentProblemActivity);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                problemListAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return view;
    }

    private void setViews(View view) {
        searchView = view.findViewById(R.id.searchViewId);
        recyclerView = view.findViewById(R.id.recyclerViewProblemListId);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayoutId);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setRecyclerView() {
        problemListAdapter = new ProblemListAdapter(getContext(), problemNames, ClonedProblemNames, problemTags, problemRating);
        recyclerView.setAdapter(problemListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()),
                DividerItemDecoration.VERTICAL));
    }

    private void setRetrofit() {
        // API client library
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterfacePS = retrofit.create(ApiInterfaceProblemSet.class);
    }

    private void getProblemList() {
        Call<ProblemSet> call = apiInterfacePS.getProblemSet(problemSetName, tags);

        call.enqueue(new Callback<ProblemSet>() {

            @Override
            public void onResponse(Call<ProblemSet> call, Response<ProblemSet> response) {
                assert response.body() != null;
                List<Problems> results = response.body().getResults().getProblems();
                for (Problems result : results) {
                    String name = result.getContestId() + result.getIndex() + ": " + result.getName();
                    String url = "https://codeforces.com/problemset/problem/" + result.getContestId() + "/" + result.getIndex() + "?mobile=true";
                    problemNames.add(name);
                    ClonedProblemNames.add(name);
                    problemUrl.put(name, url);
                    problemRating.add(result.getRating());
                    problemTags.add(result.getTags());
                    problemListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ProblemSet> call, Throwable t) {
                toastMessage(getString(R.string.warning_2));
            }
        });
    }

    private void toastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
package com.example.Codeforces_Progress.Fragment1;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Codeforces_Progress.APIInterfaces.ApiInterfaceGetStatus;
import com.example.Codeforces_Progress.APIInterfaces.ApiInterfaceGetUserInfo;
import com.example.Codeforces_Progress.R;
import com.example.Codeforces_Progress.UserInfoClasses.ResultOfUserInfo;
import com.example.Codeforces_Progress.UserInfoClasses.UserInfo;
import com.example.Codeforces_Progress.UserStatusClasses.ResultUS;
import com.example.Codeforces_Progress.UserStatusClasses.UserStatus;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.text.DateFormat.getDateInstance;

/**
 * 5 methods and 1 inner-class in DataActivity class
 * methods:
 * {@link #onCreate(Bundle)
 * @link #setViews()
 * @link #setRetrofit()
 * @link #init2DList()
 * @link #toastMessage(String)}
 * <p>
 * class:
 * {@link Task}
 */
public class DataActivity extends AppCompatActivity {

    private static final String TAG = "BUGGUB";
    private static final String BASE_URL = "https://codeforces.com/api/";
    private Integer MAX_LIMIT_CONTESTS = 2000;
    private Integer MAX_PARTICIPATED_CONTEST_ID = 0;
    private Integer MIN_PARTICIPATED_CONTEST_ID = MAX_LIMIT_CONTESTS;

    private ApiInterfaceGetStatus apiInterfaceGS;
    private ApiInterfaceGetUserInfo apiInterfaceGUI;

    private ScatterChart scatterChart;
    private ArrayList<ArrayList<Entry>> scatterEntries = new ArrayList<>();
    private ArrayList<IScatterDataSet> scatterDataSets = new ArrayList<>();

    private LineChart lineChart;
    private ArrayList<ArrayList<Entry>> lineEntries = new ArrayList<>();
    private ArrayList<Entry> xAxisDummy = new ArrayList<>();
    private ArrayList<Integer> participatedContestId = new ArrayList<>();
    private ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

    private LinearLayout LLProgressBar, LLData; // LL -> linear layout

    private ImageView avatar;
    private TextView handleName, fullName, rating, countryName, organizationName, rank,
            contribution, maxRank, friendOfCount, email, registered,
            scoreView, lastAccepted;

    private int x, y;
    private long currentValue, prevSubmissionTime, diff, userScore;
    private Boolean firstTime = true, bgWorkDone = false, b1 = false, b2 = false;

    private Double penalty;
    private String handle;

    private ProgressBar score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // for passing data from one layout to another
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            handle = bundle.getString("tag");
        } else {
            finish();
        }

        setViews();
        setRetrofit();
        init2DList();
        new Task().execute();

        scatterChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                toastMessage("Rating: " + (int) e.getY() +
                        "\nAC Problem No: " + (int) e.getX());
            }

            @Override
            public void onNothingSelected() {
                // do nothing :)
            }
        });
    }

    /**
     * 11 methods in Task lass which extends AsyncTask Interface
     * overriden methods:
     * {@link #onPreExecute()}
     * {@link #doInBackground(String...)}
     * {@link #onProgressUpdate(Integer...)}
     * {@link #onPostExecute(Boolean)}
     * <p>
     * normal methods:
     * {@link #getLastAcceptedProblem(List)}
     * {@link #setScatterChartData()}
     * {@link #setScatterChartAttribute()}
     * {@link #setLineChartData()}
     * {@link #setLineChartAttribute()}
     * {@link #setUserInfo(ResultOfUserInfo)}
     * {@link #setColorWithRating(ResultOfUserInfo)}
     */
    @SuppressLint("StaticFieldLeak")
    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LLProgressBar.setVisibility(View.VISIBLE);
            LLData.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            Call<UserStatus> callUS = apiInterfaceGS.getUserStatus(handle);
            callUS.enqueue(new Callback<UserStatus>() {

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResponse(Call<UserStatus> call, Response<UserStatus> response) {
                    try {
                        assert response.body() != null;
                        List<ResultUS> results = response.body().getResults();

                        getLastAcceptedProblem(results);

                        // to get oldest to newest sumbission results
                        Collections.reverse(results);
                        HashSet<String> solvedProblems = new HashSet<>();

                        for (ResultUS result : results) {
                            if (result.getVerdict().equals("OK")) {
                                y = result.getProblem().getRating();
                                String problemName = result.getProblem().getName();

                                if (y >= 800 && y <= 3500 && !solvedProblems.contains(problemName)) {
                                    /*
                                     * Storing data(rating & submission number) for rated solved problems.
                                     * Same rating data saved in same arraylist.
                                     */
                                    scatterEntries.get(y / 100 - 8).add(new Entry(x, y));
                                    solvedProblems.add(problemName);
                                    x++;

                                    // for score calculation
                                    if (firstTime) {
                                        prevSubmissionTime = result.getCreationTimeSeconds();
                                        firstTime = false;
                                    } else {

                                        // getting two consecutive different accepted problem submission time in days
                                        diff = (result.getCreationTimeSeconds() - prevSubmissionTime) / 86400;
                                        prevSubmissionTime = result.getCreationTimeSeconds();
                                    }

                                    if (y < 2500) {
                                        penalty = (Math.sqrt(2500 - y) + 1) * 0.005;
                                    } else {
                                        penalty = ((y / 6000 - 0.6) * (y / 6000 - 0.6)) + 0.01;
                                    }

                                    currentValue += (y - y * penalty * diff);

                                    /*
                                     * storing those problems which were solved in contest time
                                     * and getting maximum and minimum participated contest Id
                                     */
                                    if (result.getAuthor().getParticipantType().equals("CONTESTANT")) {
                                        lineEntries.get(result.getContestId()).add(new Entry(result.getContestId(), y));
                                        participatedContestId.add(result.getContestId());
                                        MAX_PARTICIPATED_CONTEST_ID = Math.max(MAX_PARTICIPATED_CONTEST_ID, result.getContestId());
                                        MIN_PARTICIPATED_CONTEST_ID = Math.min(MIN_PARTICIPATED_CONTEST_ID, result.getContestId());
                                    }
                                }
                            }
                        }
                        if (x > 0) {
                            userScore = currentValue / 35 / x;
                        }
                        scoreView.setText(String.valueOf(userScore + "/100"));

                        setScatterChartData();
                        setScatterChartAttribute();
                        setLineChartData();
                        setLineChartAttribute();

                        // passing 1 for setting true b1
                        publishProgress(1);
                    } catch (Exception e) {
                        toastMessage(String.valueOf(R.string.warning_1));
                        Log.d("BUGGUB", Objects.requireNonNull(e.getMessage()));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<UserStatus> call, Throwable t) {

                    // passing 1 & 2 for setting true bgWrokDone
                    publishProgress(1);
                    publishProgress(2);
                    toastMessage(String.valueOf(R.string.warning_2));
                    finish();
                }
            });


            Call<UserInfo> callUI = apiInterfaceGUI.getUserInfo(handle);
            callUI.enqueue(new Callback<UserInfo>() {

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResponse(Call<UserInfo> call, Response<UserInfo> response) {
                    try {
                        assert response.body() != null;
                        List<ResultOfUserInfo> results = response.body().getResultOfUserInfo();

                        ResultOfUserInfo result = results.get(0);

                        setUserInfo(result);

                        // setting color of handle name and maximum rating
                        setColorWithRating(result);

                        // passing 2 for setting true b2
                        publishProgress(2);
                    } catch (Exception e) {
                        toastMessage(getString(R.string.warning_1));
                        Log.d(TAG, "onResponse: " + e.getMessage());
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<UserInfo> call, Throwable t) {

                    // passing 1 & 2 for setting true bgWrokDone
                    publishProgress(1);
                    publishProgress(2);
                    toastMessage(getString(R.string.warning_2));
                    finish();
                }
            });

            while (!bgWorkDone) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        private void getLastAcceptedProblem(List<ResultUS> results) {

            // for getting & setting last Accepted problem's name, rating & submission time
            for (ResultUS result : results) {
                if (result.getVerdict().equals("OK")) {
                    String lastACProblem = result.getProblem().getName();
                    if (result.getProblem().getRating() != -1) {
                        lastACProblem += ", " + result.getProblem().getRating();
                    }

                    lastACProblem += ", " + getDateInstance().format(new Date(result.getCreationTimeSeconds() * 1000L));

                    if (!lastACProblem.equals("null")) {
                        lastAccepted.setText("Last AC: " + lastACProblem);
                    } else {
                        lastAccepted.setVisibility(View.GONE);
                    }
                    break;
                }
            }
        }

        private void setScatterChartData() {

            // getting the color for individual rating
            int[] ratingArray = getResources().getIntArray(R.array.cf_lvl);

            for (int i = 0; i < 28; i++) {
                if (scatterEntries.get(i).size() > 0) {
                    ScatterDataSet sds = new ScatterDataSet(scatterEntries.get(i), "");
                    sds.setColor(ratingArray[i]);
                    sds.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
                    sds.setScatterShapeSize(11f);
                    scatterDataSets.add(sds);
                }
            }
            ScatterData scatterData = new ScatterData(scatterDataSets);
            scatterChart.setData(scatterData);
        }

        private void setScatterChartAttribute() {

            scatterChart.setDragEnabled(true);
            scatterChart.setScaleEnabled(true);
            scatterChart.animateXY(0, 5000, Easing.EaseOutBounce, Easing.EaseOutBounce);
            scatterChart.invalidate();
            scatterChart.getDescription().setEnabled(false);
            scatterChart.getLegend().setEnabled(false);
            scatterChart.getAxisRight().setEnabled(false);

            XAxis xAxis = scatterChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(true);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(1f);
            xAxis.setAxisMaximum(x + 10);

            YAxis yAxis = scatterChart.getAxisLeft();
            yAxis.setDrawGridLines(true);
            yAxis.setSpaceTop(1);
            yAxis.setAxisMinimum(750f);
        }

        private void setLineChartData() {

            LineDataSet lineDataSet;
            /*
             * setting Dummy xAxis to get the whole graph
             * as MPAndroidChart library doesn't
             * work correctly on a graph which contains
             * non-decreasing order of x-axis values
             */
            xAxisDummy.add(new Entry(MIN_PARTICIPATED_CONTEST_ID, 800));
            xAxisDummy.add(new Entry(MAX_PARTICIPATED_CONTEST_ID, 800));
            lineDataSet = new LineDataSet(xAxisDummy, "");
            lineDataSet.setColor(Color.WHITE, 0);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setValueTextColor(Color.WHITE);
            lineDataSets.add(lineDataSet);

            for (Integer Id : participatedContestId) {
                lineDataSet = new LineDataSet(lineEntries.get(Id), "");
                lineDataSet.setColor(Color.GREEN, 200);
                lineDataSet.setCircleHoleRadius(8f);
                lineDataSet.setCircleColors(ColorTemplate.COLORFUL_COLORS);
                lineDataSets.add(lineDataSet);
            }

            lineChart.setData(new LineData(lineDataSets));
        }

        private void setLineChartAttribute() {

            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            lineChart.animateXY(3000, 0, Easing.EaseInElastic, Easing.EaseInExpo);
            lineChart.postInvalidate();
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(false);
            lineChart.getAxisRight().setEnabled(false);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(true);
            xAxis.setGranularityEnabled(true);
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            YAxis yAxis = lineChart.getAxisLeft();
            yAxis.setDrawGridLines(true);
            yAxis.setSpaceTop(1);
            yAxis.setGranularityEnabled(true);
            yAxis.setGranularity(100f);
        }

        private void setUserInfo(ResultOfUserInfo result) {

            String _ImageAvatar = "https:" + result.getTitlePhoto();
            String _FullName = "Name: " + result.getFirstName() + " " + result.getLastName();
            String _Rating = result.getRating() + "/" + result.getMaxRating();
            String _CountryName = "Country: " + result.getCountry();
            String _OrganizationName = "Organization: " + result.getOrganization();
            String _Rank = "Rank: " + result.getRank();
            String _Contribution = "Contribution: " + result.getContribution();
            String _MaxRank = result.getMaxRank();
            String _FriendOfCount = "Friend of: " + result.getFriendOfCount();
            String _Email = "Email: " + result.getEmail();
            String date = getDateInstance().format(new Date(result.getRegistrationTimeSeconds() * 1000L));
            String _Registered = "Registered: " + date;

            Picasso.with(DataActivity.this).load(_ImageAvatar).into(avatar);

            if (!_FullName.equals("Name: null null")) {
                fullName.setText(_FullName);
            } else {
                fullName.setVisibility(View.GONE);
            }

            if (!_CountryName.equals("Country: null")) {
                countryName.setText(_CountryName);
            } else {
                countryName.setVisibility(View.GONE);
            }

            if (!_OrganizationName.equals("Organization: ") && !_OrganizationName.equals("Organization: null")) {
                organizationName.setText(_OrganizationName);
            } else {
                organizationName.setVisibility(View.GONE);
            }

            if (!_Rank.equals("Rank: null")) {
                rank.setText(_Rank);
            } else {
                rank.setVisibility(View.GONE);
            }

            if (!_Email.equals("Email: null")) {
                email.setText(_Email);
            } else {
                email.setVisibility(View.GONE);
            }

            rating.setText(_Rating);
            contribution.setText(_Contribution);
            maxRank.setText(_MaxRank);
            friendOfCount.setText(_FriendOfCount);
            registered.setText(_Registered);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void setColorWithRating(ResultOfUserInfo result) {
            for (int i = 0; i < 2; i++) {

                int ratingNumber = 0;
                if (i == 0) {
                    ratingNumber = result.getRating(); // current rating
                } else {
                    ratingNumber = result.getMaxRating(); // max rating
                }

                if (ratingNumber == 0) {
                    // color is black
                } else if (ratingNumber < 1200) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_800, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_800, getResources().newTheme()));
                    }
                } else if (ratingNumber < 1400) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_1300, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_1300, getResources().newTheme()));
                    }
                } else if (ratingNumber < 1600) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_1500, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_1500, getResources().newTheme()));
                    }
                } else if (ratingNumber < 1900) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_1700, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_1700, getResources().newTheme()));
                    }
                } else if (ratingNumber < 2100) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_2000, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_2000, getResources().newTheme()));
                    }
                } else if (ratingNumber < 2300) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_2200, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_2200, getResources().newTheme()));
                    }
                } else if (ratingNumber < 2400) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_2300, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_2300, getResources().newTheme()));
                    }
                } else if (ratingNumber < 2600) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_2500, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_2500, getResources().newTheme()));
                    }
                } else if (ratingNumber < 3000) {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_2800, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_2800, getResources().newTheme()));
                    }
                } else {
                    if (i == 0) {
                        handleName.setTextColor(getResources().getColor(R.color.lvl_3000, getResources().newTheme()));
                    } else {
                        maxRank.setTextColor(getResources().getColor(R.color.lvl_3000, getResources().newTheme()));
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] == 1) b1 = true;
            if (values[0] == 2) b2 = true;
            if (b1 && b2) bgWorkDone = true;
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            LLProgressBar.setVisibility(View.GONE);
            LLData.setVisibility(View.VISIBLE);

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= userScore; i++) {
                        try {
                            Thread.sleep(20);
                            score.setProgress(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();

            super.onPostExecute(aBoolean);
        }
    }

    private void setViews() {

        handleName = findViewById(R.id.handleId);
        handleName.setText(handle);

        LLProgressBar = findViewById(R.id.LLProgressBarId);
        LLData = findViewById(R.id.LLDataId);
        scatterChart = findViewById(R.id.scatterChart);
        lineChart = findViewById(R.id.lineChartId);
        avatar = findViewById(R.id.avatarId);
        fullName = findViewById(R.id.fullNameId);
        rating = findViewById(R.id.ratingId);
        countryName = findViewById(R.id.countryId);
        organizationName = findViewById(R.id.organizationId);
        rank = findViewById(R.id.rankId);
        contribution = findViewById(R.id.contributionId);
        maxRank = findViewById(R.id.maxRankId);
        friendOfCount = findViewById(R.id.friendOfCountId);
        email = findViewById(R.id.emailId);
        registered = findViewById(R.id.registeredId);
        score = findViewById(R.id.scoreId);
        scoreView = findViewById(R.id.scoreViewId);
        lastAccepted = findViewById(R.id.lastAcceptedId);
    }

    private void setRetrofit() {
        // API client library
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterfaceGS = retrofit.create(ApiInterfaceGetStatus.class);
        apiInterfaceGUI = retrofit.create(ApiInterfaceGetUserInfo.class);
    }

    private void init2DList() {
        for (int i = 0; i < 28; i++) {
            scatterEntries.add(new ArrayList<Entry>());
        }

        for (int i = 0; i < MAX_LIMIT_CONTESTS; i++) {
            lineEntries.add(new ArrayList<Entry>());
        }
    }

    private void toastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
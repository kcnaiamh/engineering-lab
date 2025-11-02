package com.example.Codeforces_Progress.APIInterfaces;

import com.example.Codeforces_Progress.ProblemSetClasses.ProblemSet;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterfaceProblemSet {
    /*
     * retrofit will use relative URL to fetch data and
     * it will add body to the following abstract method
     */
    @GET("problemset.problems")

    /*
     * problemsetName: Custom problemset's short name, like 'acmsguru'
     * tags: Semicilon-separated list of tags.
     */
    Call<ProblemSet> getProblemSet(@Query("problemsetName") String problemsetName, @Query("tags") String tags);

}

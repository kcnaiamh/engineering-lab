package com.example.Codeforces_Progress.ProblemSetClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProblemSet {


    @SerializedName("result")
    @Expose
    private ResultOfProblemSet results = null;

    /*
     * Returns a "list of Submission objects", sorted in decreasing order of submission id.
     */
    public ResultOfProblemSet getResults() {
        return results;
    }
}

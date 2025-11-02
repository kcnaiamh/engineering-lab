package com.example.Codeforces_Progress.UserStatusClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserStatus {

    // name is changed from result to results
    // so serializedname needed
    @SerializedName("result")
    private List<ResultUS> results = null;

    /*
     * Returns a "list of Submission objects", sorted in decreasing order of submission id.
     */
    public List<ResultUS> getResults() {
        return results;
    }
}

package com.example.Codeforces_Progress.ProblemSetClasses;

import java.util.ArrayList;

public class Problems {
    private Integer contestId;
    private String index;
    private String name;
    private Integer rating;
    private ArrayList<String> tags;

    public int getContestId() {
        return contestId;
    }

    public String getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    // this should be returned in Integer literal
    public Integer getRating() {
        return rating;
    }

    public ArrayList<String> getTags() {
        return tags;
    }
}

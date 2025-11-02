package com.example.Codeforces_Progress.UserStatusClasses;

public class ProblemUS {

    private String name;
    private Integer rating = -1;

    /*
     * Problem name
     */
    public String getName() {
        return name;
    }

    /*
     * Integer.
     * Can be absent.
     * Problem rating (difficulty).
     */
    public int getRating() {
        return rating;
    }
}
package com.example.Codeforces_Progress.UserStatusClasses;

public class ResultUS {

    private Long id;
    private Integer contestId;
    private Long creationTimeSeconds;
    private ProblemUS problem;
    private Author author;
    private String verdict;

    /*
     * submission id
     */
    public long getId() {
        return id;
    }

    /*
     * Integer.
     * Can be absent.
     * Id of the contest, in which party is participating.
     */
    public int getContestId() {
        return contestId;
    }

    /*
     * when the code is submitted
     */
    public long getCreationTimeSeconds() {
        return creationTimeSeconds;
    }

    /*
     * Returns an object which has details of a problem
     */
    public ProblemUS getProblem() {
        return problem;
    }

    /*
     * Author's Handle name
     */
    public Author getAuthor() {
        return author;
    }

    /*
     * Verdict of the submission
     */
    public String getVerdict() {
        return verdict;
    }
}

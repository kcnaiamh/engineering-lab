![ic-launcher](https://i.ibb.co/whKHFNf/ic-launcher.png)

# Codeforces Progress

Codeforces Progress is an android application that tracks the progress of a user in codeforces.

## Uniqueness of this application

- It has a scoring system. This scoring system is based on regular practicing. One can get motivated by seeing their score and keep pushing themselves to do harder problems to gain more score. The challenge is that if someone does less difficulty level problems and/or doesn't solve problems on codeforces frequently then his score will decrease. 

- One can see the overall accepted submission of a user and their(accepted problem's) corresponding rating in chronological order.

- In codeforces, one can see just a line graph of rating change of a user in a contest on codeforces. But in this app, we can see the rating of the submitted problem in a contest using a bar like line graph.

- One can see the name, rating and date of the last accepted problem of a user.

- There is a problem list to browse through problems.

## Scoring System

if (problemRating < 2500)

>![first eq](https://latex.codecogs.com/gif.latex?penalty%20%3D%20%28%5Csqrt%7B2500%20-%20problemRating%7D%20&plus;%201%29%20*%200.005)

else 

>![second_eq](https://latex.codecogs.com/gif.latex?penalty%20%3D%20%28%5Cfrac%7BproblemRating%7D%7B6000%7D%20-%200.6%29%5E2%20&plus;%200.01)

![third_eq](https://latex.codecogs.com/gif.latex?currentValue%20%3D%20%5Csum%20%28problemRating%20-%20probleRating%20*%20penalty%20*%20ConsecutiveAcceptedSubmissionDateDiff%29)

![fourth_eq](https://latex.codecogs.com/gif.latex?mean%20%3D%20%5Cdisplaystyle%7B%5Cfrac%7BcurrentValue%7D%7BtotalAcceptedSolution%7D%7D)

![fifth_eq](https://latex.codecogs.com/gif.latex?userScore%20%3D%20%5Cdisplaystyle%7B%5Cfrac%7Bmean%20*%20100%7D%7B3500%7D%7D)

<br><a href="https://ibb.co/wJKWB0D"><img src="https://i.ibb.co/pW3zb0C/Annotation-2020-07-19-195506.png" alt="Annotation-2020-07-19-195506" border="0"></a><br>
Here x-axis represents the difficulty level of a problem and y-axis represents the penalty.

Before 2500 penalty will be counted from positive **green** graph and after 2500(included) penalty will be counted from **red** graph.

## Live Preview
https://github.com/NaimulIslam9m/Codeforces-Progress/assets/52814980/ca43a8c7-2415-4bf3-9975-31f1c6c30f0b

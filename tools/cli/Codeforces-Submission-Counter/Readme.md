
# Codeforces Submission Counter

A command line utility for checking real time submission count of each problem according to rank of participants


## Deployment

To deploy this project run

```powershell
  python -m pip install --upgrade pip
  pip install psycopg2
  pip install bs4
  pip install requests
```

You need to have postgresql as database.

In the CFTracer folder create database.ini file and put the following lines

```
[postgresql]
host=localhost
database=codeforces
user=postgres
password=<<YOUR postgres USER PASSWORD>>
```
## Live Preview

https://user-images.githubusercontent.com/52814980/158520841-11fe1a92-c80a-41ab-bb4a-c93c0d26ed47.mp4


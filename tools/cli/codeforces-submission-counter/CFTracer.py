from time import sleep
import psycopg2
from config import config
from bs4 import BeautifulSoup
import requests
import os

# contest_number = "1647"
contest_number = input("Contest ID: ")

ranks = ["Newbie", "Pupil", "Specialist", "Expert", "Candidate Master", \
         "Master", "International Master", "Grandmaster", "International Grandmaster", "Legendary Grandmaster"]

def problem_IDs():
    url = "https://codeforces.com/contest/" + contest_number
    html_text = requests.get(url).text

    soup = BeautifulSoup(html_text, "lxml")

    a = soup.findAll("table", class_="problems")
    b = a[0].findAll("tr")
    IDs = []

    for i in range(1, len(b)):
        IDs.append(b[i].td.a.text.strip())

    return IDs

problem_ids = problem_IDs()

def print_output(cur):
    os.system('cls' if os.name == 'nt' else 'clear') # clean console for refresh

    for id in problem_ids:
        print(f"Problem {id}:")
        for rank in ranks:
            cur.execute(f"SELECT COUNT(username) FROM contest WHERE problem_ID = '{id}' AND rank = '{rank}';")
            cnt = int(''.join(map(str, cur.fetchone())))
            if (cnt > 0):
                print("\t{0}: {1}".format(rank, cnt))

def get_url(page_no) -> str:
    url1 = "https://codeforces.com/"
    url2 = "contest/" + contest_number + "/status/page/"
    url3 = "?order=BY_ARRIVED_ASC"

    return url1 + url2 + str(page_no) + url3

def get_username_rank(row) -> list:
    s = str(row.find("td", class_="status-party-cell"))
    a = s.find('title="') + 7 # starting
    b = s.find('"', a) # ending
    c = s.rfind(" ", a, b) # first space from last
    username = s[c + 1:b]
    rank = s[a:c]

    return [username, rank]

def next_page_no(s) -> int:
    p = s.find("page/") + 5
    q = s.find("?", p)
    return int(s[p:q])

def next_page(soup, page_no) -> int:
    x = soup.findAll("div", class_="pagination")
    y = x[1].findAll("a", class_="arrow")

    next_page = 1
    if page_no == 1 and len(y) == 1:
        next_page = next_page_no(y[0]["href"])
    if len(y) == 2:
        next_page = next_page_no(y[1]["href"])

    return next_page

def main():
    conn = None;
    try:
        # read connection parameter
        params = config()

        # connect to the PostgreSQL server
        conn = psycopg2.connect(**params)

        # create a cursor
        cur = conn.cursor()

        cur.execute("DROP TABLE IF EXISTS contest;")
        cur.execute("CREATE TABLE contest(\
                        username VARCHAR(50),\
                        rank VARCHAR(25),\
                        problem_ID VARCHAR(1)\
                    );")

        page_no = 1

        url = get_url(page_no)
        html_text = requests.get(url).text
        soup = BeautifulSoup(html_text, "lxml")

        while next_page(soup, page_no) > page_no:

            table_row = soup.findAll("tr")

            for row in table_row[3:53]:
                verdict = row.find("td", class_="status-cell status-small status-verdict-cell").text.strip()

                if verdict == "Accepted":
                    username, rank = get_username_rank(row)

                    problem_ID = row.find("td", class_="status-small") \
                                    .find_next_sibling("td") \
                                    .find_next_sibling("td").text.strip()[0]

                    cur.execute(f"INSERT INTO contest VALUES ('{username}', '{rank}', '{problem_ID}');")

            conn.commit() # commit changes

            if page_no % 2 == 0:
                print_output(cur)

            page_no += 1

            url = get_url(page_no)
            html_text = requests.get(url).text
            soup = BeautifulSoup(html_text, "lxml")

            # sleep(4) # sleep 4 seconds
        else:
            if page_no % 2 != 1:
                print_output(cur)

        cur.execute("DROP TABLE IF EXISTS contest;")
        cur.close() # close the cursor
    except (Exception, psycopg2.DatabaseError) as error:
        print(error)
    finally:
        if conn is not None:
            conn.close()


if __name__ == "__main__":
    main()

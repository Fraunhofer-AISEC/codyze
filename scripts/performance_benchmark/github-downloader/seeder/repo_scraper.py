import os
from collections import defaultdict

import redis
import requests
from dotenv import load_dotenv


def main():
    print("Connecting to redis")
    red = redis.Redis(host="redis")

    page = 1
    repos = defaultdict(set)
    if not os.path.exists("done-repos"):
        open("done-repos", "w+").close()

    with open("done-repos") as f:
        known_repos = set(f.read().splitlines())

    total_count = None
    while True:
        query_map = {
            "q": query,
            "page": page,
            "per_page": min(limit, 100)
        }
        headers = {
            "Authorization": f"token {token}"
        }
        print(f"Github token: {token}")
        r = requests.get("https://api.github.com/search/code", query_map,
                         headers=headers)
        if r.status_code != 200:
            print(f"Error {r.status_code}: {r.content.decode()}")
            break
        r_json = r.json()
        if total_count is None:
            total_count = r_json["total_count"]
            print(f"Total found files: {total_count}")
        for i in r_json["items"]:
            repo_name = i["repository"]["full_name"]
            repo_url = f"https://github.com/{repo_name}.git"
            file_url = i["html_url"].replace("/blob/", "/", 1).replace(
                "//github.com/", "//raw.githubusercontent.com/", 1)
            if repo_url not in known_repos:
                if repo_name not in repos:
                    red.sadd("github-repos", f"{prefix}::{repo_url}")
                    print(f"Repo {len(repos)}: {repo_name}")
                repos[repo_name].add(i["html_url"])
                red.sadd("github-files", f'{prefix}::{file_url}')
        if page * 100 >= r_json["total_count"] or len(repos) >= limit:
            break
        page += 1
    print(
        f"Found {sum([len(r) for r in repos.values()])} files in {len(repos)} repos")


if __name__ == '__main__':
    load_dotenv()

    token = os.environ["GITHUB_TOKEN"]
    limit = int(os.environ.get("REPO_LIMIT", "100"))
    query = os.environ["GITHUB_QUERY"]
    prefix = os.environ.get("GITHUB_URL_PREFIX", "")
    main()

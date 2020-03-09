from github import Github
import os
from urllib import request
import json
import time
from datetime import datetime
import redis


CELERY_BROKER_URL = os.environ.get('CELERY_BROKER_URL', 'redis')

print("Connecting to redis %s" % CELERY_BROKER_URL)
red = redis.Redis(host=CELERY_BROKER_URL)

def wait(seconds):
    time.sleep(seconds / 1000)


def api_wait():
    url = 'https://api.github.com/rate_limit'
    response = request.urlopen(url).read()
    data = json.loads(response.decode())
    print("remaining core  : %s" % data['resources']['core']['remaining'])
    print("remaining search: %s" % data['resources']['search']['remaining'])
    if data['resources']['core']['remaining'] <= 10:  # extra margin of safety
        reset_time = data['resources']['core']['reset']
        wait(reset_time - time.time() + 10)
    elif data['resources']['search']['remaining'] <= 110:
        reset_time = data['resources']['search']['reset']
        wait(reset_time - time.time() + 10)


def main():
    # Create github session using an access token
    g = Github(os.environ["GITHUB_TOKEN"])

    # Check our rate limit so we can set timers accordingly:
    print(g.get_rate_limit().core)

    # Search Github
    print("Searching GitHub ...")
    content_files = g.search_code(query=os.environ["GITHUB_QUERY"])

    print("Limit reset time: %s" % datetime.fromtimestamp(content_files._PaginatedList__requester.rate_limiting_resettime).strftime('%Y-%m-%dT%H:%M:%S'))
    print("Total results   : %d" % content_files.totalCount)

    # Submit download_url of search results to celery queue
    for i, f in enumerate(content_files):
        try:
            print(f.path)
            red.sadd('github-tasks', f.download_url)
            if i % 100 == 0:
                api_wait()
        except Exception as e:
            print(e)
        wait(1000)  # We will hit undocumented abuse limit if querying too fast.


if __name__ == '__main__':
    main()

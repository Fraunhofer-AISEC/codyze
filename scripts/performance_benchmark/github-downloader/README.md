# GitHub Mass Downloader

Use this project to search on GitHub and download all search results.

## Setting up

Copy `env.template` to `.env` and configure your GitHub token and the search query.

## Starting

Just run `docker-compose up`

Once the docker stack is running, you may monitor the progress of the download tasks at <http://localhost:5555>

## Scaling up

By default, this stack uses a single "seeder" that runs the search query and four "downloaders" which download the actual files. The "seeder" is extremely sensitive to GitHub's rate limiting API. If you are running complex search queries, you might even earlier run into a non-documented "abuse" limit by GitHub. Thus, scaling up the seeder will only work if you use a different GitHub token.

### Scaling up the downloaders

Simply run `docker-compose up --scale downloader=25`

### Scaling up the seeder(s)

As stated above, simply increasing the number of seeders will not work due to GitHub's abuse limit. Instead, create `n` new GitHub accounts/tokens, create `n` new instances of this project and connect them all to the same message queue, by runnign a central redis server and adding the following environment variables to your `.env` file:

```
CELERY_BROKER_URL=redis://my-massive-redis-cluster:6379
CELERY_RESULT_BACKEND=redis://my-massive-redis-cluster:6379
```
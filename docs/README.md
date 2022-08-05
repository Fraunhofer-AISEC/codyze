# Codyze Documentation

The documentation for Codyze is built with [Material for MkDocs](https://squidfunk.github.io/) and hosted with GitHub Pages.

To edit it, simply use one of the two Dockerfiles in this directory. They contain all the necessary plugins used in this documentation already.  
For this, you must first build a Docker image:
    `docker build docs -f docs/arm64_armv7.Dockerfile -t mkdocs`  
Afterwards, you can start a development server:
    `docker run --rm -it -p 8000:8000 -v ${PWD}:/docs mkdocs`  

Please note, that the `git-revision-date-localized` plugin does not work with git worktrees.
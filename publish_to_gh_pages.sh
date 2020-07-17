#!/bin/sh

#if [ "`git status -s`" ]
#then
#    echo "The working directory is dirty. Please commit any pending changes."
#    exit 1;
#fi

echo "Saving CNAME file"
mv public/CNAME ./CNAME

echo "Deleting old publication"
rm -rf public
mkdir public
git worktree prune
rm -rf .git/worktrees/public/

echo "Checking out gh-pages branch into public"
git worktree add -B gh-pages public github/gh-pages

echo "Removing existing files"
rm -rf public/*

echo "Generating site"
hugo -s ./docs/ -d ../public

echo "Recreating CNAME file"
mv ./CNAME public/CNAME

echo "Updating gh-pages branch"
cd public && git add --all && git commit -m "Publishing to gh-pages"

echo "Now do 'git push --all' to publish Github page"
#git push --all

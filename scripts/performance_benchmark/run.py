#!/usr/bin/env python

# ##########################################################################################
#
# Gets the latest version of the analyzer tool from bitbucket and tests codyze against it:
# https://***REMOVED***/sas/teaching/2019-cpg-bouncycastle/tree/analysisserver
#
# (C) Fraunhofer AISEC 2020, Samuel Hopstock
#
# ##########################################################################################
import os
import shutil
import subprocess
import sys

sys.path.append(os.path.dirname(os.path.realpath(__file__)))

from result_parser import main as analyze


def printer(text):
    print(f"\033[36;1m[x] {text} \033[0m")


def system(cmd):
    if os.system(cmd):
        printer(f"{cmd} did not exit cleanly!")
        exit(1)


def main():
    if not os.path.exists(".env"):
        printer(".env file not set up yet. Creating a new one")
        print("You will now be guided through the setup process")
        with open(".env", "w") as f:
            f.write(
              "REDIS_HOST=redis\n"
              "GITHUB_QUERY_JAVA='language:Java \"import javax.crypto\"'\n"
              "GITHUB_QUERY_CXX='extension:cpp \"include <botan/botan.h>\"'\n"
              "ANALYSIS_MODE=files\n"
              "MARK_PATH=/mark\n"
              "OUTPUT_PATH=/logs/results.txt\n"
            )
            print("Please provide a personal GitHub token.")
            print("(Can be obtained at https://github.com/settings/tokens)")
            gh_token = input("GitHub token: ")
            f.write(f"GITHUB_TOKEN={gh_token}\n")

            print("How many repositories per language should be analyzed?")
            repos = input("Number of repositories (default: 500): ")
            f.write(f"REPO_LIMIT={repos or 500}\n")

            print("After how many seconds should we timeout the analysis?")
            timeout = input("Timeout (default: 60): ")
            f.write(f"ANALYSIS_TIMEOUT={timeout or 60}")
        print()

    printer("Publishing codyze to local maven repo")
    system("cd ../..; ./gradlew publishToMavenLocal")

    if os.path.exists("analyzer"):
        printer("Removing old analyzer version")
        try:
            shutil.rmtree("analyzer")
        except PermissionError:
            system("sudo rm -rf analyzer")

    printer("Getting the analyzer from BitBucket")
    system(
      "git clone git@***REMOVED***:sas/teaching/2019-cpg-bouncycastle.git analyzer")
    system("cd analyzer; git checkout analysisserver")

    printer("Adding .env file and python scraper")
    shutil.rmtree("analyzer/src/main/python/github-downloader")
    shutil.copytree("github-downloader",
                    "analyzer/src/main/python/github-downloader")
    shutil.copy(".env", "analyzer/.env")

    printer("Updating mark mount path")
    os.chdir("analyzer")
    docker_compose_yml = ""
    with open("docker-compose.yml", "r") as f:
        for line in f:
            if "- ./src/main/resources/mark:/mark" in line:
                docker_compose_yml += line.replace("src/main/resources",
                                                   "../../../src/dist")
            else:
                docker_compose_yml += line
    with open("docker-compose.yml", "w") as f:
        f.write(docker_compose_yml)

    printer("Getting codyze from local maven repo")
    shutil.copytree(
      os.path.expanduser("~/.m2/repository/de/fraunhofer/aisec/codyze"),
      "analyzer/codyze_m2")

    printer("Building docker images")
    system("docker-compose build")

    printer("Spinning up containers")
    containers = subprocess.getoutput("docker-compose up -d").splitlines()
    analyzer = None
    for c in containers:
        if "analyzer_1" in c:
            analyzer = c.split(" ")[1]
            printer(f"Analyzer container: {analyzer}")
            break
    if analyzer is None:
        printer("Analyzer container not found! Shutting down containers")
        system("docker compose down")
        exit(1)

    printer("Waiting for analyzer to stop.")
    print("In the meantime, JVM load can be monitored by connecting to localhost:9010 with visualvm")
    system(f"docker wait {analyzer}")
    printer("Analyzer done, shutting down redis")
    system("docker-compose down")

    printer("Analyzing the report")
    try:
        analyze("logs/results.txt")
    except KeyboardInterrupt:
        pass


if __name__ == '__main__':
    main()

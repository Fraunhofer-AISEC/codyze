# Password-Manager
Password Manager implemented in Java using BouncyCastle for CS:4640 at the University of Iowa.
Collaborators:  Ryan Wedoff & Liam Crawford

How to Run from Terminal:
    This password manager runs on Java. Below is the instructions to compile and run this file.

    NOTE: This program runs with Java8**

Compile:
    javac -cp .\bcprov-jdk15on-156.jar Main.java

Run:
    Windows:  java -classpath ".;bcprov-jdk15on-156.jar" Main
    Linux:  java -classpath ".:bcprov-jdk15on-156.jar" Main


As a new user, you will be asked to create a new master password.  You will then create a new master password and it the program will terminate.
Run the program again, and you will be asked to enter your master password.  To re-register, delete passwd_file and master_pass


**Linux Java8 install with Unlimited JCE Policy:
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-set-default
    sudo apt install oracle-java8-unlimited-jce-policy

NOTE:  If you are getting an error: Invalid Key Size, you need to update your JRE security file.
    http://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
    Windows:
        "    Most likely you don't have the unlimited strength file installed now.
        You may need to download this file:

        Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
        Extract the jar files from the zip and save them in ${java.home}/jre/lib/security/."

    Linux:
        sudo add-apt-repository ppa:webupd8team/java
        sudo apt update
        sudo apt install oracle-java8-unlimited-jce-policy


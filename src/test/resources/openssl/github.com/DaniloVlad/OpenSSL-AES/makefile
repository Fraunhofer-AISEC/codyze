# the compiler
CC = gcc

# compiler flags
CFLAGS  = -g -Wall -lcrypto

main: main.c aes.c
	$(CC) main.c $(CFLAGS) aes.c -o main

clean:
	$(RM) main

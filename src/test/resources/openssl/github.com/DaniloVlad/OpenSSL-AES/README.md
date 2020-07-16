# OpenSSL-AES

An example of using OpenSSL EVP Interface for Advanced Encryption Standard (AES) in cipher block chaining mode (CBC) with 256 bit keys.
For more information visit the [OpenSSL docs](https://www.openssl.org/docs/manmaster/)

## Usage
Compile the code with:
```
root@server:~$ make
gcc main.c -g -Wall -lcrypto aes.c -o main
```

## Reason
I saw loads of questions on stackoverflow on how to implement a simple aes256 example. So here it is! AES-256 is just a subset of the Rijndael block ciphers.  Block ciphers operate on fixed sized matrices called "blocks". For AES these blocks are 4x4 matrices and each element is 1 byte (Hence 16 byte "block size").  Any message not a multiple of the block size will be extended to fill the space.  This is the default behavoir for the EVP_ENCRYPTFINAL_ex functions.
ie: 12 chars becomes 16 chars, 22 chars becomes 32 chars.  
```C
int enc_length = *(plaintext -> length) + (AES_BLOCK_SIZE - *(plaintext -> length) % AES_BLOCK_SIZE);
```

## Sample Output
```
root@server:~$ make
gcc main.c -g -Wall -lcrypto aes.c -o main
root@server:~$ ./main
Enter a message up to 1024 chars: 
Hello World! My name is Danilo and I love you!
Key:

9A 24 EB 27 
BB FE 4B 78 
66 10 0A 26 
9F 41 A8 F1 
AC 00 F0 1C 
40 4E BB 2C 
B9 A7 18 4E 
2B 18 E4 C7 

IV:

2F 2F 66 EC 
F0 EE B0 FB 
1E 80 77 06 
94 FB A2 BB 
35 82 A9 C0 
E5 3F 59 19 
B5 F5 EE ED 
E4 A6 16 A8 

User Message:

48 65 6C 6C 
6F 20 57 6F 
72 6C 64 21 
20 4D 79 20 
6E 61 6D 65 
20 69 73 20 
44 61 6E 69 
6C 6F 20 61 
6E 64 20 49 
20 6C 6F 76 
65 20 79 6F 
75 21 0A 

Hello World! My name is Danilo and I love you!

Sending message to be encrypted...
Encrypted Message:

EF 6C 0F E6 
13 1C E0 CC 
E7 5B 46 FE 
A5 20 F4 F9 
C2 06 61 4C 
A6 43 81 3F 
CE 81 01 C1 
8C 50 60 DC 
E3 B2 89 CF 
46 A7 C8 FE 
03 4F DD E1 
11 C2 1C 58 

Sending message to be decrypted...
Decrypted Message:

48 65 6C 6C 
6F 20 57 6F 
72 6C 64 21 
20 4D 79 20 
6E 61 6D 65 
20 69 73 20 
44 61 6E 69 
6C 6F 20 61 
6E 64 20 49 
20 6C 6F 76 
65 20 79 6F 
75 21 0A 

Hello World! My name is Danilo and I love you!

```
#ifndef AES_H_
#define AES_H_

#include <stdlib.h>
#include <string.h>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/rand.h>
#include <openssl/err.h>

#define AES_BLOCK_SIZE 16
#define AES_KEY_SIZE 32

typedef struct _AES_DATA 
{
    unsigned char *key;
    unsigned char *iv;
} AES_DATA;

typedef struct Message_Struct
{
    unsigned char *body;
    int *length;
    AES_DATA *aes_settings;
    
} Message;

Message *message_init(int);

int aes256_init(Message *);

Message *aes256_encrypt(Message *);

Message *aes256_decrypt(Message *);

void aes_cleanup(AES_DATA *);
void message_cleanup(Message *);



#endif
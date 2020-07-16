#include "includes/aes.h"

Message * message_init(int length) {
    Message *ret = malloc(sizeof(Message));
    ret -> body = malloc(length);
    ret -> length = malloc(sizeof(int));
    *ret -> length = length;
    //used string terminator to allow string methods to work
    memset(ret -> body, '\0', length);
    //initialize aes_data
    aes256_init(ret);
    return ret;
}

int aes256_init(Message * input) {
    AES_DATA *aes_info = malloc(sizeof(AES_DATA));
    aes_info -> key = malloc(sizeof(char) * AES_KEY_SIZE);
    aes_info -> iv = malloc(sizeof(char) * AES_KEY_SIZE);
    //point to new data
    input -> aes_settings = aes_info;
    //set to zero
    memset(input -> aes_settings -> key, 0, AES_KEY_SIZE);
    memset(input -> aes_settings -> iv, 0, AES_KEY_SIZE);
    //get rand bytes
    if(!RAND_bytes(input -> aes_settings -> key, AES_KEY_SIZE) || !RAND_bytes(input -> aes_settings -> iv, AES_KEY_SIZE)) {
        printf("Error: couldn't generate key or iv!");
        return 1;
    }
    return 0;
}

Message *aes256_encrypt(Message * plaintext) {
    EVP_CIPHER_CTX *enc_ctx;
    Message * encrypted_message;
    int enc_length = *(plaintext -> length) + (AES_BLOCK_SIZE - *(plaintext -> length) % AES_BLOCK_SIZE);

    encrypted_message = message_init(enc_length);
    //set up encryption context
    enc_ctx = EVP_CIPHER_CTX_new();
    EVP_EncryptInit(enc_ctx, EVP_aes_256_cbc(), plaintext -> aes_settings -> key, plaintext -> aes_settings -> iv);
    //encrypt all the bytes up to but not including the last block
    if(!EVP_EncryptUpdate(enc_ctx, encrypted_message -> body, &enc_length, plaintext -> body, *plaintext -> length)) {
        EVP_CIPHER_CTX_cleanup(enc_ctx);
        printf("EVP Error: couldn't update encryption with plain text!\n");
        return NULL;
    }
    //update length with the amount of bytes written
    *(encrypted_message -> length) = enc_length;
    //EncryptFinal will cipher the last block + Padding
    if(!EVP_EncryptFinal_ex(enc_ctx, enc_length + encrypted_message -> body, &enc_length)) {
        EVP_CIPHER_CTX_cleanup(enc_ctx);
        printf("EVP Error: couldn't finalize encryption!\n");
        return NULL;
    }
    //add padding to length
    *(encrypted_message -> length) += enc_length;
    //no errors, copy over key & iv rather than pointing to the plaintext msg
    memcpy(encrypted_message -> aes_settings -> key, plaintext -> aes_settings -> key, AES_KEY_SIZE);
    memcpy(encrypted_message -> aes_settings -> iv, plaintext -> aes_settings -> iv, AES_KEY_SIZE);
    //Free context and return encrypted message
    EVP_CIPHER_CTX_cleanup(enc_ctx);
    return encrypted_message;
}

Message *aes256_decrypt(Message *encrypted_message) {
    EVP_CIPHER_CTX *dec_ctx;
    int dec_length = 0;
    Message * decrypted_message;
    //initialize return message and cipher context
    decrypted_message = message_init(*encrypted_message -> length);
    dec_ctx = EVP_CIPHER_CTX_new();
    EVP_DecryptInit(dec_ctx, EVP_aes_256_cbc(), encrypted_message -> aes_settings -> key, encrypted_message -> aes_settings -> iv);
    //same as above
    if(!EVP_DecryptUpdate(dec_ctx, decrypted_message -> body, &dec_length, encrypted_message -> body, *encrypted_message -> length)) {
        EVP_CIPHER_CTX_cleanup(dec_ctx);
        printf("EVP Error: couldn't update decrypt with text!\n");
        return NULL;
    }  
    *(decrypted_message -> length) = dec_length;
    if(!EVP_DecryptFinal_ex(dec_ctx, *decrypted_message->length + decrypted_message -> body, &dec_length)) {
        EVP_CIPHER_CTX_cleanup(dec_ctx);
        printf("EVP Error: couldn't finalize decryption!\n");
        return NULL;
    }
    //auto handle padding
    *(decrypted_message -> length) += dec_length;
    //Terminate string for easier use.
    *(decrypted_message -> body + *decrypted_message -> length) = '\0';
    //no errors, copy over key & iv rather than pointing to the encrypted msg
    memcpy(decrypted_message -> aes_settings -> key, encrypted_message -> aes_settings -> key, AES_KEY_SIZE);
    memcpy(decrypted_message -> aes_settings -> iv, encrypted_message -> aes_settings -> iv, AES_KEY_SIZE);
    //free context and return decrypted message
    EVP_CIPHER_CTX_cleanup(dec_ctx);
    return decrypted_message;
}

void aes_cleanup(AES_DATA *aes_data) {
    free(aes_data -> iv);
    free(aes_data -> key);
    free(aes_data);
}

void message_cleanup(Message *message) {
    //free message struct
    aes_cleanup(message -> aes_settings);
    free(message -> length);
    free(message -> body);
    free(message);
}


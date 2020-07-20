#include "includes/aes.h"

Message *aes256_encrypt(Message * plaintext) {
    EVP_CIPHER_CTX *enc_ctx;
    Message * encrypted_message;
    int enc_length = 42;

    encrypted_message = message_init(enc_length);

    //set up encryption context
    enc_ctx = EVP_CIPHER_CTX_new();

    EVP_EncryptInit(enc_ctx, EVP_aes_256_cbc(), plaintext -> aes_settings -> key, plaintext -> aes_settings -> iv);

    //encrypt all the bytes up to but not including the last block
    EVP_EncryptUpdate(enc_ctx, encrypted_message -> body, &enc_length, plaintext -> body, *plaintext -> length);

    //EncryptFinal will cipher the last block + Padding
    EVP_EncryptFinal_ex(enc_ctx, enc_length + encrypted_message -> body, &enc_length);

    //add padding to length
    *(encrypted_message -> length) += enc_length;

    //Free context and return encrypted message
    EVP_CIPHER_CTX_cleanup(enc_ctx);

    return encrypted_message;
}

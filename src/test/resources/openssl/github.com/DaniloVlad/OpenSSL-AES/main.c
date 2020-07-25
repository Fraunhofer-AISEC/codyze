#include <stdio.h>
#include "includes/aes.h"

void hex_print(unsigned char *in, size_t len) {
    for(int i = 0; i < len; i++) {
        if(i % 4 == 0)
            printf("\n");
        printf("%02X ", *(in + i));
    }
    printf("\n\n");
}

int main() {
    // Initialize openSSL
    ERR_load_crypto_strings();
    OpenSSL_add_all_algorithms();
    OPENSSL_config(NULL);

    
    //input buffer, the AES alg (Rijndael) works on blocks of 16 bytes in a 4x4
    //If a string is 12 chars (bytes) it gets padded to 16.
    char input[1024] = {0};
   //create & init message pointer 
    Message *message, *enc_msg, *dec_msg;

    //get message to be encrypted
    printf("Enter a message up to 1024 chars: \n");
    fgets(input, 1024, stdin);

    message = message_init(strlen(input));
    strcpy((char *) message -> body, input);

    if(aes256_init(message)) {
        puts("Error: Couldn't initialize message with aes data!");
        return 1;
    }

    puts("Key:");
    hex_print(message -> aes_settings -> key, AES_KEY_SIZE);

    puts("IV:");
    hex_print(message -> aes_settings -> iv, AES_KEY_SIZE);

    puts("User Message:");
    hex_print(message -> body, *message -> length);
    puts((char *) message -> body);
    puts("Sending message to be encrypted...");
    enc_msg = aes256_encrypt(message);

    puts("Encrypted Message:");
    hex_print(enc_msg -> body, *enc_msg -> length);

    puts("Sending message to be decrypted...");
    dec_msg = aes256_decrypt(enc_msg);

    puts("Decrypted Message:");
    hex_print(dec_msg -> body, *dec_msg -> length);
    puts((char *) dec_msg -> body);
    //destroy messages
    message_cleanup(message);
    message_cleanup(enc_msg);
    message_cleanup(dec_msg);
    //clean up ssl;
    EVP_cleanup(); 
    CRYPTO_cleanup_all_ex_data(); //Stop data leaks
    ERR_free_strings();

    return 0;
}

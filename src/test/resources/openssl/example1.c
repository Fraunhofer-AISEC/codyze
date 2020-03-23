// compile with gcc openssl_aes.c -lcrypto

#include <stdio.h>
#include <string.h>
#include <openssl/evp.h>
#include <openssl/err.h>

unsigned char key[16] = "0123456789ABCDEF";
unsigned char iv[8] = {1,2,3,4,5,6,7,8};

int main(int argc, char *argv[])
{

    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();
    unsigned char* input = argv[1];
    const EVP_CIPHER *cipher = EVP_bf_cbc();

    printf("strlen: %ld\n", strlen(input));

    // ENCRYPT
    unsigned char *encrypted = malloc(256 * sizeof(unsigned char));
    int outlen1, outlen2;

    EVP_CIPHER_CTX_init( ctx );

    EVP_CipherInit_ex(ctx, cipher, NULL, key, iv, 1); // 1 == ENCRYPT
    EVP_CipherUpdate(ctx, encrypted, &outlen1, input, strlen(input));
    if( EVP_CipherFinal_ex(ctx, encrypted + outlen1, &outlen2) != 1 ) {
        printf("error: ");
        ERR_print_errors_fp( stderr );
    }

    printf("outlen encrypt: %d + %d\n", outlen1, outlen2);

    // DECRYPT
    unsigned char *decrypted = malloc((outlen1 + outlen2 + 1) * sizeof(unsigned char)); // +1 for \0

    EVP_CIPHER_CTX_init( ctx );

    EVP_CipherInit_ex( ctx, EVP_bf_cbc(), NULL, key, iv , 0); // 0 == DECRYPT
    EVP_CipherUpdate( ctx, decrypted, &outlen1, encrypted, outlen1 + outlen2 );
    if( EVP_CipherFinal_ex( ctx, decrypted + outlen1, &outlen2) != 1 ) {
        printf("error: ");
        ERR_print_errors_fp( stderr );
    }

    printf("outlen decrypt: %d + %d\n", outlen1, outlen2);

    printf("text length: %d\n", outlen1 + outlen2);
    printf("text: %s", decrypted);

    EVP_CIPHER_CTX_free(ctx);
    free(encrypted);

    return 0;
}

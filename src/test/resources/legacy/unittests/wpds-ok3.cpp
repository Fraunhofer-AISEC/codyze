/* Example for correct typestate over non-objects */

// allowed:
// cm.create(), cm.init(), (cm.start(), cm.process()*, cm.finish())+, cm.reset()?


int main()
{
    EVP_CIPHER_CTX *ctx = EVP_CIPHER_CTX_new();
    EVP_EncryptInit(ctx, EVP_aes_256_gcm(), key, iv);

    const size_t block_size = 128;
    unsigned char inbuf[block_size];
    unsigned char outbuf[block_size + EVP_MAX_BLOCK_LENGTH];
    int inlen, outlen;
    for (;;)
    {
            inlen = fread(inbuf, 1, block_size, stdin);
            if (inlen <= 0)
                    break;

            EVP_EncryptUpdate(ctx, outbuf, &outlen, inbuf, inlen);
            fwrite(outbuf, 1, outlen, stdout);
    }
    EVP_EncryptFinal_ex(ctx, outbuf, &outlen);
    fwrite(outbuf, 1, outlen, stdout);
    return 0;
}
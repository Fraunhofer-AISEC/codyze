// copied from symm_block_cipher, added with a few forbidden calls

// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

void do_crypt()
{

    char* cipher;
    SymmetricKey key;
    InitializationVector iv;
    Cipher_Dir direction;
    uint8_t* buf;

    Botan p(get_cipher_mode(cipher, direction));
    Botan p2(get_cipher_mode(cipher, direction));
    p->set_key(key);
    p->start(iv.bits_of());
    p->finish(buf);

    p2->start(iv.bits_of());
    p2->finish(buf);

}
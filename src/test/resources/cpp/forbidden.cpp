// copied from symm_block_cipher, added with a few forbidden calls

// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

void do_crypt()
{

    uint8_t nonce;
    size_t nonce_length;
    Botan::InitializationVector iv;
    Botan b;

/*
              Botan::get_cipher_mode(_, _);
    decl myValue = Botan::AutoSeededRNG::random_vec(_);

              Botan::set_key(_);
    forbidden Botan::set_key(_, _);

              Botan::start(iv);
    forbidden Botan::start();
    forbidden Botan::start(_);
    forbidden Botan::start(nonce, _);

    forbidden Botan::start_msg(*);
*/

// the following are directly allowed
    b.get_cipher_mode(nonce, nonce_length);
    b.set_key(iv);
    b.set_key(nonce);

// the following are forbidden

    b.start();
    b.start(nonce, b);

    b.start_msg(nonce); // not recognized yet (* missing)
    b.start_msg(nonce, iv, b); // not recognized yet (* missing)

    b.set_key(nonce, iv);

// the following are not forbidden. These match a forbidden rule, but also a non-forbidden rule

    b.start(iv);

// the following do not match anything, and are therefore allowed

    b.get_cipher_mode();
    b.get_cipher_mode(iv);


}
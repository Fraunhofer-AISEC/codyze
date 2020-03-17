int main() {
    const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
    const std::vector<uint8_t> test_sig = Botan::hex_decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    Botan::AutoSeeded_RNG rng;
    Botan::RSA_PublicKey pub_key(rng, 4096);
    Botan::PK_Verifier sig_verifier(Botan::PK_Verifier(pub_key, "EMSA4(SHA-256)"));
    sig_verifier.verify_message(data, test_sig);
}
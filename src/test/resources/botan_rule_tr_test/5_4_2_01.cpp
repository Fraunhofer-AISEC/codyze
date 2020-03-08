int main() {
    const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
    const std::vector<uint8_t> test_sig = Botan::hex_decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    Botan::AutoSeeded_RNG rng;
    Botan::DL_Group dl_group("dsa/botan/3072");
    Botan::DSA_PublicKey::DSA_PublicKey(rng, dl_group);
    Botan::PK_Verifier sig_verifier(Botan::PK_Verifier(pub_key, "EMSA4(SHA-256)"));
    sig_verifier.verify_message(data, test_sig);
}
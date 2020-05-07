#include <botan/auto_rng.h>
#include <botan/ec_group.h>
#include <botan/ecdsa.h>
#include <botan/hex.h>
#include <botan/pubkey.h>


int main() {
    const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
    const std::vector<uint8_t> test_sig = Botan::hex_decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    Botan::AutoSeeded_RNG rng;
    Botan::EC_Group ec_group("brainpoolP256r1");
    Botan::ECDSA_PrivateKey ecdsa_key(rng, ec_group);
    Botan::PK_Verifier sig_verifier(ecdsa_key, "EMSA4(SHA-256)");
    sig_verifier.verify_message(data, test_sig);
}

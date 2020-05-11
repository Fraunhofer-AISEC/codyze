#include <iostream>

#include <botan/auto_rng.h>
#include <botan/dh.h>
#include <botan/hex.h>
#include <botan/rng.h>


int main() {
    const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
    const std::vector<uint8_t> test_sig = Botan::hex_decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    Botan::AutoSeeded_RNG rng;
    Botan::DL_Group dl_group("dsa/botan/3072");
    Botan::DH_PrivateKey(rng, dl_group);
}

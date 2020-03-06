#include <botan/rng.h>
#include <botan/auto_rng.h>
#include <botan/cipher_mode.h>
#include <botan/hex.h>
#include <iostream>

int main() {
   Botan::AutoSeeded_RNG rng;

   const std::string plaintext("Your great-grandfather gave this watch to your granddad for good luck. Unfortunately, Dane's luck wasn't as good as his old man's.");
   const std::vector<uint8_t> key = Botan::hex_decode("2B7E151628AED2A6ABF7158809CF4F3C");

   Botan::Cipher_Mode* enc = Botan::get_cipher_mode("AES-128/CBC/PKCS7", Botan::ENCRYPTION);
   enc->set_key(key);

   Botan::secure_vector<uint8_t> pt(plaintext.data(), plaintext.data()+plaintext.length());

   //generate fresh nonce (IV)
   enc->start(rng.random_vec(enc->default_nonce_length()));
   enc->finish(pt);

   std::cout << enc->name() << " with iv " << Botan::hex_encode(iv) << " " << Botan::hex_encode(pt) << "\n";
   return 0;
}
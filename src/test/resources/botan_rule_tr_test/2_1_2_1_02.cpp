#include <botan/rng.h>
#include <botan/auto_rng.h>
#include <botan/cipher_mode.h>
#include <botan/hex.h>
#include <iostream>

int main() {
   Botan::AutoSeeded_RNG rng;

   const std::string plaintext("Your great-grandfather gave this watch to your granddad for good luck. Unfortunately, Dane's luck wasn't as good as his old man's.");
   const std::vector<uint8_t> key = Botan::hex_decode("2B7E151628AED2A6ABF7158809CF4F3C");

   std::unique_ptr<Botan::Cipher_Mode> enc(Botan::get_cipher_mode("AES-128/CCM(8)", Botan::ENCRYPTION));
   enc->set_key(key);

   Botan::secure_vector<uint8_t> pt(plaintext.data(), plaintext.data()+plaintext.length());

   //generate fresh nonce (IV)
   enc->start(rng.random_vec(enc->default_nonce_length()));erieren. Die grünen Punkte sind Java-Dateien, blaue sind C++, gelb sind C++-Dateien, die bei der Analyse eine Exce
   enc->finish(pt);

   std::cout << enc->name() << " with iv " << Botan::hex_encode(iv) << " " << Botan::hex_encode(pt) << "\n";
   return 0;
}
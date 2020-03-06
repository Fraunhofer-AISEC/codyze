#include <botan/pkcs8.h>
#include <botan/hex.h>
#include <botan/pk_keys.h>
#include <botan/pubkey.h>
#include <botan/auto_rng.h>
#include <botan/rng.h>
#include <iostream>
int main (int argc, char* argv[]) {
  if(argc!=2)
     return 1;
  std::string plaintext("Your great-grandfather gave this watch to your granddad for good luck. Unfortunately, Dane's luck wasn't as good as his old man's.");
  std::vector<uint8_t> pt(plaintext.data(),plaintext.data()+plaintext.length());
  std::unique_ptr<Botan::RandomNumberGenerator> rng(new Botan::AutoSeeded_RNG);

  //load keypair
  std::unique_ptr<Botan::Private_Key> kp(Botan::PKCS8::load_key(argv[1],*rng.get()));

  Botan::EC_Group ec_group("brainpoolP256r1");

  Botan::ECIES_System_Params ecies_params(ec_group, "KDF2(SHA-256)", "AES-256", 32, "HMAC", 16);

  //encrypt with pk
  Botan::ECIES_Encryptor enc(*rng.get(), ecies_params);
  std::vector<uint8_t> ct = enc.encrypt(pt,*rng.get());

  return 0;
 }
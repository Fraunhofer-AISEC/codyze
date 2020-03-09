#include <botan/pkcs8.h>
#include <botan/hex.h>
#include <botan/pk_keys.h>
#include <botan/pubkey.h>
#include <botan/auto_rng.h>
#include <botan/rng.h>
#include <iostream>
#include <botan/ec_group.h>
#include <botan/ecies.h>
int main (int argc, char* argv[]) {
  if(argc!=2)
     return 1;
  std::string plaintext("Your great-grandfather gave this watch to your granddad for good luck. Unfortunately, Dane's luck wasn't as good as his old man's.");
  std::vector<uint8_t> pt(plaintext.data(),plaintext.data()+plaintext.length());
  Botan::AutoSeeded_RNG rng;

  //load keypair
  Botan::Private_Key* kp(Botan::PKCS8::load_key(argv[1], rng));

  Botan::EC_Group ec_group("brainpoolP256r1");

  Botan::ECIES_System_Params ecies_params(ec_group, "KDF2(SHA-256)", "AES-256", 32, "HMAC", 16);

  //encrypt with pk
  Botan::ECIES_Encryptor enc( rng, ecies_params);
  std::vector<uint8_t> ct = enc.encrypt(pt, rng);

  return 0;
 }
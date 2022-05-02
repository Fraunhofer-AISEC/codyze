#include <assert.h>
#include <iostream>

#include <botan/auto_rng.h>
#include <botan/hex.h>
#include <botan/dl_group.h>
#include <botan/dlies.h>
#include <botan/pk_keys.h>
#include <botan/pkcs8.h>
#include <botan/pubkey.h>
#include <botan/rng.h>


int main (int argc, char* argv[]) {
  std::string plaintext("Your great-grandfather gave this watch to your granddad for good luck. Unfortunately, Dane's luck wasn't as good as his old man's.");
  std::vector<uint8_t> pt(plaintext.data(), plaintext.data()+plaintext.length());
  Botan::AutoSeeded_RNG rng;

  Botan::DL_Group dl_group("dsa/botan/3072");

  Botan::DH_PrivateKey own_key(rng, dl_group);
  Botan::DH_PublicKey other_key(dl_group, Botan::BigInt(15));


  assert(own_key.check_key(rng, false));

  Botan::KDF* kdf = Botan::get_kdf("HKDF(HMAC(SHA-256))");


  Botan::MAC mac = Botan::MessageAuthenticationCode::create("HMAC(SHA-256)");

  //encrypt with pk
  Botan::DLIES_Encryptor enc(own_key, rng, kdf, mac);

  std::vector<uint8_t> ct = enc.encrypt(pt, rng);

  return 0;
 }

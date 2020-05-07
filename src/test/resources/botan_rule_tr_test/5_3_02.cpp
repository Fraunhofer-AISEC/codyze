#include <iostream>

#include <botan/auto_rng.h>
#include <botan/hex.h>
#include <botan/mac.h>


int main()
   {
   Botan::AutoSeeded_RNG rng;
   const Botan::SymmetricKey key = Botan::SymmetricKey(rng, 16);
   const std::vector<uint8_t> iv = Botan::hex_decode("FFFFFFFFFFFFFFFFFFFFFFFF");
   const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
   Botan::MessageAuthenticationCode mac = Botan::MessageAuthenticationCode::create("GMAC(AES-256)");
   if(!mac)
      return 1;
   mac->set_key(key);
   mac->start(iv);
   mac->update(data);
   Botan::secure_vector<uint8_t> tag = mac->final();
   std::cout << mac->name() << ": " << Botan::hex_encode(tag) << std::endl;

   //Verify created MAC
   mac->start(iv);
   mac->update(data);
   std::cout << "Verification: " << (mac->verify_mac(tag) ? "success" : "failure");
   return 0;
   }

#include <iostream>

#include <botan/auto_rng.h>
#include <botan/hex.h>
#include <botan/mac.h>



int main()
   {
   Botan::AutoSeeded_RNG rng;
   const std::vector<uint8_t> key = Botan::SymmetricKey(rng, 16);
   size_t iv_len = 12;
   uint8_t iv[iv_len]  = {0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63, 0x63};
   const std::vector<uint8_t> data = Botan::hex_decode("6BC1BEE22E409F96E93D7E117393172A");
   Botan::MessageAuthenticationCode mac = Botan::MessageAuthenticationCode::create("GMAC(AES-256)");
   if(!mac)
      return 1;
   mac->set_key(key);
   mac->start(iv, iv_len);
   mac->update(data);
   Botan::secure_vector<uint8_t> tag = mac->final();
   std::cout << mac->name() << ": " << Botan::hex_encode(tag) << std::endl;
   }

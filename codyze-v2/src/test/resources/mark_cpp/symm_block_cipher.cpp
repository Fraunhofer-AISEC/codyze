#include <iostream>
#include <stdexcept>
#include <cassert>
#include <botan/aead.h>
#include <botan/hex.h>
#include <botan/block_cipher.h>

#define __CIPHER "AES-256/CBC"
#define __KEY_LENGTH 32
#define __IV_LENGTH 16


Botan::secure_vector<uint8_t> do_crypt(const std::string &cipher,
                                       const std::vector<uint8_t> &input,
                                       const Botan::SymmetricKey &key,
                                       const Botan::InitializationVector &iv,
                                       Botan::Cipher_Dir direction)
{
    if(iv.size() == 0)
        throw std::runtime_error("IV must not be empty");

    //std::unique_ptr<Botan::Cipher_Mode> processor(Botan::get_cipher_mode(cipher, direction));
    Botan::Cipher_Mode processor(Botan::get_cipher_mode(cipher, direction));
    Botan::Cipher_Mode processor2(Botan::get_cipher_mode(cipher, direction));
    if(!processor)
        throw std::runtime_error("Cipher algorithm not found");

    // Set key
    processor->set_key(key);

    // Set IV
    processor->start(iv.bits_of());

    Botan::secure_vector<uint8_t> buf(input.begin(), input.end());
    processor->finish(buf);

    processor2->start(iv.bits_of());
    processor2->finish(buf);


    return buf;
}


std::string encrypt(std::string cleartext) {
    const std::string key_hex = "f00dbabef00dbabef00dbabef00dbabef00dbabef00dbabef00dbabef00dbabe";
    const std::string iv_hex  = "deadbeefdeadbeefdeadbeefdeadbeef";
    const Botan::SymmetricKey key(key_hex);
    assert(key.length() == __KEY_LENGTH);
    const Botan::InitializationVector iv(iv_hex);
    assert(iv.length() == __IV_LENGTH);

    const std::vector<uint8_t> input(cleartext.begin(), cleartext.end());
    std::cerr << "Got " << input.size() << " bytes of input data.\n";

    Botan::secure_vector<uint8_t> cipherblob = do_crypt(__CIPHER, input, key, iv, Botan::Cipher_Dir::ENCRYPTION);
    return Botan::hex_encode(cipherblob);
}

std::string decrypt(const std::string& ciphertext) {
    const std::string key_hex = "f00dbabef00dbabef00dbabef00dbabef00dbabef00dbabef00dbabef00dbabe";
    const std::string iv_hex  = "deadbeefdeadbeefdeadbeefdeadbeef";
    const Botan::SymmetricKey key(key_hex);
    assert(key.length() == __KEY_LENGTH);
    const Botan::InitializationVector iv(iv_hex);
    assert(iv.length() == __IV_LENGTH);

    const std::vector<uint8_t> input = Botan::hex_decode(ciphertext);
    std::cerr << "Got " << input.size() << " bytes of ciphertext data.\n";


    Botan::secure_vector<uint8_t> clearblob = do_crypt(__CIPHER, input, key, iv, Botan::Cipher_Dir::DECRYPTION);
    return std::string(clearblob.begin(), clearblob.end());
}


int main() {
    std::string cleartext = "Hello World";
    auto ciphertext = encrypt(cleartext);
    std::cout << "ciphertext: " << ciphertext << std::endl;
    auto cleartext_decrypted = decrypt(ciphertext);
    std::cout << "cleartext_decrypted: " << cleartext_decrypted << std::endl;
    return 0;
}



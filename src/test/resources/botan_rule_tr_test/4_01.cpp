#include <botan/hash.h>
#include <botan/hex.h>
#include <iostream>
int main () {
    std::unique_ptr<Botan::HashFunction> hash1(Botan::HashFunction::create("SHA-1"));
    std::unique_ptr<Botan::HashFunction> hash2(Botan::HashFunction::create("SHA-3"));
    std::unique_ptr<Botan::HashFunction> hash3(Botan::HashFunction::create("SHA3-356"));
    std::vector<uint8_t> buf(2048);
    while(std::cin.good())
    {
    //read STDIN to buffer
    std::cin.read(reinterpret_cast<char *>(buf.data()), buf.size());
    size_t readcount = std::cin.gcount();
    //update hash computations with read data
    hash1->update(buf.data(),readcount);
    hash2->update(buf.data(),readcount);
    hash3->update(buf.data(),readcount);
    }
    std::cout << "SHA-1: " << Botan::hex_encode(hash1->final()) << std::endl;
    std::cout << "Whirlpool: " << Botan::hex_encode(hash2->final()) << std::endl;
    std::cout << "SHA-3: " << Botan::hex_encode(hash3->final()) << std::endl;
    return 0;
}
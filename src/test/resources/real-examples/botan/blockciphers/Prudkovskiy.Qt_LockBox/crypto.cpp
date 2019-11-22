#include "crypto.h"
#include <QDebug>
#include <vector>
#include <iostream>

Crypto::Crypto()
{
     Botan::InitializationVector init;
}

string Crypto::MyShifr(string line, string log, string pas, bool ok)
{
        Botan::SymmetricKey key (log+pas);
        Botan::InitializationVector v ("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"); //initialis_vector must be 16*bb
        if(ok){
            Botan::Pipe pipe(Botan::get_cipher("AES-128/CBC",key,v,Botan::ENCRYPTION),new Botan::Hex_Encoder);
            pipe.process_msg(line);
            std::string str = pipe.read_all_as_string(0);
            return move(str.data());
        }
        else{
            Botan::Pipe pipe1(new Botan::Hex_Decoder,Botan::get_cipher("AES-128/CBC",key,v,Botan::DECRYPTION));
            pipe1.process_msg(line);
            std::string str1 = pipe1.read_all_as_string(0);
            return move(str1.data());
        }
}

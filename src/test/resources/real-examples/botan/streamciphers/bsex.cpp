// FROM https://raw.githubusercontent.com/jlaako/bsex/master/bsex.cpp
/*

    bsex - Botan based stream cipher utility for backup encryption
    Copyright (C) 2018 Jussi Laako

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions, and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions, and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

*/


#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <string>
#include <vector>
#include <memory>
#include <iostream>
#include <fstream>

#include <botan/version.h>
#include <botan/init.h>
#include <botan/types.h>
#include <botan/secmem.h>
#include <botan/rng.h>
#include <botan/auto_rng.h>
#include <botan/pubkey.h>
#include <botan/rsa.h>
#include <botan/ed25519.h>
#include <botan/x509_key.h>
#include <botan/x509_obj.h>
#include <botan/pkcs8.h>
#include <botan/ctr.h>


#define BSEX_SUBDIR             "/.bsex/"
#define BSEX_BASENAME           "own"


static void keygen (const std::string &baseName, const std::string &passphrase)
{
    std::string privRSA(baseName + "_priv_rsa.pem");
    std::string privEd25519(baseName + "_priv_ed25519.pem");

    Botan::AutoSeeded_RNG PRNG;

    Botan::RSA_PrivateKey KeyPairRSA(PRNG, 4096);
    if (!KeyPairRSA.check_key(PRNG, true))
        throw std::domain_error("RSA key check failed");
    // we don't truncate on purpose to avoid accidentally overwriting keys
    std::ofstream PubFileRSA(baseName + "_pub_rsa.pem",
        std::ios_base::out | std::ios_base::binary);
    std::ofstream PrivFileRSA(privRSA,
        std::ios_base::out | std::ios_base::binary);
    chmod(privRSA.c_str(), 0600);
    PubFileRSA << Botan::X509::PEM_encode(KeyPairRSA);
    // unwrap key is encrypted
    PrivFileRSA << Botan::PKCS8::PEM_encode(KeyPairRSA, PRNG, passphrase);

    Botan::Ed25519_PrivateKey KeyPairEd25519(PRNG);
    if (!KeyPairEd25519.check_key(PRNG, true))
        throw std::domain_error("Ed25519 key check failed");
    // we don't truncate on purpose to avoid accidentally overwriting keys
    std::ofstream PubFileEd25519(baseName + "_pub_ed25519.pem",
        std::ios_base::out | std::ios_base::binary);
    std::ofstream PrivFileEd25519(privEd25519,
        std::ios_base::out | std::ios_base::binary);
    chmod(privEd25519.c_str(), 0600);
    PubFileEd25519 << Botan::X509::PEM_encode(KeyPairEd25519);
    PrivFileEd25519 << Botan::PKCS8::PEM_encode(KeyPairEd25519);
}


static void encrypt (const std::string &signBaseName,
    const std::string &wrapBaseName, std::ostream &fileOut)
{
    Botan::AutoSeeded_RNG PRNG;

    std::unique_ptr<Botan::Private_Key> SignKey(
        Botan::PKCS8::load_key(signBaseName + "_priv_ed25519.pem", PRNG));
    std::unique_ptr<Botan::Public_Key> WrapKey(
        Botan::X509::load_key(wrapBaseName + "_pub_rsa.pem"));

    std::unique_ptr<Botan::StreamCipher> SymCipher(
        Botan::CTR_BE::create_or_throw("CTR(AES-256)"));
    Botan::secure_vector<uint8_t> SymKey(
        PRNG.random_vec(SymCipher->maximum_keylength()));
    Botan::secure_vector<uint8_t> SymIV(
        PRNG.random_vec(SymCipher->default_iv_length()));

    fprintf(stderr, "keysize=%lu, ivsize=%lu\n", SymKey.size(), SymIV.size());
    SymCipher->set_key(SymKey);
    SymCipher->set_iv(SymIV.data(), SymIV.size());

    uint32_t u32WrappedSize;
    Botan::PK_Encryptor_EME WrapEncryptor(*WrapKey.get(), PRNG, "EME1(SHA-256)");
    std::vector<uint8_t> WrappedKey(WrapEncryptor.encrypt(SymKey, PRNG));
    std::vector<uint8_t> WrappedIV(WrapEncryptor.encrypt(SymIV, PRNG));
    // wrapped key
    u32WrappedSize = static_cast<uint32_t> (WrappedKey.size());
    fileOut.write(reinterpret_cast<char *> (&u32WrappedSize), sizeof(u32WrappedSize));
    if (!fileOut.good())
        throw std::range_error("failed to write wrapped key size");
    fileOut.write(reinterpret_cast<char *> (WrappedKey.data()), WrappedKey.size());
    if (!fileOut.good())
        throw std::range_error("failed to write wrapped key");
    // wrapped IV
    u32WrappedSize = static_cast<uint32_t> (WrappedIV.size());
    fileOut.write(reinterpret_cast<char *> (&u32WrappedSize), sizeof(u32WrappedSize));
    if (!fileOut.good())
        throw std::range_error("failed to write wrapped iv size");
    fileOut.write(reinterpret_cast<char *> (WrappedIV.data()), WrappedIV.size());
    if (!fileOut.good())
        throw std::range_error("failed to write wrapped iv");

    uint32_t u32SigSize;
    size_t sizeSigPos = fileOut.tellp();
    Botan::PK_Signer Signer(*SignKey.get(), PRNG, "SHA-512");
    u32SigSize = Signer.signature_length();
    std::vector<uint8_t> Signature(u32SigSize);
    fprintf(stderr, "sigpos=%lu, sigsize=%u\n", sizeSigPos, u32SigSize);
    // signature placeholder
    fileOut.write(reinterpret_cast<char *> (&u32SigSize), sizeof(u32SigSize));
    if (!fileOut.good())
        throw std::range_error("failed to write signature size");
    fileOut.write(reinterpret_cast<char *> (Signature.data()), Signature.size());
    if (!fileOut.good())
        throw std::range_error("failed to write signature");

    const size_t sizeBlock = 1048576 * 4;  // 4 MiB
    std::vector<uint8_t> DataBlock(sizeBlock);
    Botan::secure_vector<uint8_t> CipherBlock(sizeBlock);

    while (std::cin.good())
    {
        std::cin.read(reinterpret_cast<char *> (DataBlock.data()), DataBlock.size());
        Signer.update(DataBlock.data(), std::cin.gcount());
        SymCipher->cipher(DataBlock.data(), CipherBlock.data(), std::cin.gcount());
        fileOut.write(reinterpret_cast<char *> (CipherBlock.data()), std::cin.gcount());
        if (!fileOut.good())
            throw std::range_error("failed to write cipher block");
    }

    Signature = Signer.signature(PRNG);
    // signature
    fileOut.seekp(sizeSigPos + sizeof(u32SigSize));
    fileOut.write(reinterpret_cast<char *> (Signature.data()), Signature.size());
    if (!fileOut.good())
        throw std::range_error("failed to write signature");
    fileOut.seekp(0, std::ios_base::end);
}


static void decrypt (const std::string &wrapBaseName,
    const std::string &signBaseName, std::istream &fileIn,
    const std::string &passphrase,
    bool verify = false)
{
    Botan::AutoSeeded_RNG PRNG;

    std::unique_ptr<Botan::Public_Key> SignKey(
        Botan::X509::load_key(signBaseName + "_pub_ed25519.pem"));
    std::unique_ptr<Botan::Private_Key> WrapKey(
        Botan::PKCS8::load_key(wrapBaseName + "_priv_rsa.pem", PRNG, passphrase));

    uint32_t u32WrappedSize = 0;
    uint32_t u32SigSize = 0;
    Botan::PK_Decryptor_EME WrapDecryptor(*WrapKey.get(), PRNG, "EME1(SHA-256)");
    std::vector<uint8_t> WrappedKey;
    std::vector<uint8_t> WrappedIV;
    std::vector<uint8_t> Signature;
    // wrapped key
    fileIn.read(reinterpret_cast<char *> (&u32WrappedSize), sizeof(u32WrappedSize));
    if (!fileIn.good())
        throw std::range_error("failed to read wrapped key size");
    WrappedKey.resize(u32WrappedSize);
    fileIn.read(reinterpret_cast<char *> (WrappedKey.data()), WrappedKey.size());
    if (!fileIn.good())
        throw std::range_error("failed to read wrapped key");
    // wrapped iv
    fileIn.read(reinterpret_cast<char *> (&u32WrappedSize), sizeof(u32WrappedSize));
    if (!fileIn.good())
        throw std::range_error("failed to read wrapped iv size");
    WrappedIV.resize(u32WrappedSize);
    fileIn.read(reinterpret_cast<char *> (WrappedIV.data()), WrappedIV.size());
    if (!fileIn.good())
        throw std::range_error("failed to read wrapped iv");
    // signature
    fileIn.read(reinterpret_cast<char *> (&u32SigSize), sizeof(u32SigSize));
    if (!fileIn.good())
        throw std::range_error("failed to read signature size");
    Signature.resize(u32SigSize);
    fileIn.read(reinterpret_cast<char *> (Signature.data()), Signature.size());
    if (!fileIn.good())
        throw std::range_error("failed to read signature");

    Botan::secure_vector<uint8_t> SymKey(WrapDecryptor.decrypt(WrappedKey));
    Botan::secure_vector<uint8_t> SymIV(WrapDecryptor.decrypt(WrappedIV));
    fprintf(stderr, "keysize=%lu, ivsize=%lu, sigsize=%lu\n",
        SymKey.size(), SymIV.size(), Signature.size());
    std::unique_ptr<Botan::StreamCipher> SymCipher(
        Botan::CTR_BE::create_or_throw("CTR(AES-256)"));
    SymCipher->set_key(SymKey);
    SymCipher->set_iv(SymIV.data(), SymIV.size());

    const size_t sizeBlock = 1048576 * 4;  // 4 MiB
    Botan::secure_vector<uint8_t> CipherBlock(sizeBlock);
    Botan::PK_Verifier Verifier(*SignKey.get(), "SHA-512");

    while (fileIn.good())
    {
        fileIn.read(reinterpret_cast<char *> (CipherBlock.data()), CipherBlock.size());
        SymCipher->cipher1(CipherBlock.data(), fileIn.gcount());
        Verifier.update(CipherBlock.data(), fileIn.gcount());
        if (!verify)
        {
            std::cout.write(reinterpret_cast<char *> (CipherBlock.data()),
                fileIn.gcount());
            if (!std::cout.good())
                throw std::range_error("failed to write cipher block");
        }
    }

    if (Verifier.check_signature(Signature))
        std::cerr << "signature OK" << std::endl;
    else
        throw std::domain_error("signature check failed");
}


static void makesig (const std::string &signBaseName,
    const std::string &fileName)
{
    std::ifstream fileIn(fileName, std::ios_base::in | std::ios_base::binary);

    Botan::AutoSeeded_RNG PRNG;
    std::unique_ptr<Botan::Private_Key> SignKey(
        Botan::PKCS8::load_key(signBaseName + "_priv_ed25519.pem", PRNG));
    Botan::PK_Signer Signer(*SignKey.get(), PRNG, "SHA-512");

    uint32_t u32SigSize;
    u32SigSize = Signer.signature_length();
    std::vector<uint8_t> Signature(u32SigSize);
    fprintf(stderr, "sigsize=%u\n", u32SigSize);

    const size_t sizeBlock = 1048576 * 4;  // 4 MiB
    std::vector<uint8_t> SignBlock(sizeBlock);

    while (fileIn.good())
    {
        fileIn.read(reinterpret_cast<char *> (SignBlock.data()), SignBlock.size());
        Signer.update(SignBlock.data(), fileIn.gcount());
    }

    Signature = Signer.signature(PRNG);
    // signature
    std::cout.write(reinterpret_cast<char *> (&u32SigSize), sizeof(u32SigSize));
    if (!std::cout.good())
        throw std::range_error("failed to write signature size");
    std::cout.write(reinterpret_cast<char *> (Signature.data()), Signature.size());
    if (!std::cout.good())
        throw std::range_error("failed to write signature");
}


static void checksig (const std::string &signBaseName,
    const std::string &fileName)
{
    std::ifstream fileIn(fileName, std::ios_base::in | std::ios_base::binary);

    Botan::AutoSeeded_RNG PRNG;
    std::unique_ptr<Botan::Public_Key> SignKey(
        Botan::X509::load_key(signBaseName + "_pub_ed25519.pem"));
    Botan::PK_Verifier Verifier(*SignKey.get(), "SHA-512");

    uint32_t u32SigSize;
    std::cin.read(reinterpret_cast<char *> (&u32SigSize), sizeof(u32SigSize));
    if (!std::cin.good())
        throw std::range_error("failed to read signature size");
    std::vector<uint8_t> Signature(u32SigSize);
    fprintf(stderr, "sigsize=%u\n", u32SigSize);
    std::cin.read(reinterpret_cast<char *> (Signature.data()), Signature.size());
    if (!std::cin.good())
        throw std::range_error("failed to read signature");

    const size_t sizeBlock = 1048576 * 4;  // 4 MiB
    std::vector<uint8_t> SignBlock(sizeBlock);

    while (fileIn.good())
    {
        fileIn.read(reinterpret_cast<char *> (SignBlock.data()), SignBlock.size());
        Verifier.update(SignBlock.data(), fileIn.gcount());
    }

    if (Verifier.check_signature(Signature))
        std::cerr << "signature OK" << std::endl;
    else
        throw std::domain_error("signature check failed");
}


static void keycheck (const std::string &baseName,
    const std::string &passphrase)
{
    Botan::AutoSeeded_RNG PRNG;

    std::unique_ptr<Botan::Private_Key> SignKey(
        Botan::PKCS8::load_key(baseName + "_priv_ed25519.pem", PRNG));
    std::unique_ptr<Botan::Private_Key> WrapKey(
        Botan::PKCS8::load_key(baseName + "_priv_rsa.pem", PRNG, passphrase));

    if (!dynamic_cast<Botan::Ed25519_PrivateKey *> (SignKey.get())->check_key(PRNG, true))
        throw std::domain_error("Ed25519 key check failed");
    if (!dynamic_cast<Botan::RSA_PrivateKey *> (WrapKey.get())->check_key(PRNG, true))
        throw std::domain_error("RSA key check failed");
}


static void change_passphrase (const std::string &baseName,
    const std::string &oldPhrase, const std::string &newPhrase)
{
    Botan::AutoSeeded_RNG PRNG;

    std::unique_ptr<Botan::Private_Key> WrapKey(
        Botan::PKCS8::load_key(baseName + "_priv_rsa.pem", PRNG, oldPhrase));

    std::ofstream PrivFileRSA(baseName + "_priv_rsa.pem",
        std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
    PrivFileRSA << Botan::PKCS8::PEM_encode(*WrapKey, PRNG, newPhrase);
}


static void print_help (const char *execname)
{
    std::cerr << execname << " command args..." << std::endl;
    std::cerr << "\tkeygen" << std::endl;
    std::cerr << "\tencrypt <recipient> [output filename]" << std::endl;
    std::cerr << "\tdecrypt <sender> <input filename>" << std::endl;
    std::cerr << "\tverify <sender> <input filename>" << std::endl;
    std::cerr << "\tmakesig <file to sign>" << std::endl;
    std::cerr << "\tchecksig <sender> <signed file>" << std::endl;
    std::cerr << "\tkeycheck [basename]" << std::endl;
    std::cerr << "\tpassphrase [basename]" << std::endl;
}


static std::string get_passphrase (
    const std::string &prompt = std::string("Passphrase: "))
{
    struct termios termp;
    std::string passphrase;

    std::cerr << prompt;

    if (tcgetattr(STDIN_FILENO, &termp))
        throw std::runtime_error("tcgetattr() failed");
    termp.c_lflag &= ~ECHO;
    if (tcsetattr(STDIN_FILENO, TCSANOW, &termp))
        throw std::runtime_error("tcsetattr() failed");

    std::cin >> passphrase;

    termp.c_lflag |= ECHO;
    if (tcsetattr(STDIN_FILENO, TCSANOW, &termp))
        throw std::runtime_error("tcsetattr() failed");

    std::cerr << std::endl;

    return passphrase;
}


int main (int argc, char *argv[])
{
    try
    {
        if (argc < 2)
        {
            print_help(argv[0]);
            return 1;
        }

        std::string Home(std::getenv("HOME"));
        std::string KeyPath(Home + std::string(BSEX_SUBDIR));
        std::string OwnBase(KeyPath + std::string(BSEX_BASENAME));
        std::string Cmd(argv[1]);

        if (access(KeyPath.c_str(), R_OK | X_OK))
        {
            if (mkdir(KeyPath.c_str(), 0700))
            {
                throw std::runtime_error(
                    std::string("mkdir(): ") +
                    std::string(strerror(errno)));
            }
        }

        if (Cmd == "keygen" && argc == 2)
            keygen(OwnBase, get_passphrase());
        else if (Cmd == "encrypt" && argc == 3)
            encrypt(OwnBase, KeyPath + std::string(argv[2]), std::cout);
        else if (Cmd == "encrypt" && argc == 4)
        {
            std::ofstream fileOut(argv[3],
                std::ios_base::out | std::ios_base::binary | std::ios_base::trunc);
            encrypt(OwnBase, KeyPath + std::string(argv[2]), fileOut);
        }
        /*else if (Cmd == "decrypt" && argc == 3)
            decrypt(OwnBase, KeyPath + std::string(argv[2]), std::cin,
                get_passphrase());*/
        else if (Cmd == "decrypt" && argc == 4)
        {
            std::ifstream fileIn(argv[3],
                std::ios_base::in | std::ios_base::binary);
            decrypt(OwnBase, KeyPath + std::string(argv[2]), fileIn,
                get_passphrase());
        }
        /*else if (Cmd == "verify" && argc == 3)
            decrypt(OwnBase, KeyPath + std::string(argv[2]), std::cin,
                get_passphrase(), true);*/
        else if (Cmd == "verify" && argc == 4)
        {
            std::ifstream fileIn(argv[3],
                std::ios_base::in | std::ios_base::binary);
            decrypt(OwnBase, KeyPath + std::string(argv[2]), fileIn,
                get_passphrase(), true);
        }
        else if (Cmd == "makesig" && argc == 3)
            makesig(OwnBase, std::string(argv[2]));
        else if (Cmd == "checksig" && argc == 4)
            checksig(KeyPath + std::string(argv[2]), std::string(argv[3]));
        else if (Cmd == "keycheck" && argc == 2)
            keycheck(OwnBase, get_passphrase());
        else if (Cmd == "keycheck" && argc == 3)
            keycheck(KeyPath + std::string(argv[2]), get_passphrase());
        else if (Cmd == "passphrase" && argc == 2)
        {
            std::string oldPhrase =
                get_passphrase(std::string("Old passphrase: "));
            std::string newPhrase =
                get_passphrase(std::string("New passphrase: "));
            change_passphrase(OwnBase, oldPhrase, newPhrase);
        }
        else if (Cmd == "passphrase" && argc == 3)
        {
            std::string oldPhrase =
                get_passphrase(std::string("Old passphrase: "));
            std::string newPhrase =
                get_passphrase(std::string("New passphrase: "));
            change_passphrase(KeyPath + std::string(argv[2]),
                oldPhrase, newPhrase);
        }
        else
            print_help(argv[0]);
    }
    catch (std::exception &x)
    {
        std::cerr << x.what() << std::endl;
        return 2;
    }
    catch (...)
    {
        std::cerr << "Unknown exception" << std::endl;
        return 3;
    }
    return 0;
}

#include <string>

/*
    Pseudo Botan classes to simplify the example.
*/
namespace Botan {
    class AlgorithmIdentifier
    {
    };

    class RSA_PublicKey {
        public:
        PubKey(AlgorithmIdentifier id, int size)
        {
        }
    };

    class PK_Verifier
    {
        public:
        PK_Verifier(RSA_PublicKey ok, std::string algo)
        {
       }
    };
}

/* Example of nested constructors, aka temporaries in C++ */
int main()
{
    Botan::AlgorithmIdentifier id;
    Botan::PK_Verifier sig_verifier(Botan::RSA_PublicKey(id, 123), "I AM INCORRECT EMSA4(SHA-256)");
}

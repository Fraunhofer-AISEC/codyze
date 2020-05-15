#include <string>

/*
    Pseudo Botan classes to simplify the example.
*/
namespace Botan {
    class AlgorithmIdentifier
    {
    };

    class PubKey {
        public:
        PubKey(AlgorithmIdentifier id, int size)
        {
        }
    };

    class PK_Verifier
    {
        public:
        PK_Verifier(PubKey ok, std::string algo)
        {
       }
    };
}

/* Example of nested constructors, aka temporaries in C++ */
int main()
{
    Botan::AlgorithmIdentifier id;
    Botan::PK_Verifier sig_verifier(Botan::PubKey(id, 123), "I AM INCORRECT EMSA4(SHA-256)");
}

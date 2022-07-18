#include <iostream>
#include <botan/botan.h>

using namespace std;

int main() {
	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!

	Botan::Cipher_Mode *cm = Botan::get_cipher_mode("AES", Botan::Cipher_Dir::ENCRYPTION);

	Botan::Cipher_Mode *cm2 = Botan::get_cipher_mode("RC4", Botan::Cipher_Dir::ENCRYPTION);

	return 0;
}

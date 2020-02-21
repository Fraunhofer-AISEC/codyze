/* 
 * From Botan Handbook:
 * "None of the functions on Public_Key and Private_Key itself are particularly useful for users of the library,
because ‘bare’ public key operations are very insecure. The only purpose of these functions is to provide a clean
interface that higher level operations can be built on. So really the only thing you need to know is that when a function
takes a reference to a Public_Key, it can take any public key or private key, and similiarly for Private_Key."
* 
* TODO: shall we add forbidden ops?
 */



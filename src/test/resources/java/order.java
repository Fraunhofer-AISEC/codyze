public class Someclass {
// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

    void do_crypt()
    {

      char[] cipher;
      SymmetricKey key;
      InitializationVector iv;
      Cipher_Dir direction;
      char[] buf;

      Botan p = new Botan(1);
      p.set_key(key);
      p.start(iv.bits_of());
      p.finish(buf);
      p.foo(); // not in the entity and therefore ignored
      p.set_key(key);

      Botan p2 = new Botan(2);
      p2.start(iv.bits_of());
      // missing p2.finish(buf);

      Botan p3 = new Botan(2);
      if (3 < 4) {
        p3.start(iv);
      }
      p3.finish(buf);
      // potentially wrong path which only calls p3.finish without p3.start

      // ok:
      Botan p4 = new Botan(2);
      p4.start(iv);
      p4.finish(buf);

    }
}
/**
 op start() {
   Botan::start(iv);

   forbidden Botan::start();
   forbidden Botan::start(_);
   forbidden Botan::start(nonce, _);
   forbidden Botan::start_msg(*);
 }
 op finish() {
   Botan::finish(_)
 }

 rule UseOfBotan_CipherMode {
   using Forbidden as cm
   ensure
    order cm.start(), cm.finish()
   onfail WrongUseOfBotan_CipherMode
 }
*/
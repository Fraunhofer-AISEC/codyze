class forbidden {

// DOES NOT COMPILE
// DOES NOT MAKE REAL SENSE

void do_crypt()
        {

        int nonce;
        int nonce_length;
        int iv;
        BotanF b;

/*
              BotanF::get_cipher_mode(_, _);
    decl myValue = BotanF::AutoSeededRNG::random_vec(_);

              BotanF::set_key(_);
    forbidden BotanF::set_key(_, _);

              BotanF::start(iv);
    forbidden BotanF::start();
    forbidden BotanF::start(_);
    forbidden BotanF::start(nonce, _);

    forbidden BotanF::start_msg(*);
*/

// the following are directly allowed
        b.get_cipher_mode(nonce, nonce_length);
        b.set_key(iv);
        b.set_key(nonce);

// the following are forbidden

        b.start();
        b.start(nonce, b);

        b.start_msg(nonce); // not recognized yet (* missing)
        b.start_msg(nonce, iv, b); // not recognized yet (* missing)

        b.set_key(nonce, iv);

// the following are not forbidden. These match a forbidden rule, but also a non-forbidden rule

        b.start(iv);

// the following do not match anything, and are therefore allowed

        b.get_cipher_mode();
        b.get_cipher_mode(iv);


        }
}


public class BotanF {
  public void get_cipher_mode(){}
  public void get_cipher_mode(int o){}
  public void get_cipher_mode(int o, int o){}
  public void set_key(int o){}
  public void set_key(int o){}
  public void set_key(int o, int o){}
  public void start(){}
  public void start(int o){}
  public void start(int o, BotanF o){}
  public void start_msg(int o){}
  public void start_msg(int o, int o, BotanF o){}
}
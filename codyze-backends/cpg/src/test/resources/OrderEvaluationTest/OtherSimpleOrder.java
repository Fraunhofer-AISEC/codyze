public class OtherSimpleOrder {
	// DOES NOT COMPILE
	// DOES NOT MAKE REAL SENSE

	char[] cipher;
	int key;
	int iv;
	Cipher_Dir direction;
	char[] buf;

	void ok() {
		// ok:
		Botan p4 = new Botan(2);
		p4.start(1);
		p4.finish(buf);
	}

	void ok2() {
		// ok:
		Botan p4 = new Botan(2);
		p4.start(1);
		p4.foo(); // not in the entity and therefore ignored
		p4.finish(buf);
	}

	void nok1() {
		Botan p = new Botan(1);
		p.set_key(key); // not allowed as start
		p.start(iv);
		p.finish(buf);
		p.foo(); // not in the entity and therefore ignored
		p.set_key(key);
	}

	void nok2() {
		Botan p2 = new Botan(2);
		p2.start(1);
		// missing p2.finish(buf);
	}

	void nok3() {
		// ok:
		Botan p3 = new Botan(2);
		p3.start(0); // start is called with 0 instead of 1
		p3.finish(buf);
	}
}







public class Botan {
	public Botan(int i) {}

	public void create() {}

	public void finish(char[] b) {}

	public void init() {}

	public void process() {}

	public void reset() {}

	public void start(int i) {}

	public void set_key(int i) {}
}
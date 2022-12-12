interface Concept {}

interface Cipher : Concept {
    // to 'group' functions, we can use optional parameters and let the implementation kts file deal
    // with all the different signatures
    fun instantiate(transform: Any, provider: Any? = null): Op
    fun init(
        opmode: Any,
        certificate: Any? = null,
        random: Any? = null,
        key: Any? = null,
        params: Any? = null,
        paramspec: Any? = null,
    ): Op
    fun aad(src: Any, vararg args: Any): Op
    fun update(input: Any, output: Any? = null, vararg args: Any): Op
    fun finalize(input: Any? = null, output: Any? = null, vararg args: Any): Op
}

// @Rule("")
// fun `Crypt order2`(cipher: Cipher) {
//    cipher.transform evaluatesTo 5
// }

// @Rule("")
// fun `Crypt order`(cipher: Cipher) =
//// if (cipher::test)  // if could replace the 'given' block from MARK
// order {
//    once(cipher.instantiate(transform = "CCM"))
//    some(cipher::init)
// }

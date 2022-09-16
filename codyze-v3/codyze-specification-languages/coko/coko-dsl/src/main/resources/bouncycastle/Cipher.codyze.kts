interface Concept {}

interface Cipher : Concept {
    // to 'group' functions, we can use optional parameters and let the implementation kts file deal
    // with all the different signatures
    fun instantiate(transform: String, provider: Any = Wildcard): Nodes
    fun init(
        opmode: Int,
        certificate: Any = Wildcard,
        random: Any = Wildcard,
        key: Any = Wildcard,
        params: Any = Wildcard,
        paramspec: Any = Wildcard
    ): Nodes
    fun aad(src: Any = Wildcard, vararg args: Any): Nodes
    fun update(input: Any, output: Any = Wildcard, vararg args: Any): Nodes
    // fun finalize(input: Any = Wildcard, output: Any = Wildcard, vararg args: Any): Nodes
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

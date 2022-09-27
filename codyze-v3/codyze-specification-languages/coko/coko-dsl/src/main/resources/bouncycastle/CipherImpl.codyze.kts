@file:Import("Cipher.codyze.kts")

class CipherImpl : Cipher {
    // override val transform = variable("")

    override fun instantiate(transform: Any, provider: Any?) =
        // deal with the different function signatures with a more complex configuration of the
        // CallExpression
        callFqn("javax.crypto.Cipher.getInstance") {
            signature(transform) || signature(transform, provider)
        }

    override fun init(
        opmode: Any,
        certificate: Any?, // optional
        random: Any?, // optional
        key: Any?, // optional
        params: Any?, // optional
        paramspec: Any? // optional
    ) =
        callFqn("javax.crypto.Cipher.init") {
            signature(opmode, certificate) ||
                signature(opmode, certificate, random) ||
                signature(opmode, key) ||
                signature(opmode, key, params) ||
                signature(opmode, key, params, random) ||
                signature(opmode, key, random) ||
                signature(opmode, key, paramspec) ||
                signature(opmode, key, paramspec, random)
        }

    override fun aad(src: Any, vararg args: Any) =
        callFqn("javax.crypto.Cipher.updateAAD") {
            signature(src)
            // TODO: signature(src, ...) in MARK
        }

    override fun update(input: Any, output: Any?, vararg args: Any) =
        callFqn("javax.crypto.Cipher.update") {
            signature(input) ||
                // TODO: Do we need args here? The arguments are not important for the analysis
                //      according to the MARK entity
                when (args.size) {
                    2 ->
                        signature(input, Wildcard, Wildcard) ||
                            signature(input, Wildcard, Wildcard, output)
                    3 -> signature(input, Wildcard, Wildcard, output, Wildcard)
                    else -> false
                } ||
                signature(input, output)

            //            input flowsTo arguments[0] &&
            //                    if (output != Wildcard && args.size != 0) output flowsTo
            // arguments[3]
            //                    else output flowsTo arguments[1]
        }

    override fun finalize(input: Any?, output: Any?, vararg args: Any) =
        callFqn("javax.crypto.Cipher.doFinal") {
            signature() ||
                signature(input) ||
                when (args.size) {
                    1 -> signature(output, Wildcard)
                    2 ->
                        signature(input, Wildcard, Wildcard) ||
                            signature(input, Wildcard, Wildcard, output) ||
                            signature(input, Wildcard, Wildcard, output, Wildcard)
                    else -> false
                } ||
                signature(input, output)
        }
}

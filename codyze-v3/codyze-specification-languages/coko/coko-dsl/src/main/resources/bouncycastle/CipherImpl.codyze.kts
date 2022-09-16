@file:Import("Cipher.codyze.kts")

class CipherImpl : Cipher {
    // override val transform = variable("")

    override fun instantiate(transform: String, provider: Any) =
        // deal with the different function signatures with a more complex configuration of the
        // CallExpression
        callFqn("javax.crypto.Cipher.getInstance") {
            if (provider != Wildcard)
                transform flowsTo arguments[0] && provider flowsTo arguments[1]
            else transform flowsTo arguments[0]
        }

    // TODO: is there a better name?
    // TODO: move this to Evaluators.kt
    fun signature(vararg args: Any): Boolean {
        // do an automatic args[0] flowsTo argument[0] ...
        return false
    }

    override fun init(
        opmode: Int,
        certificate: Any, // optional
        random: Any, // optional
        key: Any, // optional
        params: Any, // optional
        paramspec: Any // optional
    ) =
        callFqn("javax.crypto.Cipher.init") {
            // TODO: this is quite hard to read.. can we make it easier?
            //            allow declarative description of where the parameters go
            //            - javax_crypto_Cipher_init(opmode, certificate)
            //            or like this: signature(opmode, certificate) || signature(opmode, random)
            //            or actually create a javax object that has a crypto member?

            var result: Boolean = opmode flowsTo arguments[0]

            if (certificate != Wildcard) result = result && certificate flowsTo arguments[1]

            if (params != Wildcard) result = result && random flowsTo arguments[3]
            else result = result && random flowsTo arguments[2]

            if (key != Wildcard) result = result && key flowsTo arguments[1]

            if (params != Wildcard) result = result && params flowsTo arguments[2]

            if (paramspec != Wildcard) result = result && paramspec flowsTo arguments[2]

            result
        }

    override fun aad(src: Any, vararg args: Any) =
        callFqn("javax.crypto.Cipher.updateAAD") { src flowsTo arguments[0] }

    override fun update(input: Any, output: Any, vararg args: Any) =
        callFqn("javax.crypto.Cipher.update") {
            input flowsTo arguments[0] &&
                if (output != Wildcard && args.size != 0) output flowsTo arguments[3]
                else output flowsTo arguments[1]
        }

    //    override fun finalize(input: Any, output: Any, vararg args: Any) =
    //        callFqn("javax.crypto.Cipher.doFinal") {
    //
    //        }
}

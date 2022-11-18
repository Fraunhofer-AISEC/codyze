@file:Import("Cipher.codyze.kts")

class CipherImpl : Cipher {
    // override val transform = variable("")

    override fun instantiate(transform: Any, provider: Any?) =
    // deal with the different function signatures with a more complex configuration of the
    // CallExpression
    op {
        +definition("javax.crypto.Cipher.getInstance") {
            +signature(transform)
            +signature(transform, provider)
        }
    }

    override fun init(
        opmode: Any,
        certificate: Any?, // optional
        random: Any?, // optional
        key: Any?, // optional
        params: Any?, // optional
        paramspec: Any? // optional
    ) = op {
        +definition("javax.crypto.Cipher.init") {
            +signature(opmode, certificate)
            +signature(opmode, certificate, random)
            +signature(opmode, key)
            +signature(opmode, key, params)
            +signature(opmode, key, params, random)
            +signature(opmode, key, random)
            +signature(opmode, key, paramspec)
            +signature(opmode, key, paramspec, random)
        }
    }

    override fun aad(src: Any, vararg args: Any) = op {
        +definition("javax.crypto.Cipher.updateAAD") {
            +signature(src)
            // TODO: signature(src, ...) in MARK
        }
    }

    override fun update(input: Any, output: Any?, vararg args: Any) = op {
        +definition("javax.crypto.Cipher.update") {
            +signature(input)
            +signature(input, Wildcard, Wildcard)
            +signature(input, Wildcard, Wildcard, output)
            +signature(input, Wildcard, Wildcard, output, Wildcard)
            +signature(input, output)
        }
    }

    override fun finalize(input: Any?, output: Any?, vararg args: Any) = op {
        +definition("javax.crypto.Cipher.doFinal") {
            +signature()
            +signature(input)
            +signature(output, Wildcard)
            +signature(input, Wildcard, Wildcard)
            +signature(input, Wildcard, Wildcard, output)
            +signature(input, Wildcard, Wildcard, output, Wildcard)
            +signature(input, output)
        }
    }
}

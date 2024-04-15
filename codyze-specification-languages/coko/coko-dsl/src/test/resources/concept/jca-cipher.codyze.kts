@file:Import("bsi-tr-rules.codyze.kts")

import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.BackendDataItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.DataItem
import de.fraunhofer.aisec.codyze.specificationLanguages.coko.core.modelling.value

class JCACipher {
    fun getInstance(transformation: Any, provider: Any? = null) = op {
        // TODO: Change back to "javax.crypto.Cipher.getInstance" when CPG resolves names correctly
        "Cipher.getInstance" {
            signature(transformation)
            signature(transformation, provider)
        }
    }

    fun init(opmode: Any, certificate: Any?, random: Any?, key: Any?, params: Any?) = op {
        "javax.crypto.Cipher.init" {
            signature(opmode, certificate)
            signature(opmode, certificate, random)
            signature(opmode, key)
            signature(opmode, key, params)
            signature(opmode, key, params, random)
            signature(opmode, key, random)

        }
    }

    fun doFinal(input: Any?, output: Any?, outputOffset: Any?, inputOffset: Any?, inputLen: Any?) = op {
        "javax.crypto.Cipher.doFinal" {
            signature()
            signature(input)
            signature(output, outputOffset)
            signature(input, inputOffset, inputLen)
            signature(input, inputOffset, inputLen, output)
            signature(input, inputOffset, inputLen, output, outputOffset)
            signature(input, output)
        }
    }
}

class JCAEncryptionImpl: Bsi_tr_rules_codyze.Encryption {
    private val jcaCipher = JCACipher()
    override val cypher: Bsi_tr_rules_codyze.Cypher
        get() = JCACipherImpl()
    override val iv: Bsi_tr_rules_codyze.InitializationVector
        get() = TODO("Not yet implemented")

    override fun encrypt(plaintext: Any?): Op =
        jcaCipher.doFinal(plaintext, Wildcard, Wildcard, Wildcard, Wildcard) with
                jcaCipher.init(".*ENCRYPT_MODE", Wildcard, Wildcard, Wildcard, Wildcard)

    override fun decrypt(ciphertext: Any?): Op {
        TODO("Not yet implemented")
    }

}

// "AES/CBC/Padding"
class JCACipherImpl: Bsi_tr_rules_codyze.Cypher {
    val cipher = JCACipher()

    override val algo: DataItem<Bsi_tr_rules_codyze.Algorithm>
        get() = cipher.getInstance(Wildcard, Wildcard).arguments[0] withTransformation { dataItem ->
            transformTransformationString(dataItem, 0, "an algorithm") { string ->
                Bsi_tr_rules_codyze.Algorithm.entries.firstOrNull {  it.name == string.uppercase() }
            }
        }
    override val mode: DataItem<Bsi_tr_rules_codyze.Mode>
        get() = cipher.getInstance(Wildcard, Wildcard).arguments[0] withTransformation { dataItem ->
            transformTransformationString(dataItem, 1, "a mode") { string ->
                Bsi_tr_rules_codyze.Mode.entries.firstOrNull {  it.name == string.uppercase() }
            }
        }
    override val keySize: DataItem<Int>
        get() = value(1)
    override val tagSize: DataItem<Int>
        get() = value(1)


    fun <E> transformTransformationString(
        dataItem: BackendDataItem,
        index: Int,
        errorMessage: String,
        stringToResult: (String) -> E?): TransformationResult<E, String> {
        val stringValue = dataItem.value as? String
        return if(dataItem.value == null)
            TransformationResult.failure("Value of BackendDataItem is null.")
        else if(stringValue == null) {
            TransformationResult.failure("Value of BackendDataItem is not a string.")
        }
        else {
            val string = stringValue.split('/')[index]
            val result = stringToResult(string)
            if(result == null)
                TransformationResult.failure("Could not resolve the string value to $errorMessage")
            else
                TransformationResult.success(result)
        }
    }
}




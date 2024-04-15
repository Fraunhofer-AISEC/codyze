interface BlockCiper {
    fun algorithm(algo: Any): Op
    fun blockLength(length: Any): Op
    fun mode(mode: Any): Op
    fun padding(pad: Any): Op
}


// TODO: need evaluator with condition so we can check if algorithm is AES
@Rule(description = "Check the block length of AES")
fun `correct AES block length`(blockCiper: BlockCiper) =
    only(blockCiper.blockLength(listOf(128, 192, 256)))

@Rule(description = "Check the operation mode of ")
fun `correct mode`(blockCiper: BlockCiper) =
    only(blockCiper.mode(listOf("CCM", "GCM", "CBC", "CTR")))
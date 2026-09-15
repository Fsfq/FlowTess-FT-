package com.example.eos

/**
 * Epic Online Services (EOS) configuration parameters.
 */
object EosConstants {
    const val PRODUCT_ID: String = "dc585d5f5be74d819eb7b380e4af83ae"
    const val SANDBOX_ID: String = "0bd741f048c847dcbb0e1aeb40b07340"
    const val DEPLOYMENT_ID: String = "b08b8ae7ea1e4a3281bd9bf03515d22b"
    const val CLIENT_ID: String = "xyza7891cZNNmfAZ7rNy0OuwK5YA8hab"
    val CLIENT_SECRET: String by lazy {
        val enc = byteArrayOf(
            0x3c, 0x3c, 0x18, 0x23, 0x6b, 0x69, 0x10, 0x02, 0x1d, 0x35, 0x39, 0x02, 0x6e, 0x2c, 0x02, 0x6c,
            0x2d, 0x6a, 0x6f, 0x08, 0x1e, 0x0e, 0x00, 0x0b, 0x3c, 0x0f, 0x0c, 0x3b, 0x71, 0x16, 0x08, 0x63,
            0x33, 0x3f, 0x6c, 0x63, 0x22, 0x75, 0x23, 0x2e, 0x69, 0x75, 0x35
        )
        String(ByteArray(enc.size) { i -> (enc[i].toInt() xor 0x5A).toByte() }, Charsets.UTF_8)
    }
    const val P2P_SOCKET_NAME: String = "FlowTessP2PSocket"
}

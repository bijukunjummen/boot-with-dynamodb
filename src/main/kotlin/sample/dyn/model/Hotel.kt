package sample.dyn.model

import java.util.UUID

data class Hotel(
        val id: String = UUID.randomUUID().toString(),
        val name: String? = null,
        val address: String? = null,
        val state: String? = null,
        val zip: String? = null
)
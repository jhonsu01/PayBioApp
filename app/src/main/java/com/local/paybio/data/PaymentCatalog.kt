package com.local.paybio.data

import android.content.Context
import org.json.JSONObject

/** A payment method template loaded from assets/payment_catalog.json. */
data class CatalogMethod(
    val platform: String,
    val type: String,
    val color: String,
    val pattern: String,
    val hint: String
) {
    val regex: Regex? by lazy { runCatching { Regex(pattern) }.getOrNull() }
    fun matches(value: String): Boolean = regex?.matches(value.trim()) ?: false
}

data class CatalogCountry(
    val name: String,
    val methods: List<CatalogMethod>
)

object PaymentCatalog {

    @Volatile
    private var cached: List<CatalogCountry>? = null

    fun load(context: Context): List<CatalogCountry> =
        cached ?: synchronized(this) {
            cached ?: parse(context).also { cached = it }
        }

    private fun parse(context: Context): List<CatalogCountry> {
        val json = context.assets.open("payment_catalog.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val countriesArr = root.getJSONArray("countries")
        val countries = ArrayList<CatalogCountry>(countriesArr.length())
        for (i in 0 until countriesArr.length()) {
            val c = countriesArr.getJSONObject(i)
            val methodsArr = c.getJSONArray("methods")
            val methods = ArrayList<CatalogMethod>(methodsArr.length())
            for (j in 0 until methodsArr.length()) {
                val m = methodsArr.getJSONObject(j)
                methods.add(
                    CatalogMethod(
                        platform = m.getString("platform"),
                        type = m.optString("type", ""),
                        color = m.optString("color", "#00E676"),
                        pattern = m.optString("pattern", ".+"),
                        hint = m.optString("hint", "")
                    )
                )
            }
            countries.add(CatalogCountry(c.getString("name"), methods))
        }
        return countries
    }
}

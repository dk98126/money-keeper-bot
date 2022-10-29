package com.github.dk98126.moneykeeperbot.money

import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.propertyeditors.CurrencyEditor
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
class RubRatesKeeper(
    private val restTemplate: RestTemplate,
    @Value("\${currency-api.key}")
    private val key: String,
) {
    private val rates = ConcurrentHashMap<String, Double>()
    private val from = "RUB"
    private val to = listOf("USD", "EUR", "TRY")

    fun getRubRates(): Map<String, Double> = HashMap(rates)

    //    @Scheduled(fixedRate = 600000)
    @PostConstruct
    private fun updateRates() {
        val ratesUri = URIBuilder("https://api.currencyapi.com/v3/latest")
            .addParameter("apikey", key)
            .addParameter("base_currency", from)
            .addParameter("currencies", to.joinToString(separator = ","))
            .build()

        val response = restTemplate.getForEntity<RatesResponseBody>(ratesUri)
        response.body?.let { body ->
            rates.putAll(
                body.data.map {
                    it.value.code to it.value.value
                }
            )
        }
    }
}

private data class RatesResponseBody(
    val data: Map<String, Currency>
)

private data class Currency(
    val code: String,
    val value: Double
)

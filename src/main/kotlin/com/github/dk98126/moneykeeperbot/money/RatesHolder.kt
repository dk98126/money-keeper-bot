package com.github.dk98126.moneykeeperbot.money

import com.github.dk98126.moneykeeperbot.money.currency.Currency
import com.github.dk98126.moneykeeperbot.money.currency.Currency.*
import com.github.dk98126.moneykeeperbot.money.currency.CurrencyRates
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import java.lang.IllegalArgumentException
import java.time.Instant
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
class RatesHolder(
    private val restTemplate: RestTemplate,
    @Value("\${currency-api.key}")
    private val key: String,
) {
    private val log = LoggerFactory.getLogger(RatesHolder::class.java)

    private val currencyToRates = ConcurrentHashMap<Currency, CurrencyRates>()

    private lateinit var instant: Instant
    fun getRatesFor(currency: Currency): CurrencyRates =
        currencyToRates[currency] ?: throw IllegalArgumentException("Неизвестная валюта $currency")

    fun getLastUpdated(): Instant {
        return instant
    }

    @Scheduled(cron = "0 * * * *")
    @PostConstruct
    private fun updateRates() {
        instant = Instant.now()
        for (base in Currency.values()) {
            val others = listOf(*Currency.values()) - base

            val ratesUri = URIBuilder("https://api.currencyapi.com/v3/latest")
                .addParameter("apikey", key)
                .addParameter("base_currency", base.code)
                .addParameter("currencies", others.joinToString(separator = ",") { it.code })
                .build()
            val response = restTemplate.getForEntity<RatesResponseBody>(ratesUri)
            response.body?.let { body ->
                val rates = mutableMapOf(base to 1.0)
                body.data.map {
                    rates[it.key] = it.value.value
                }
                currencyToRates.put(base, CurrencyRates(base, rates))
            }
        }

        log.info("Rates updated: $currencyToRates")
    }
}

private data class RatesResponseBody(
    val data: Map<Currency, Amount>
)

private data class Amount(
    val value: Double
)

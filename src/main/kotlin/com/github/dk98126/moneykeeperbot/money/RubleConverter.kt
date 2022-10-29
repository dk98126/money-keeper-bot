package com.github.dk98126.moneykeeperbot.money

import org.springframework.stereotype.Component

@Component
class RubleConverter(
    private val rubRatesKeeper: RubRatesKeeper
) {

    private val symbolToCodeMap = mapOf(
        "$" to "USD",
        "€" to "EUR",
        "₺" to "TRY",
    )

    fun convert(amount: Double, currencySymbol: String): Double {
        if (currencySymbol == "₽") {
            return amount
        }
        val rates = rubRatesKeeper.getRubRates()
        val code = symbolToCodeMap[currencySymbol]
            ?: throw IllegalArgumentException("Валюта $currencySymbol не поддерживается.")
        val rate = rates[code] ?: throw IllegalStateException("Курс валюты $code не найден.")
        return amount / rate
    }
}

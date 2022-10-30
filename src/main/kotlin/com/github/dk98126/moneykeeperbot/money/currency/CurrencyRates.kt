package com.github.dk98126.moneykeeperbot.money.currency

data class CurrencyRates(
    val baseCurrency: Currency,
    private val rates: Map<Currency, Double>
) {
    fun getRate(to: Currency): Double =
        rates[to] ?: throw IllegalArgumentException("Курс валюты $to к $baseCurrency неизвестен")
}

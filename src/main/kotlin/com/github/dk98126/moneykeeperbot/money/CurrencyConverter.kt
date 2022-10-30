package com.github.dk98126.moneykeeperbot.money

import com.github.dk98126.moneykeeperbot.money.currency.Currency
import com.github.dk98126.moneykeeperbot.money.currency.CurrencyAmount
import org.springframework.stereotype.Component

@Component
class CurrencyConverter(
    private val ratesHolder: RatesHolder,
) {
    fun convert(currencyAmount: CurrencyAmount, to: Currency): CurrencyAmount {
        val currencyRates = ratesHolder.getRatesFor(to)
        val rate = currencyRates.getRate(currencyAmount.currency)
        return CurrencyAmount(
            currency = to,
            value = currencyAmount.value / rate,
        )
    }
}

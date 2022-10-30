package com.github.dk98126.moneykeeperbot.money.currency

import java.lang.IllegalArgumentException

enum class Currency(
    val symbol: String
) {
    RUB("₽"),
    USD("$"),
    EUR("€"),
    TRY("₺");

    companion object {
        fun fromSymbol(symbol: String): Currency {
            return Currency.values().firstOrNull { it.symbol == symbol }
                ?: throw IllegalArgumentException("Валюта для символа $symbol не найдена")
        }
    }

    val code: String = this.name

    override fun toString(): String = symbol
}

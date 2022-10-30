package com.github.dk98126.moneykeeperbot.bot

import com.github.dk98126.moneykeeperbot.money.CurrencyConverter
import com.github.dk98126.moneykeeperbot.money.currency.Currency.*
import com.github.dk98126.moneykeeperbot.money.currency.CurrencyAmount
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Component
class MoneyKeeperBot(
    @Value("\${bot.token}")
    private val token: String,
    @Value("\${bot.username}")
    private val username: String,
    private val currencyConverter: CurrencyConverter,
) : TelegramLongPollingBot() {

    private val CURRENCY_PATTERN = "(\\d+[.,]?\\d+)([₽$€₺])".toRegex()

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = username

    private val availableCurrencies = linkedSetOf(RUB, USD, EUR, TRY)

    private val amountsEntered = mutableListOf<CurrencyAmount>()

    override fun onUpdateReceived(update: Update) {

        if (update.hasMessage() && update.message.hasText()) {
            val message = SendMessage()
            message.chatId = update.message.chatId.toString()

            val receivedText = update.message.text

            var textToSend: String = """
                Привет! Я умею считать твои накопления и конвертировать их в базовую валюту.
                Напиши /calculate, чтобы начать расчет.
            """.trimIndent()

            if (receivedText == "/calculate") {
                textToSend = """
                    Вводи валюту в следующих форматах:
                    
                    1000$
                    555€
                    46.7₺
                    55,47₽
                    
                    Сейчас подсчет доступен в 4 валютах: $availableCurrencies.
                """.trimIndent()
            }

            CURRENCY_PATTERN.matchEntire(receivedText)?.let { matchResult ->
                val amount = matchResult.groups[1]!!.value.replace(oldValue = ",", newValue = ".").toDouble()
                val currencySymbol = matchResult.groups[2]!!.value
                amountsEntered += CurrencyAmount(
                    currency = Companion.fromSymbol(currencySymbol),
                    value = amount
                )

                textToSend = "Добавлено ${amount.format()}$currencySymbol"
            }

            if (receivedText == "/reset") {
                amountsEntered.clear()
                textToSend = "Подсчет сброшен."
            }

            if (receivedText == "/sum") {
                val base = RUB
                val convertedAmountsPairs = amountsEntered.map { fromAmount ->
                    val toAmount = currencyConverter.convert(
                        fromAmount,
                        RUB
                    )
                    fromAmount to toAmount
                }
                textToSend = convertedAmountsPairs.joinToString(separator = "\n") {
                    "${it.first.value.format()}${it.first.currency} -> ${it.second.value.format()}${it.second.currency}"
                }
                    .plus("\n")
                    .plus(
                        "Всего: ${convertedAmountsPairs.sumOf { it.second.value }.format()}$base"
                    )
            }

            message.text = textToSend



            try {
                execute(message) // Call method to send the message
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

}

private fun Double.format(digits: Int = 1) = "%.${digits}f".format(this)

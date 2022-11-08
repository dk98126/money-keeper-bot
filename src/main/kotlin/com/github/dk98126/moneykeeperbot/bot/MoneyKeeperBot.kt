package com.github.dk98126.moneykeeperbot.bot

import com.github.dk98126.moneykeeperbot.money.CurrencyConverter
import com.github.dk98126.moneykeeperbot.money.RatesHolder
import com.github.dk98126.moneykeeperbot.money.currency.Currency.*
import com.github.dk98126.moneykeeperbot.money.currency.CurrencyAmount
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update


@Component
class MoneyKeeperBot(
    @Value("\${bot.token}")
    private val token: String,
    @Value("\${bot.username}")
    private val username: String,
    private val currencyConverter: CurrencyConverter,
    private val ratesHolder: RatesHolder,
) : TelegramWebhookBot() {

    private val CURRENCY_PATTERN = "(\\d*?[.,]?\\d+)([₽$€₺])(.{0,32}?)".toRegex()

    private val availableCurrencies = linkedSetOf(RUB, USD, EUR, TRY)

    private val amountsEnteredWithComments = mutableListOf<Pair<CurrencyAmount, String?>>()

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = username

    override fun getBotPath(): String = "https://money-keeper-bot.herokuapp.com/"

    override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*> {
        if (update.hasMessage() && update.message.hasText()) {
            val message = SendMessage()
            message.chatId = update.message.chatId.toString()

            val receivedText = update.message.text

            var textToSend: String = """
                Привет! Я умею считать твои накопления и конвертировать их в базовую валюту.
                Напиши /examples, чтобы увидеть примеры.
            """.trimIndent()

            if (receivedText == "/examples") {
                textToSend = """
                    Вводи валюту в следующих форматах:
                    
                    Добавь
                    1000$ в банке
                    
                    Добавь
                    555€ в кошельке
                    
                    Добавь
                    46.7₺ на карте
                    
                    Добавь
                    55,47₽
                    
                    Можно отправить сразу несколько волют в одном сообщении (каждую с новой строки):
                    Добавь
                    1000$ в банке
                    555€ в кошельке
                    46.7₺ на карте
                    55,47₽
                    
                    Сейчас подсчет доступен в 4 валютах: $availableCurrencies.
                """.trimIndent()
            }

            if (receivedText.startsWith("Добавь")) {
                val splitted = receivedText.split("\n")
                val currenciesWithComment = splitted.subList(1, splitted.size)
                textToSend = currenciesWithComment
                    .map { it.trim() }.joinToString(separator = "\n") { addMoneyIfPossible(it) }
            }

            if (receivedText == "/reset") {
                amountsEnteredWithComments.clear()
                textToSend = "Подсчет сброшен."
            }

            if (receivedText == "/sum") {
                val base = RUB
                val convertedAmountsPairs = amountsEnteredWithComments.map { fromAmount ->
                    val toAmount = currencyConverter.convert(
                        fromAmount.first,
                        RUB
                    )
                    fromAmount to toAmount
                }
                textToSend = convertedAmountsPairs.joinToString(separator = "\n") {
                    "${it.first.first.value.format()}${it.first.first.currency} -> ${it.second.value.format()}${it.second.currency}${it.first.second?.let { comment -> " - $comment" } ?: ""}"
                }
                    .plus("\n")
                    .plus(
                        "Всего: ${convertedAmountsPairs.sumOf { it.second.value }.format()}$base\n"
                    )
                    .plus("Курс на ${ratesHolder.getLastUpdated()}")
            }

            message.text = textToSend



            return message
        } else {
            throw RuntimeException("No text been provided in update")
        }
    }

    private fun addMoneyIfPossible(currencyLine: String): String {
        CURRENCY_PATTERN.matchEntire(currencyLine)?.let { matchResult ->
            val amount = matchResult.groups[1]!!.value.replace(oldValue = ",", newValue = ".").toDouble()
            val currencySymbol = matchResult.groups[2]!!.value
            val comment = matchResult.groups[3]!!.value.trim()
            amountsEnteredWithComments += CurrencyAmount(
                currency = Companion.fromSymbol(currencySymbol),
                value = amount
            ) to comment

            return "Добавлено ${amount.format()}$currencySymbol - $comment"
        } ?: return "Не удалось добавить: $currencyLine"
    }

}

private fun Double.format(digits: Int = 1) = "%.${digits}f".format(this)

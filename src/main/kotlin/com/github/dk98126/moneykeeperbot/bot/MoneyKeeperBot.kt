package com.github.dk98126.moneykeeperbot.bot

import com.github.dk98126.moneykeeperbot.money.RubleConverter
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
    private val rubleConverter: RubleConverter,
) : TelegramLongPollingBot() {

    private val CURRENCY_PATTERN = "(\\d+[.,]?\\d+)([₽$€₺])".toRegex()

    private val mustEscapeChars = "_*[]()~`>#+-=|{}.!"

    override fun getBotToken(): String = token

    override fun getBotUsername(): String = username

    private val availableCurrencies = linkedSetOf("₽", "$", "€", "₺")

    private val amountsEntered = mutableListOf<Pair<Double, String>>()

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
                val amount = matchResult.groups[1]!!.value.toDouble()
                val currency = matchResult.groups[2]!!.value
                amountsEntered += amount to currency

                textToSend = "Добавлено ${amount.format()}$currency"
            }

            if (receivedText == "/reset") {
                amountsEntered.clear()
                textToSend = "Подсчет сброшен."
            }

            if (receivedText == "/sum") {
                val amountsToRubles = amountsEntered.map {
                    val rublesEquivalent = rubleConverter.convert(
                        amount = it.first,
                        currencySymbol = it.second
                    )
                    it to rublesEquivalent
                }
                textToSend = amountsToRubles.joinToString(separator = "\n") {
                    "${it.first.first.format()}${it.first.second} -> ${it.second.format()}₽"
                }
                    .plus("\n")
                    .plus(
                        "Всего: ${amountsToRubles.sumOf { it.second }.format()}₽"
                    )
            }

//            mustEscapeChars.forEach { char -> textToSend = textToSend.replace("$char", "\\$char") }
            message.text = textToSend



            try {
                execute(message) // Call method to send the message
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

}

fun Double.format(digits: Int = 2) = "%.${digits}f".format(this)

package com.github.dk98126.moneykeeperbot.bot

import com.github.dk98126.moneykeeperbot.counter.IntegerConventionalUnitsCounter
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

    private val integerConventionalUnitsCounter: IntegerConventionalUnitsCounter,
) : TelegramLongPollingBot() {
    override fun getBotToken(): String = token

    override fun getBotUsername(): String = username

    override fun onUpdateReceived(update: Update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.message.hasText()) {
            val message = SendMessage()
            message.chatId = update.message.chatId.toString()

            val receivedText = update.message.text

            receivedText.toIntOrNull()?.let {
                integerConventionalUnitsCounter.add(it)
                message.text = "Прибавлено $it"
            }

            if (receivedText == "/count") {
                val sum = integerConventionalUnitsCounter.count()
                message.text = "Посчитано $sum"
            }

            if (receivedText == "/reset") {
                integerConventionalUnitsCounter.reset()
                message.text = "Счетчик сброшен"
            }

            try {
                execute(message) // Call method to send the message
            } catch (e: TelegramApiException) {
                e.printStackTrace()
            }
        }
    }

}

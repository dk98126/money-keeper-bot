package com.github.dk98126.moneykeeperbot.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@Component
class MoneyKeeperBotStarter(
    moneyKeeperBot: MoneyKeeperBot
) {
    init {
        try {
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(moneyKeeperBot)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}

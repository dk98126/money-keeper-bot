package com.github.dk98126.moneykeeperbot.bot

import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession


@Component
class MoneyKeeperBotStarter(
    moneyKeeperBot: MoneyKeeperBot
) {
    init {
        try {
            val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
            botsApi.registerBot(moneyKeeperBot, SetWebhook("https://money-keeper-bot.herokuapp.com"))
            moneyKeeperBot.execute(
                SetMyCommands(
                    listOf(
                        BotCommand("examples", "посмотреть примеры"),
                        BotCommand("sum", "посчитать сумму"),
                        BotCommand("reset", "сбросить счетчик")
                    ),
                    BotCommandScopeDefault(),
                    null
                )
            )
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }
}

package com.github.dk98126.moneykeeperbot.counter

import org.springframework.stereotype.Component

@Component
class IntegerConventionalUnitsCounter : MoneyCounter<Int> {

    @Volatile
    private var sum: Int = 0

    override fun add(amount: Int) {
        sum += amount
    }

    override fun count(): Int = sum

    override fun reset() {
        sum = 0
    }
}

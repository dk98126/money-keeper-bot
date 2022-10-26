package com.github.dk98126.moneykeeperbot.counter

interface MoneyCounter<T> {
    fun add(amount: T)
    fun count(): T
    fun reset()
}

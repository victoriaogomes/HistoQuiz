package com.lenda.histoquiz.util

import java.util.*

class RoomCreator {
    var lowerAlphabet = "abcdefghijklmnopqrstuvwxyz"
    var actualRoomName: String? = null
    var numbers = "0123456789"
    private var upperAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private var alphaNumeric = upperAlphabet + lowerAlphabet + numbers
    private var sb = StringBuilder()
    private var random = Random()

    fun newRoomCode(length: Int): String {
        for (i in 0 until length) {
            val index = random.nextInt(alphaNumeric.length)
            val randomChar = alphaNumeric[index]
            sb.append(randomChar)
        }
        actualRoomName = sb.toString()
        return actualRoomName as String
    }
}
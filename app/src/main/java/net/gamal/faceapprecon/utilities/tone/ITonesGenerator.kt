package net.gamal.faceapprecon.utilities.tone

internal interface ITonesGenerator {
    fun registerSuccessfulTone()
    fun registerDeclinedTone()
    fun registerDefaultTone(timeInMillis:Double)
}
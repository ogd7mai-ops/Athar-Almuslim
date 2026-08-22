package com.example.data.model

data class Reciter(
    val id: String,
    val name: String,
    val rewaya: String = "حفص عن عاصم",
    val serverUrl: String,
    val photoUrl: String = ""
)

data class MuezzinVoice(
    val id: String,
    val name: String,
    val location: String,
    val sampleAudioUrl: String
)

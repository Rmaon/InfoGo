package com.ramon.infogo

data class Usuario(
    val email: String = "",
    var username: String = "",
    val profilePic: String = "",
    val password: String = ""
) {
    // Constructor sin argumentos requerido por Firebase Firestore
    constructor() : this("", "", "", "")
}

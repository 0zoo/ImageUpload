package xyz.e0zoo.todolistapplication.api.model

data class User(val email: String,val name: String, val password: String)

data class SignInUserBody(val email: String, val password: String)


package com.example.contactsapp.model

sealed class ResponseModel<out T>() {
    data class Success<out T>(val data: T) : ResponseModel<T>()
    data class Loading(val isLoading: Boolean) : ResponseModel<Nothing>()
    data class Error(val message: String) : ResponseModel<Nothing>()
}

package com.example.contactsapp.network

import com.example.contactsapp.model.ContactModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface NetworkServiceInterface {

    @GET("/api/contacts")
    suspend fun getUserDetails() : Response<ContactModel>

}
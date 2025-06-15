package com.example.contactsapp.network

import com.example.contactsapp.model.ContactModel
import retrofit2.Response

class Repository(val networkServiceInterface: NetworkServiceInterface) {

    suspend fun getUserContactDetails() : Response<ContactModel> {
        return networkServiceInterface.getUserDetails()
    }

}
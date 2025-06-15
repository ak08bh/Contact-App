package com.example.contactsapp

import android.app.Application
import com.example.contactsapp.network.NetworkService
import com.example.contactsapp.network.Repository

class UserApplication : Application(){

    lateinit var repository : Repository

    override fun onCreate() {
        super.onCreate()

        repository = Repository(NetworkService.networkInterface)

    }
}
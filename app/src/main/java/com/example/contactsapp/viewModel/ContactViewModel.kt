package com.example.contactsapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contactsapp.model.ResponseModel
import com.example.contactsapp.model.User
import com.example.contactsapp.network.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(val repository: Repository) : ViewModel() {

    private val _list = MutableLiveData<ResponseModel<List<User>>>()

    val list : LiveData<ResponseModel<List<User>>> get() = _list


    fun getUserContactDetails(){
        viewModelScope.launch {

            try {
                _list.value = ResponseModel.Loading(true)
                val response = withContext(Dispatchers.IO) {
                    repository.getUserContactDetails()
                }
                if (response.isSuccessful) {
                    val items = response.body()?.Data?.users ?: emptyList()
                    _list.value = ResponseModel.Success(items)
                } else {
                    _list.value = ResponseModel.Error(response.message())
                }
            } catch (e: Exception) {
                _list.value = ResponseModel.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

}
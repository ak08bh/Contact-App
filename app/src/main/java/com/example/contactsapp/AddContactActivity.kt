package com.example.contactsapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.contactsapp.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {

    private var _binding : ActivityAddContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

     /*   val name = binding.name.text
        val surname = binding.surname.text
        val company = binding.company.text
        val phone = binding.phone.text*/

// Creates a new Intent to insert a contact
        val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            // Sets the MIME type to match the Contacts Provider
            type = ContactsContract.RawContacts.CONTENT_TYPE
        }
        binding.saveButton.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val surname = binding.surname.text.toString().trim()
            val company = binding.company.text.toString().trim()
            val phone = binding.phone.text.toString().trim()

            System.out.println("$phone the phone")

            intent.apply {
                val intent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
                    type = ContactsContract.RawContacts.CONTENT_TYPE
                    putExtra(ContactsContract.Intents.Insert.NAME, "$name $surname")
                    putExtra(ContactsContract.Intents.Insert.COMPANY, company)
                    putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                    putExtra(
                        ContactsContract.Intents.Insert.PHONE_TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    )
                }

                startActivity(intent)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
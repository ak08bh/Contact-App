package com.example.contactsapp

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsapp.adapter.ContactDetailAdapter
import com.example.contactsapp.adapter.onEditClickListener
import com.example.contactsapp.databinding.ActivitySyncContactBinding
import com.example.contactsapp.model.ResponseModel
import com.example.contactsapp.model.User
import com.example.contactsapp.utils.Constants.TAG
import com.example.contactsapp.viewModel.ContactViewModel
import com.example.contactsapp.viewModel.ContactViewModelFactory

class SyncContactActivity : AppCompatActivity() {

    private var _binding: ActivitySyncContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactDetailAdapter

    private val viewModel: ContactViewModel by viewModels {
        ContactViewModelFactory((application as UserApplication).repository)
    }

    private val syncedContacts = mutableSetOf<String>() // Keep track of synced numbers

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            saveContactsToPhone()
        } else {
            Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySyncContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ContactDetailAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.getUserContactDetails()

        viewModel.list.observe(this) { state ->
            when (state) {
                is ResponseModel.Success -> {
                    binding.progress.isVisible = false
                    adapter.submitList(state.data)
                }
                is ResponseModel.Error -> {
                    binding.progress.isVisible = false
                    Toast.makeText(this, "Error loading contacts", Toast.LENGTH_SHORT).show()
                }
                is ResponseModel.Loading -> binding.progress.isVisible = true
            }
        }

        binding.saveButton.setOnClickListener {
            if (hasContactPermissions()) {
                saveContactsToPhone()
            } else {
                requestContactPermissions()
            }
        }

        adapter.setOnClickListener(object : onEditClickListener{
            override fun onClick(email: String, fullName: String, phone: String, position: Int) {

                val uri = getContactUriByPhone(phone)
                if (uri != null) {
                    val editIntent = Intent(Intent.ACTION_EDIT).apply {
                        setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                        putExtra("finishActivityOnSaveCompleted", true)
                    }
                    startActivity(editIntent)
                } else {
                    Toast.makeText(this@SyncContactActivity, "Sync the contact to edit", Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    private fun saveContactsToPhone() {
        val users = adapter.currentList
        var count = 0
        for (user in users) {
            if (!syncedContacts.contains(user.phone)) {
                if (saveContactToPhone(user)) {
                    syncedContacts.add(user.phone)
                    count++
                }
            }
        }


        Toast.makeText(this, "$count contacts synced", Toast.LENGTH_SHORT).show()
    }

    private fun saveContactToPhone(user: User): Boolean {
        return try {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withYieldAllowed(true)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, user.fullName)
                    .withYieldAllowed(true)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, user.phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .withYieldAllowed(true)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, user.email)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .withYieldAllowed(true)
                    .build()
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: Exception) {
                // Display a warning
                val txt: String = getString(R.string.contactCreationFailure)
                Toast.makeText(applicationContext, txt, Toast.LENGTH_SHORT).show()

                // Log exception
                Log.e(TAG, "Exception encountered while inserting contact: $e")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun hasContactPermissions(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
        val writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)
        return readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS
            )
        )
    }

    private fun getContactUriByPhone(phone: String): Uri? {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val selectionArgs = arrayOf(phone)

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val lookupKey = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY))
                return ContactsContract.Contacts.getLookupUri(contactId, lookupKey)
            }
        }

        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

package com.example.contactsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListView
import android.widget.SimpleCursorAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.contactsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val REQUEST_READ_CONTACTS = 100
        private const val CONTACT_LOADER_ID = 1
    }

    private lateinit var contactsListView: ListView
    private lateinit var cursorAdapter: SimpleCursorAdapter

    private val FROM_COLUMNS = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    private val TO_IDS = intArrayOf(android.R.id.text1)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contactsListView = binding.contactsList

        cursorAdapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_list_item_1,
            null,
            FROM_COLUMNS,
            TO_IDS,
            0
        )

        contactsListView.adapter = cursorAdapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS
            )
        } else {
            LoaderManager.getInstance(this).initLoader(CONTACT_LOADER_ID, null, this)
        }

        binding.sync.setOnClickListener {
            val intent = Intent(this@MainActivity,SyncContactActivity::class.java)
            startActivity(intent)
        }

        binding.add.setOnClickListener {
            val intent = Intent(this@MainActivity,AddContactActivity::class.java)
            startActivity(intent)
        }

        contactsListView.setOnItemClickListener { parent, view, position, id ->
            val cursor = cursorAdapter.getItem(position) as Cursor
            val contactId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))

            val uri = ContactsContract.Contacts.getLookupUri(contactId, cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)))

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
            }

            startActivity(intent)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            LoaderManager.getInstance(this).initLoader(CONTACT_LOADER_ID, null, this)
        } else {
            // Handle permission denied
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )

        return CursorLoader(
            this,
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        cursorAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        cursorAdapter.swapCursor(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

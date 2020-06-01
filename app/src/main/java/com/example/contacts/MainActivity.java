package com.example.contacts;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
RecyclerView recycle;
ArrayList<ContactDetails> details=new ArrayList<>();
    Account account[];

    final String[] projections={ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        account= AccountManager.get(this).getAccounts();
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS},0);
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_CONTACTS},0);
        }
        Cursor cursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projections,
                null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        if (cursor != null && cursor.getCount() >= 0) {
            while (cursor.moveToNext()) {
                String name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String id=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                Cursor cursor1=getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        new String[] {ContactsContract.RawContacts.ACCOUNT_NAME,ContactsContract.RawContacts.ACCOUNT_TYPE},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ? ",new String[] {id}
                        ,null);
                if (cursor1.moveToFirst()) {
                    String account=cursor1.getString(cursor1.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                    String type=cursor1.getString(cursor1.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                    details.add(new ContactDetails(name,number,account+":"+type,id));
                }
                cursor1.close();
            }
            cursor.close();
        }
        ContactsAdapter ada=new ContactsAdapter(details,this);
        recycle=findViewById(R.id.recyclerView);
        recycle.setAdapter(ada);
        recycle.setLayoutManager(new LinearLayoutManager(this));
        recycle.setHasFixedSize(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Cursor cursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,projections,
                null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
        if (cursor != null && cursor.getCount() >= 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                copyToAccount(item.getItemId(),name,number);
            }
            cursor.close();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i=0;i<account.length;i++)
            menu.add(0,i,0,i+": "+account[i].name);
        return super.onCreateOptionsMenu(menu);
    }

    public void copyToAccount(int id,String name,String number) {
        ArrayList<ContentProviderOperation> ops=new ArrayList<>();
        Log.d("check",account[id].name+" : "+account[id].type);
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,account[id].name)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,account[id].type)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,name)
                .build());
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());

        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
        } catch (Exception e) {
            Log.d("check",""+e);
        }
    }
}

package com.example.kishorebaktha.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static int SIGN_IN_CODE=1;
    private FirebaseListAdapter<ChatMessage> Adapter;
    RelativeLayout activity_main;
     Button send;
       int count=0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_id)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main,"Successfully signed out",Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main=(RelativeLayout)findViewById(R.id.Activity_main);
         send=(Button)findViewById(R.id.button);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et=(EditText)findViewById(R.id.editText);
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(et.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                et.setText("");
            }
        });
        //check if signed in else navigate to sign in page
        if(FirebaseAuth.getInstance().getCurrentUser()==null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_CODE);
        }
        else
        {
            //feedback
            Snackbar.make(activity_main,"Welcome"+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            //load content
            displaychatMessage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==SIGN_IN_CODE&&resultCode==RESULT_OK)
        {
            Snackbar.make(activity_main,"Successfully signed in",Snackbar.LENGTH_SHORT).show();
            displaychatMessage();
        }
        else
        {
            Snackbar.make(activity_main,"Couldn't singed in...try again later",Snackbar.LENGTH_SHORT).show();
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displaychatMessage()
    {
        final ListView message=(ListView)findViewById(R.id.listView);
        Adapter=new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class,R.layout.list_item,FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position)
            {
                  //get references to views of list_item.xml
                TextView MessageText,MessageUser,MessageTime;
                MessageText=(TextView)v.findViewById(R.id.messagetext);
                MessageUser=(TextView)v.findViewById(R.id.messageuser);
                MessageTime=(TextView)v.findViewById(R.id.messagetime);
                MessageText.setText(model.getMessageText());
                MessageUser.setText(model.getMessageUser());
                MessageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",model.getMessageTime()));
            }
        };
        message.setAdapter(Adapter);
        message.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                remove(position);
            }
        });
    }
    public void remove(final int pos)
    {
        FirebaseDatabase.getInstance().getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if(pos==count)
                            {
                                snapshot.getRef().removeValue();
                                Toast.makeText(getApplicationContext(),"Deleted",Toast.LENGTH_SHORT).show();
                                break;
                            }
                             count++;
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        count=0;
    }
}

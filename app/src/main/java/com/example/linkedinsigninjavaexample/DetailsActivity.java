package com.example.linkedinsigninjavaexample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String linkedinId = getIntent().getStringExtra("linkedin_id");
        String linkedinFirstName = getIntent().getStringExtra("linkedin_first_name");
        String linkedinLastName = getIntent().getStringExtra("linkedin_last_name");
        String linkedinEmail = getIntent().getStringExtra("linkedin_email");
        String linkedinProfilePicURL = getIntent().getStringExtra("linkedin_profile_pic_url");
        String linkedinAccessToken = getIntent().getStringExtra("linkedin_access_token");

        TextView linkedinIdTextView = findViewById(R.id.linkedin_id_textview);
        TextView linkedinFirstNameTextView = findViewById(R.id.linkedin_first_name_textview);
        TextView linkedinLastNameTextView = findViewById(R.id.linkedin_last_name_textview);
        TextView linkedinEmailTextView = findViewById(R.id.linkedin_email_textview);
        TextView linkedinProfilePicUrlTextView = findViewById(R.id.linkedin_profile_pic_url_textview);
        TextView linkedinAccessTokenTextView = findViewById(R.id.linkedin_access_token_textview);

        linkedinIdTextView.setText(linkedinId);
        linkedinFirstNameTextView.setText(linkedinFirstName);
        linkedinLastNameTextView.setText(linkedinLastName);
        linkedinEmailTextView.setText(linkedinEmail);


        if (linkedinProfilePicURL != null) {
            if (linkedinProfilePicURL.equals("")) {
                linkedinProfilePicUrlTextView.setText("Not Exist");
            } else {
                linkedinProfilePicUrlTextView.setText(linkedinProfilePicURL);
            }
        }
        linkedinAccessTokenTextView.setText(linkedinAccessToken);

    }
}

package com.example.linkedinsigninjavaexample;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.linkedinsigninjavaexample.model.LinkedInEmailModel;
import com.example.linkedinsigninjavaexample.model.LinkedInProfileModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity {


    String linkedinAuthURLFull;
    Dialog linkedIndialog;
    String linkedinCode;

    String id = "";
    String firstName = "";
    String lastName = "";
    String email = "";
    String profilePicURL = "";
    String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String state = "linkedin" + TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis());

        linkedinAuthURLFull = LinkedInConstants.AUTHURL + "?response_type=code&client_id=" + LinkedInConstants.CLIENT_ID + "&scope=" + LinkedInConstants.SCOPE + "&state=" + state + "&redirect_uri=" + LinkedInConstants.REDIRECT_URI;

        Button linkedinLoginBtn = findViewById(R.id.linkedin_login_btn);
        linkedinLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupLinkedinWebviewDialog(linkedinAuthURLFull);
            }
        });
    }

    public void setupLinkedinWebviewDialog(String url) {
        linkedIndialog = new Dialog(this);
        WebView webView = new WebView(this);
        //webView.isVerticalScrollBarEnabled();
        // webView.isHorizontalScrollBarEnabled();
        webView.setWebViewClient(new LinkedInWebViewClient());
        webView.getSettings().getJavaScriptEnabled();
        webView.loadUrl(url);
        linkedIndialog.setContentView(webView);
        linkedIndialog.show();
    }

    // A client to know about WebView navigations
    class LinkedInWebViewClient extends WebViewClient {

        // For API 21 and above
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().startsWith(LinkedInConstants.REDIRECT_URI)) {
                handleUrl(request.getUrl().toString());

                // Close the dialog after getting the authorization code
                if (request.getUrl().toString().contains("?code=")) {
                    linkedIndialog.dismiss();
                }
                return true;
            }
            return false;
        }


        // For API 19 and below
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(LinkedInConstants.REDIRECT_URI)) {
                handleUrl(url);

                // Close the dialog after getting the authorization code
                if (url.contains("?code=")) {
                    linkedIndialog.dismiss();
                }
                return true;
            }
            return false;
        }

        // Check webview url for access token code or error
        private void handleUrl(String url) {
            Uri uri = Uri.parse(url);
            if (url.contains("code")) {
                linkedinCode = uri.getQueryParameter("code");
                LinkedInRequestForAccessToken task = new LinkedInRequestForAccessToken(MainActivity.this, linkedinCode);
                task.execute();
            } else if (url.contains("error")) {
                String error = uri.getQueryParameter("error");
                if (error != null) {
                    Log.e("Error: ", error);
                }
            }
        }
    }

    private static class LinkedInRequestForAccessToken extends AsyncTask<Void, Void, String> {

        private String postParams;
        private WeakReference<MainActivity> activityReference;

        LinkedInRequestForAccessToken(MainActivity context, String authCode) {
            activityReference = new WeakReference<>(context);
            String grantType = "authorization_code";
            postParams = "grant_type=" + grantType + "&code=" + authCode + "&redirect_uri=" + LinkedInConstants.REDIRECT_URI + "&client_id=" + LinkedInConstants.CLIENT_ID + "&client_secret=" + LinkedInConstants.CLIENT_SECRET;
        }


        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(LinkedInConstants.TOKENURL);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                );
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                outputStreamWriter.write(postParams);
                outputStreamWriter.flush();


                InputStream inputStream;
                // get stream
                if (httpsURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = httpsURLConnection.getInputStream();
                } else {
                    inputStream = httpsURLConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder response = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    response.append(temp);
                }
                JSONObject jsonObject = (JSONObject) new JSONTokener(response.toString()).nextValue();
                String accessToken = jsonObject.getString("access_token"); //The access token
                Log.e("accessToken is: ", accessToken);

                Integer expiresIn = jsonObject.getInt("expires_in"); //When the access token expires
                Log.e("expires in: ", String.valueOf(expiresIn));
                return accessToken;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            FetchLinkedInUserProfile task = new FetchLinkedInUserProfile(activity, result);
            task.execute();
        }
    }

    private static class FetchLinkedInUserProfile extends AsyncTask<Void, Void, LinkedInProfileModel> {
        String tokenURLFull;
        String token;
        private WeakReference<MainActivity> activityReference;

        FetchLinkedInUserProfile(MainActivity context, String accToken) {
            activityReference = new WeakReference<>(context);
            tokenURLFull = "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,profilePicture(displayImage~:playableStreams))&oauth2_access_token=" + accToken;
            token = accToken;
        }

        @Override
        protected LinkedInProfileModel doInBackground(Void... voids) {
            try {
                URL url = new URL(tokenURLFull);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(false);
                InputStream inputStream;
                // get stream
                if (httpsURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = httpsURLConnection.getInputStream();
                } else {
                    inputStream = httpsURLConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder response = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    response.append(temp);
                }
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(response.toString(), LinkedInProfileModel.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkedInProfileModel model) {
            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            Log.d("LinkedIn Access Token: ", token);
            activity.accessToken = token;

            // LinkedIn Id
            String linkedinId = model.getId();
            Log.d("LinkedIn Id: ", linkedinId);
            activity.id = linkedinId;

            // LinkedIn First Name
            String linkedinFirstName = model.getFirstName().getLocalized().getEnUs();
            Log.d("LinkedIn First Name: ", linkedinFirstName);
            activity.firstName = linkedinFirstName;

            // LinkedIn Last Name
            String linkedinLastName = model.getLastName().getLocalized().getEnUs();
            Log.d("LinkedIn Last Name: ", linkedinLastName);
            activity.lastName = linkedinLastName;

            // LinkedIn Profile Picture URL
            /*
                 Change row of the 'elements' array to get diffrent size of the profile pic
                 elements[0] = 100x100
                 elements[1] = 200x200
                 elements[2] = 400x400
                 elements[3] = 800x800
            */

            String linkedinProfilePic = model.getProfilePicture().getDisplayImage().getElements().get(2).getIdentifiers().get(0).getIdentifier();
            Log.d("LinkedIn Profile URL: ", linkedinProfilePic);
            activity.profilePicURL = linkedinProfilePic;

            // Get user's email address
            FetchLinkedInEmailAddress task = new FetchLinkedInEmailAddress(activity, token);
            task.execute();
        }
    }

    private static class FetchLinkedInEmailAddress extends AsyncTask<Void, Void, LinkedInEmailModel> {
        String tokenURLFull;
        String token;
        private WeakReference<MainActivity> activityReference;

        FetchLinkedInEmailAddress(MainActivity context, String accToken) {
            activityReference = new WeakReference<>(context);
            tokenURLFull = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))&oauth2_access_token=" + accToken;
            token = accToken;
        }

        @Override
        protected LinkedInEmailModel doInBackground(Void... voids) {
            try {
                URL url = new URL(tokenURLFull);
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoInput(true);
                httpsURLConnection.setDoOutput(false);
                InputStream inputStream;
                // get stream
                if (httpsURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = httpsURLConnection.getInputStream();
                } else {
                    inputStream = httpsURLConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp;
                StringBuilder response = new StringBuilder();
                while ((temp = bufferedReader.readLine()) != null) {
                    response.append(temp);
                }
                Gson gson = new GsonBuilder().create();
                return gson.fromJson(response.toString(), LinkedInEmailModel.class);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkedInEmailModel model) {
            // get a reference to the activity if it is still there
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            // LinkedIn Email
            String linkedinEmail = model.getElements().get(0).getHandle().getEmailAddress();
            Log.d("LinkedIn Email: ", linkedinEmail);
            activity.email = linkedinEmail;

            activity.openDetailsActivity();
        }
    }


    public void openDetailsActivity() {
        Intent myIntent = new Intent(this, DetailsActivity.class);
        myIntent.putExtra("linkedin_id", id);
        myIntent.putExtra("linkedin_first_name", firstName);
        myIntent.putExtra("linkedin_last_name", lastName);
        myIntent.putExtra("linkedin_email", email);
        myIntent.putExtra("linkedin_profile_pic_url", profilePicURL);
        myIntent.putExtra("linkedin_access_token", accessToken);
        startActivity(myIntent);
    }

}

package com.tbc_hackathon.dailyhunt.korbis_2;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaredrummler.android.device.DeviceName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Inflater;

import android.Manifest;
import android.widget.Toast;


public class GetTagsActivity extends AppCompatActivity implements LocationListener {

    Uri imageUri;
    LocationManager mLocationManager;
    EditText tag;
    LinearLayout parent;
    Button addBtn;
    TextView tagView;
    String tags="";
    HashMap<String, String> implicitTags = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_tags);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        imageUri = Uri.parse(getIntent().getStringExtra("imageUri"));
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Obtaining location


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    11);
            Log.e("LocError","Location permissions error");
            return;
        }
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null)
        {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            //implicitTags.put("address",addresses.get(0).getAddressLine(0));// If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            implicitTags.put("City",addresses.get(0).getLocality()); Log.e("City",implicitTags.get("City"));
            implicitTags.put("State",addresses.get(0).getAdminArea()); Log.e("State",implicitTags.get("State"));
            implicitTags.put("Country",addresses.get(0).getCountryName()); Log.e("Country",implicitTags.get("Country"));
            implicitTags.put("Postal Code",addresses.get(0).getPostalCode());
        }
        else
        {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        //Obtaining Device details
        implicitTags.put("Device name", DeviceName.getDeviceName());

        //Obtaining time of capture
        Date d = new Date();
        implicitTags.put("Capture time", DateFormat.format("EEEE,MMMM-d,yyyy,hh:mm:ss", d.getTime()).toString());

        //Obtaining hour of the day
        implicitTags.put("Hour of capture",HourOfTheDayUtility.get());

        Log.e("See","Before population");
        populateImplicitTags();

        addBtn = (Button)parent.findViewById(R.id.tag_addBtn);
        tagView = (TextView)parent.findViewById(R.id.tags_tv);
        tag = (EditText)parent.findViewById(R.id.tag_editT);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tags+=tag.getText().toString()+" ";
                tagView.setText(tagView.getText()+" #"+tag.getText().toString());
                tag.setText("");
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(GetTagsActivity.this,"Clicked submit!!",Toast.LENGTH_SHORT).show();
            Iterator it = implicitTags.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                if(pair.getValue()!=null && !pair.getValue().equals("null"))
                {
                    tags+=pair.getValue().toString()+" ";
                }
            }

            new ServerUpload().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateImplicitTags()
    {
        Iterator it = implicitTags.entrySet().iterator();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parent = (LinearLayout)findViewById(R.id.parentView);

        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();
            View custom = inflater.inflate(R.layout.tag_card, null);
            TextView keyView = (TextView) custom.findViewById(R.id.keyView);
            TextView valView = (TextView) custom.findViewById(R.id.valView);
            keyView.setText(pair.getKey().toString());
            valView.setText( pair.getValue().toString());
            parent.addView(custom);
            Log.d(pair.getKey().toString(),pair.getValue().toString());
        }

        parent.addView(inflater.inflate(R.layout.tags_add_view,null));

    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location != null) {
            Log.d("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
    }
    @Override
    public void onProviderDisabled(String s) {
    }


    class ServerUpload extends AsyncTask<Void, Integer, String>
    {

        long totalSize=0;
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible

        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Configure.FILE_UPLOAD_URL);

            try {
                MultipartEntity entity = new MultipartEntity();



                // Adding file data to http body
                entity.addPart("photo", new FileBody(new File(imageUri.getPath())));

                // Extra parameters if you want to pass to server
                entity.addPart("tags",
                        new StringBody(tags));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("onPostExecute", "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }
    }




}

package com.swen900014.orange.rideshareoz;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by uidu9665 on 6/09/2015.
 */

public class MyRidesFragment extends Fragment {

    private RidesAdaptor mRidesAdapter;

    private Bundle savedInstanceState;
    public MyRidesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //if (savedInstanceState == null) {
            inflater.inflate(R.menu.myridesfragment, menu);
        //}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchRidesTask ridesTask = new FetchRidesTask();
            ridesTask.execute("http://144.6.226.237/ride/getall");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        Ride[] data = {new Ride(Ride.RideState.VIEWING),
                new Ride(Ride.RideState.JOINED),
                new Ride(Ride.RideState.OFFERING),
                new Ride(Ride.RideState.VIEWING),
                new Ride(Ride.RideState.OFFERING),
                new Ride(Ride.RideState.JOINED),
                new Ride(Ride.RideState.OFFERING),
                new Ride(Ride.RideState.JOINED),
                new Ride(Ride.RideState.VIEWING),
                new Ride(Ride.RideState.OFFERING)
        };
        List<Ride> currentRides = new ArrayList<Ride>(Arrays.asList(data));

        // Now that we have some dummy  data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy data) and
        // use it to populate the ListView it's attached to.
        mRidesAdapter = new RidesAdaptor(getActivity(), (ArrayList<Ride>)currentRides);


        View rootView = inflater.inflate(R.layout.fragment_myrides, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_myrides);
        listView.setAdapter(mRidesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent;
                Ride selectedRide = mRidesAdapter.getItem(position);
                if (selectedRide.getRideState().equals(Ride.RideState.OFFERING)){
                    intent = new Intent(getActivity(), DriverViewRideActivity.class);
                }else{
                    intent = new Intent(getActivity(), PassViewRideActivity.class);
                }

                intent.putExtra("SelectedRide", selectedRide);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public class FetchRidesTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchRidesTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String ridesJsonStr = null;

            try {
                // Construct the URL for the Rides query
                final String RIDES_BASE_URL = "http://144.6.226.237/ride/getall";//R.string.all_rides_url;


                URL url = new URL(RIDES_BASE_URL);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                ridesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the  data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

                return ridesJsonStr ;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

                try {
                    ArrayList<Ride> serverRides = Ride.fromJson(new JSONArray(result));
                    mRidesAdapter.clear();
                    for(Ride listItemRide : serverRides) {
                        mRidesAdapter.add(listItemRide);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // New data is back from the server.  Hooray!
            }
        }
    }
}
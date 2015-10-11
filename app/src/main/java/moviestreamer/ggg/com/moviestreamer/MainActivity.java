package moviestreamer.ggg.com.moviestreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerOrderBy;
    private GridView gridViewThumnails;
    public ArrayAdapter<String> spinnerAdapter;
    private String spinnerOrderByArray[];
    public ImageAdapter imageAdapter;
    private JSONArray movieDataJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerOrderByArray = new String[]{ "Order By Popularity", "Order By Rating"};
        spinnerOrderBy = (Spinner) findViewById(R.id.spinnerOrderBy);
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerOrderByArray);
        spinnerOrderBy.setAdapter(spinnerAdapter);
        spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) findViewById(R.id.spinnerOrderBy);
                if (spinner.getSelectedItem().toString() == "Order By Rating") {
                    getMovieData("vote_average");
                } else {
                    getMovieData("popularity");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        gridViewThumnails = (GridView) findViewById(R.id.gridViewThumbnails);
        gridViewThumnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("GridView OnItemClick", "Selected: " + position);
                if (movieDataJsonArray != null) {
                    Log.d("GridView", "MovieDataJsonArray not null");
                    try {
                        JSONObject tmpObj = movieDataJsonArray.getJSONObject(position);
                        if (tmpObj != null) {

                            Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                            intent.putExtra("movieJsonString", tmpObj.toString());
                            Log.d("GridView", tmpObj.toString());
                            startActivity(intent);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        gridViewThumnails.setAdapter(imageAdapter = new ImageAdapter(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean getMovieData(String orderBy){
        if(!isNetworkAvailable()) {
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return false; //bail if there is no network
        }
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute(orderBy);
        return true;
    }
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService
                (Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    /***
     *
     * Fetch Movies Via Async Task
     *
     */

    public class FetchMoviesTask extends AsyncTask<String, Void, String>{
        private JSONArray movieArray;
            @Override
            protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;

            try{
                URL url = new URL("http://api.themoviedb.org/3/discover/movie?sort_by=" +params[0]+
                        ".desc&api_key="+getString(R.string.picasso_api_key)+"&language=en");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    //do nothing
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line + '\n');
                }
                if(buffer.length() == 0){
                    //stream empty, do nothing
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e){
                Log.e("mainactivity", "Error", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MainActivity", "Error closing stream", e);
                    }
                }
            }

            try {
                getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e){
                e.printStackTrace();
            }
            return movieJsonStr;
        }
        public void getMovieDataFromJson(String jsonString) throws JSONException {
            JSONObject movies = new JSONObject(jsonString);
            movieArray = movies.getJSONArray("results");

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            movieDataJsonArray = movieArray;
            imageAdapter.updateURLs(movieArray);
        }
    }
}

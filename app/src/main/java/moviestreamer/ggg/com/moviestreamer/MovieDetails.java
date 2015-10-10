package moviestreamer.ggg.com.moviestreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieDetails extends AppCompatActivity {

    TextView year, length, title, synopsis, rating;
    ImageView poster;
    String posterURL, backdropPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        year = (TextView)findViewById(R.id.year);
        title = (TextView)findViewById(R.id.title);
        synopsis = (TextView)findViewById(R.id.synopsis);
        rating = (TextView)findViewById(R.id.rating);
        poster = (ImageView)findViewById(R.id.poster);

        Intent intent = getIntent();
        String jsonString = getIntent().getStringExtra("movieJsonString");
        Log.d("JasonString from Intent", jsonString);
        try{
            JSONObject movie = new JSONObject(jsonString);
            Log.d("Grab yearJSON OBject", movie.getString("release_date"));
            if(movie.getString("release_date") == "null") {
                year.setText("TBA");
            } else {
                year.setText(movie.getString("release_date").substring(0, 4));
            }
            title.setText(movie.getString("original_title"));
            if(movie.getString("overview") == "null"){
                synopsis.setText("Synopsis: TBA");
            } else {
                synopsis.setText(movie.getString("overview"));
            }
            if(movie.getString("vote_average") == "null") {
                rating.setText("Rating: TBA");
            } else {
                rating.setText(movie.getString("vote_average") + "/10");
            }
            posterURL = "http://image.tmdb.org/t/p/w185" + movie.getString("poster_path");
            backdropPath = "http://image.tmdb.org/t/p/w300" + movie.getString("backdrop_path");
            poster = (ImageView) findViewById(R.id.poster);
            Picasso.with(poster.getContext())
                    .load(posterURL)
                    .placeholder(R.drawable.ic_hourglass128)
                    .error(R.drawable.ic_404)
                    .into(poster);
        } catch (JSONException e){
            e.printStackTrace();
        }

    }

}

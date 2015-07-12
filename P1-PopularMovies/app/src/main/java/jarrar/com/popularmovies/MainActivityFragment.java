package jarrar.com.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    View rootView;
    private MoviesAdapter moviesAdapter;
    private ArrayList<Movie> moviesList;
    SharedPreferences sharedPreferences;
    public final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    GridView gridViewMovies;
    Boolean allowUpdate = true;
    String SORT_BY;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SORT_BY = sharedPreferences.getString("sortBy", "popularity");
        moviesList = new ArrayList<Movie>();
        if (savedInstanceState == null || !savedInstanceState.containsKey("movies")) {
        } else {
            moviesList = savedInstanceState.getParcelableArrayList("movies");
            allowUpdate = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("movies", moviesList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridViewMovies = (GridView) rootView.findViewById(R.id.movies_list_gridView);
        moviesAdapter = new MoviesAdapter(getActivity(), moviesList);
        gridViewMovies.setAdapter(moviesAdapter);

        gridViewMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), DetailsActivity.class);
                i.putExtra("Movie", moviesList.get(position));
                startActivity(i);
            }
        });

        if (allowUpdate)
            new requestMoviesTask().execute();
        return rootView;

    }

    public class requestMoviesTask extends AsyncTask<Void, Void, Void> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String moviesJsonStr = null;

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(LOG_TAG, "Requesting movies List..");
            Uri builtUri = Uri.parse(getString(R.string.BASE_URL)).buildUpon()
                    .appendQueryParameter(getString(R.string.API_KEY_PARAM), getString(R.string.API_KEY))
//                    .appendQueryParameter(Utility.APPEND_TO_RESPONSE_PARAM, Utility.MY_REQUEST_PARAMS)
                    .appendQueryParameter(getString(R.string.LANGUAGE_PARAM), getString(R.string.LANGUAGE_EN_PARAM))
                    .appendQueryParameter(getString(R.string.SORT_PARAM), getString(R.string.POPULARITY_PARAM) + getString(R.string.SORT_DESC_PARAM))
                    .build();

            try {
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty
                }
                moviesJsonStr = buffer.toString();
                extractFromJSON(moviesJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error ", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ValidateSort();
            moviesAdapter.notifyDataSetChanged();
        }
    }

    private void extractFromJSON(String jsonStr) {

        try {
            JSONObject moviesJson = new JSONObject(jsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray("results");

            for (int i = 0; i < moviesArray.length(); i++) {
                Movie tempMovie = new Movie();
                JSONObject jo = moviesArray.getJSONObject(i);
                tempMovie.setTitle(jo.getString(getString(R.string.ORIGIONAL_TITLE_PARAM)));
                tempMovie.setPoster_Location(jo.getString(getString(R.string.POSTER_PATH_PARAM)));
                tempMovie.setBackdrop_path(jo.getString(getString(R.string.BACKDROP_PATH_PARAM)));
                tempMovie.setOverview(jo.getString(getString(R.string.OVERVIEW_PARAM)));
                tempMovie.setRelease_Date(jo.getString(getString(R.string.RELEASE_DATE_PARAM)));
                tempMovie.setRating(jo.getString(getString(R.string.RATING_PARAM)));
                tempMovie.setPopularity(jo.getString(getString(R.string.POPULARITY_PARAM)));
                moviesList.add(tempMovie);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainActivity.prefChanged) {
            SORT_BY = sharedPreferences.getString("sortBy", "popularity");
            ValidateSort();
            MainActivity.prefChanged = false;
        }
    }

    public void ValidateSort() {

        if (SORT_BY.equalsIgnoreCase(getString(R.string.RATING_PARAM))) {
            Collections.sort(moviesList, Movie.RATE_COMPARATOR);
        } else {
            Collections.sort(moviesList, Movie.POP_COMPARATOR);
        }
        moviesAdapter.notifyDataSetChanged();
        moviesAdapter.notifyDataSetInvalidated();
    }
}
package jarrar.com.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {

    View rootView;
    Intent intent;
    Movie movie;
    String roundedRate;

    public DetailsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getActivity().getIntent();
        movie = intent.getParcelableExtra("Movie");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_details, container, false);
        ImageView image_backdrop = (ImageView) rootView.findViewById(R.id.imageView_backdrop);
        ImageView image_poster = (ImageView) rootView.findViewById(R.id.imageView_poster);
        TextView text_title = (TextView) rootView.findViewById(R.id.textView_movie_title);
        TextView text_releaseDate = (TextView) rootView.findViewById(R.id.textView_release_date);
        TextView text_rating = (TextView) rootView.findViewById(R.id.textView_rating);
        TextView text_overview = (TextView) rootView.findViewById(R.id.textView_overview);

        Picasso.with(getActivity()).load(getString(R.string.BASE_URL_DROPBACK) + movie.backdrop_path)
                .into(image_backdrop);
        Picasso.with(getActivity()).load(getString(R.string.BASE_URL_POSTER) + movie.poster_Location)
                .into(image_poster);
        text_title.setText(movie.title);
        text_releaseDate.setText(movie.release_Date);
        roundedRate = String.valueOf(Math.round(Float.parseFloat(movie.rating)));
        text_rating.setText(roundedRate + "/10");
        text_overview.setText(movie.overview);

        return rootView;
    }
}

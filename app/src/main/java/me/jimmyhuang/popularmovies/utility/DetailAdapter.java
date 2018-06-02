package me.jimmyhuang.popularmovies.utility;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import me.jimmyhuang.popularmovies.MainActivity;
import me.jimmyhuang.popularmovies.R;
import me.jimmyhuang.popularmovies.model.Movie;
import me.jimmyhuang.popularmovies.model.Review;
import me.jimmyhuang.popularmovies.model.Trailer;

import static me.jimmyhuang.popularmovies.utility.NetworkUtil.buildPosterUrl;


// https://stackoverflow.com/questions/40683817/how-to-set-two-adapters-to-one-recyclerview?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
public class DetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_TYPE_MOVIE = 1;
    private static final int ITEM_TYPE_TRAILER = 2;
    private static final int ITEM_TYPE_REVIEW = 3;

    private List<Object> mItems;

    private final View.OnClickListener mTralerOnClickListener = new TrailerClickListener();

    private Context mContext;

    private RecyclerView mRecyclerView;

    public DetailAdapter(List<Object> items) {
        this.mItems = items;
    };

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
        super.onAttachedToRecyclerView(rv);

        mRecyclerView = rv;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (viewType == ITEM_TYPE_MOVIE) {
            View view = inflater.inflate(R.layout.adapter_detail, parent, false);

            return new MovieViewHolder(view);
        } else if (viewType == ITEM_TYPE_TRAILER) {
            View view = inflater.inflate(R.layout.adapter_trailer, parent, false);

            return new TrailerViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.adapter_review, parent, false);

            return new ReviewViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = mItems.get(position);

        if (holder instanceof MovieViewHolder) {
            ((MovieViewHolder) holder).bind((Movie) item);
        } else if (holder instanceof TrailerViewHolder) {
            ((TrailerViewHolder) holder).bind((Trailer) item);
        } else {
            ((ReviewViewHolder) holder).bind((Review) item);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) instanceof Movie) {
            return ITEM_TYPE_MOVIE;
        } else if (mItems.get(position) instanceof Trailer) {
            return ITEM_TYPE_TRAILER;
        } else {
            return ITEM_TYPE_REVIEW;
        }
    }

    private static class MovieViewHolder extends RecyclerView.ViewHolder {

        private ImageView mPosterImage;
        private TextView mTitleText;
        private TextView mReleaseText;
        private TextView mRatingText;
        private TextView mOverviewText;

        public MovieViewHolder(View itemView) {
            super(itemView);

            mPosterImage = itemView.findViewById(R.id.detail_poster_iv);
            mTitleText = itemView.findViewById(R.id.detail_title_tv);
            mReleaseText = itemView.findViewById(R.id.detail_release_tv);
            mRatingText = itemView.findViewById(R.id.detail_rating_tv);
            mOverviewText = itemView.findViewById(R.id.detail_overview_tv);
        }

        public void bind(Movie movie) {
            String posterUrl = movie.getPosterPath();
            if (!posterUrl.isEmpty()) {

                Picasso.Builder builder = new Picasso.Builder(itemView.getContext());
                builder.listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        Log.e("URL", exception.toString());
                    }
                });
                builder.build().with(itemView.getContext()).load(buildPosterUrl(posterUrl).toString()).fit().into(mPosterImage);
            }

            mTitleText.setText(movie.getTitle());
            mReleaseText.setText(movie.getReleaseDate());
            mRatingText.setText(String.valueOf(movie.getVoteAverage()));
            mOverviewText.setText(movie.getOverview());
        }
    }

    private class TrailerViewHolder extends RecyclerView.ViewHolder {

        private final TextView mVideoText;

        public TrailerViewHolder(View itemView) {
            super(itemView);

            mVideoText = itemView.findViewById(R.id.trailer_tv);
            itemView.setOnClickListener(mTralerOnClickListener);
        }

        public void bind(Trailer trailer) {
            if (trailer != null) {
                String name = trailer.getName();
                mVideoText.setText(name);
            }
        }
    }

    private static class ReviewViewHolder extends RecyclerView.ViewHolder {

        private final TextView mAuthorText;
        private final TextView mReviewText;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            mAuthorText = itemView.findViewById(R.id.author_tv);
            mReviewText = itemView.findViewById(R.id.review_tv);
        }

        public void bind(Review review) {
            if (review != null) {
                String author = review.getAuthor();
                String reviewContent = review.getReview();
                mAuthorText.setText(author);
                mReviewText.setText(reviewContent);
            }
        }
    }

    // https://developer.android.com/training/basics/intents/sending
    private void videoIntent(String id) {
        Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + id));

        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(videoIntent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (activities.size() > 0) {
            mContext.startActivity(videoIntent);
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.video_error), Toast.LENGTH_LONG).show();
        }
    }

    private class TrailerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildLayoutPosition(v);
            Object obj = mItems.get(position);
            if (obj instanceof Trailer) {
                Trailer trailer = (Trailer) obj;
                videoIntent(trailer.getKey());
            }
        }
    }
}

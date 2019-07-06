package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Tweet> mTweets;
    private Context context;
    private static RecyclerViewClickListener rvListen;
    private TwitterClient client;

    // pass in the Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets, TweetAdapter.RecyclerViewClickListener rvListen) {
        mTweets = tweets;
        this.rvListen = rvListen;
    }

    //
    @Override
    public int getItemCount() {
        return mTweets.size();
    }


    @Override
    public int getItemViewType(int position) {
        if(mTweets.get(position).mediaURL != "") {
            return 1;
        } else {
            return 0;
        }
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder;
        context = viewGroup.getContext();
        client = TwitterApp.getRestClient(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (i) {
            case 0:
                View v1 = inflater.inflate(R.layout.item_tweet, viewGroup, false);
                viewHolder = new ViewHolder(v1);
                break;
            case 1:
                View v2 = inflater.inflate(R.layout.item_tweet2, viewGroup, false);
                viewHolder = new ViewHolder2(v2);
                break;
            default:
                View v3 = inflater.inflate(R.layout.item_tweet2, viewGroup, false);
                viewHolder = new ViewHolder2(v3);
        }

       return viewHolder;
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        switch (viewHolder.getItemViewType()) {
            case 0:
                ViewHolder vh1 = (ViewHolder) viewHolder;
                final int pos = i;
                configureViewHolder1(vh1, pos);
                break;
            case 1:
                final int pos2 = i;
                ViewHolder2 vh2 = (ViewHolder2) viewHolder;
                configureViewHolder2(vh2, pos2);
                break;
            default:
        }
    }

    private void configureViewHolder1(final ViewHolder viewHolder, int i) {
        // get the data according to position
        final Tweet tweet = mTweets.get(i);

        // populate the views according to this data
        viewHolder.tvUserName.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvAt.setText("@" + tweet.userName);
        viewHolder.tvTimestamp.setText(tweet.relativeTime);
        viewHolder.tvLike.setText("" + tweet.likeCount);
        viewHolder.tvRetweet.setText("" + tweet.retweetCount);
        if(tweet.liked){
            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart));
        } else {
            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
        }

        if(tweet.retweet) {
            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet));
        } else {
            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet_stroke));
        }

        final int pos = i;
        viewHolder.tvBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TweetDetailView.class);
                intent.putExtra("tweet", mTweets.get(pos));
                v.getContext().startActivity(intent);
            }
        });

        viewHolder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.ivRetweet.getDrawable().getConstantState().equals(v.getContext().getResources().getDrawable(R.drawable.ic_vector_retweet_stroke).getConstantState())) {
                    client.sendRetweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet));
                            viewHolder.tvRetweet.setText(Integer.toString(Integer.parseInt(viewHolder.tvRetweet.getText().toString()) +1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Retweeting", "I hate this app.");
                            Log.e("Error", error.toString());
                        }
                    });

                } else {
                    client.undoRetweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet_stroke));
                            viewHolder.tvRetweet.setText(Integer.toString(Integer.parseInt(viewHolder.tvRetweet.getText().toString()) - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Undo Retweet", "I hate this app.");
                            Log.e("Error", error.toString());
                        }
                    });
                }
            }
        });

        viewHolder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.ivLike.getDrawable().getConstantState().equals(v.getContext().getResources().getDrawable(R.drawable.ic_vector_heart_stroke).getConstantState())){
                    client.sendLike(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart));
                            viewHolder.tvLike.setText(Integer.toString(Integer.parseInt(viewHolder.tvLike.getText().toString()) + 1) );
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Liking", "I hate this app." + error.toString());
                        }
                    });

                } else {
                    client.undoLike(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
                            viewHolder.tvLike.setText(Integer.toString(Integer.parseInt(viewHolder.tvLike.getText().toString()) - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Unliking", "I hate this app." + error.toString());
                        }
                    });
                }
            }
        });

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10;
        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .error(R.drawable.ic_launcher)
                .bitmapTransform(new RoundedCornersTransformation(context, radius, margin))
                .into(viewHolder.ivProfileImage);
    }

    private void configureViewHolder2(final ViewHolder2 viewHolder, int i) {
        // get the data according to position
        final Tweet tweet = mTweets.get(i);

        // populate the views according to this data
        viewHolder.tvUserName.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvAt.setText("@" + tweet.userName);
        viewHolder.tvTimestamp.setText(tweet.relativeTime);
        viewHolder.tvLike.setText("" + tweet.likeCount);
        viewHolder.tvRetweet.setText("" + tweet.retweetCount);

        if(tweet.liked){
            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart));
        } else {
            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
        }

        if(tweet.retweet) {
            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet));
        } else {
            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
        }

        final int pos = i;
        viewHolder.tvBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), TweetDetailView.class);
                intent.putExtra("tweet", mTweets.get(pos));
                v.getContext().startActivity(intent);
            }
        });

        viewHolder.ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((viewHolder.ivRetweet.getDrawable()).getConstantState().equals(v.getContext().getResources().getDrawable(R.drawable.ic_vector_retweet_stroke).getConstantState())) {
                    client.sendRetweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet));
                                viewHolder.tvRetweet.setText(Integer.toString(Integer.parseInt(viewHolder.tvRetweet.getText().toString()) + 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Retweeting", "I hate this app.");
                        }
                    });

                } else {
                    client.undoRetweet(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivRetweet.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_retweet_stroke));
                            viewHolder.tvRetweet.setText(Integer.toString(Integer.parseInt(viewHolder.tvRetweet.getText().toString()) - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Undo Retweeting", "I hate this app." + error.toString());
                        }
                    });
                }
            }
        });

        viewHolder.ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.ivLike.getDrawable().getConstantState().equals(v.getContext().getResources().getDrawable(R.drawable.ic_vector_heart_stroke).getConstantState())){
                    client.sendLike(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart));
                            viewHolder.tvLike.setText(Integer.toString(Integer.parseInt(viewHolder.tvLike.getText().toString()) + 1));

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Liking", "I hate this app." + error.toString());
                        }
                    });

                } else {
                    client.undoLike(tweet.uid, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            viewHolder.ivLike.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
                            viewHolder.tvLike.setText(Integer.toString(Integer.parseInt(viewHolder.tvLike.getText().toString()) - 1));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Log.e("Unliking", "I hate this app." + error.toString());
                        }
                    });
                }
            }
        });

        int radius = 30; // corner radius, higher value = more rounded
        int margin = 10;
        Glide.with(context)
                .load(tweet.user.profileImageUrl)
                .error(R.drawable.ic_launcher)
                .bitmapTransform(new RoundedCornersTransformation(context, radius, margin))
                .into(viewHolder.ivProfileImage);

        Glide.with(context)
                .load(tweet.mediaURL)
                .error(R.drawable.ic_launcher)
                .bitmapTransform(new RoundedCornersTransformation(context, radius, margin))
                .override(500, 1000)
                .into(viewHolder.ivMedia);
    }

    // create ViewHolder class

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvAt;
        public TextView tvTimestamp;
        public ImageView ivRetweet;
        public ImageView ivLike;
        public TextView tvLike;
        public TextView tvRetweet;

        public ViewHolder(final View itemView)
        {
            super(itemView);
            itemView.findViewById(R.id.btnReply).setOnClickListener(this);

            // perform findViewById lookups

            ivProfileImage = (ImageView)  itemView.findViewById(R.id.ivProfileImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvAt = itemView.findViewById(R.id.tvAt);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvLike = itemView.findViewById(R.id.tvLike);
            tvRetweet = itemView.findViewById(R.id.tvRetweet);
        }

        @Override
        public void onClick(View v) {
            rvListen.rvListClicked(v, this.getLayoutPosition());
        }
    }

    public static class ViewHolder2 extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvBody;
        public TextView tvAt;
        public TextView tvTimestamp;
        public ImageView ivMedia;
        public ImageView ivRetweet;
        public ImageView ivLike;
        public TextView tvLike;
        public TextView tvRetweet;

        public ViewHolder2(View itemView)
        {
            super(itemView);
            itemView.findViewById(R.id.btnReply).setOnClickListener(this);

            // perform findViewById lookups

            ivProfileImage = (ImageView)  itemView.findViewById(R.id.ivProfileImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvAt = itemView.findViewById(R.id.tvAt);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
            tvLike = itemView.findViewById(R.id.tvLike);
            tvRetweet = itemView.findViewById(R.id.tvRetweet);
        }


        @Override
        public void onClick(View v) {
            rvListen.rvListClicked(v, this.getLayoutPosition());
        }


    }





    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        mTweets.addAll(list);
        notifyDataSetChanged();
    }
    public interface RecyclerViewClickListener {
        public void rvListClicked(View v, int pos);
    }
}

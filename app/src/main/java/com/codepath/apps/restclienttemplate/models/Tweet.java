package com.codepath.apps.restclienttemplate.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Tweet implements Parcelable {

    public void setBody(String body) {
        this.body = body;
    }

    // list out the attributes
    public String body;
    public long uid;        // database ID for the tweet
    public User user;
    public String userName;
    public String relativeTime;
    public String mediaURL;
    public long likeCount;
    public long retweetCount;
    public boolean liked;
    public boolean retweet;


    protected Tweet(Parcel in) {
        body = in.readString();
        uid = in.readLong();
        user = in.readParcelable(User.class.getClassLoader());
        userName = in.readString();
        relativeTime = in.readString();
        mediaURL = in.readString();
        likeCount = in.readLong();
        retweetCount = in.readLong();
        liked = in.readSparseBooleanArray().get(0);
        retweet = in.readSparseBooleanArray().get(0);

    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    public Tweet() {
    }

    // deserialize the JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        // extract the values from the JSON
        tweet.body = jsonObject.getString("full_text");
        tweet.uid = jsonObject.getLong("id");
        tweet.relativeTime = getRelativeTimeAgo(jsonObject.getString("created_at"));
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.userName = tweet.user.screenName;
        tweet.likeCount = jsonObject.getLong("favorite_count");
        tweet.retweetCount = jsonObject.getLong("retweet_count");
        tweet.liked = jsonObject.getBoolean("favorited");
        tweet.retweet = jsonObject.getBoolean("retweeted");
       try {
           JSONArray mediaObjs = jsonObject.getJSONObject("entities").getJSONArray("media");
           tweet.mediaURL = mediaObjs.getJSONObject(0).getString("media_url_https");
       } catch (JSONException e) {
           tweet.mediaURL = "";
       }
        return tweet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeLong(uid);
        dest.writeString(relativeTime);
        dest.writeParcelable(user, flags);
        dest.writeString(userName);
        dest.writeString(mediaURL);
        dest.writeBooleanArray(new boolean[]{liked});
        dest.writeBooleanArray(new boolean[]{liked});
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    private static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }
}

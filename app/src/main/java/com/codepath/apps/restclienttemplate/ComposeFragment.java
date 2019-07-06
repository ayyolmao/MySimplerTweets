package com.codepath.apps.restclienttemplate;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class ComposeFragment extends DialogFragment implements View.OnClickListener {

    private EditText etCompose;
    public Tweet compTweet;
    private MenuItem miActionProgressItem;
    private ComposeFragmentListener listener;
    View view;

    public ComposeFragment () {
    }

    public static ComposeFragment newInstance(String title) {
        ComposeFragment frag = new ComposeFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onClick(View v) {
           // showProgressBar();
            AsyncHttpResponseHandler handler = new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        compTweet = Tweet.fromJSON(response);
                        // Prepare data intent
                        ComposeFragmentListener listener = (ComposeFragmentListener) getActivity();
                        listener.onFinishCompose(compTweet);
                        dismiss();
                    } catch (JSONException e) {
                        Log.e("ERROR", e.toString());
                    }
                    //hideProgressBar();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e("ComposeActivity", "Don't work for shit.");
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    //hideProgressBar();
                }
            };
            TwitterClient client = new TwitterClient(v.getContext());
            EditText etCompose = (EditText) view.findViewById(R.id.etTweet);
            client.sendTweet(etCompose.getText().toString(), handler);
    }

    public interface ComposeFragmentListener {
        void onFinishCompose(Tweet tweet);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_compose, container);
        Button btnCompose = view.findViewById(R.id.btnCompose);
        btnCompose.setOnClickListener(this);
        return view;

    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState){
        if (view != null) {
            super.onViewCreated(view, savedInstanceState);
            // Get field from view
            etCompose = view.findViewById(R.id.etTweet);
            // Fetch arguments from bundle and set title
            String title = getArguments().getString("title", "Composing");
            getDialog().setTitle(title);
            final TextView tvCharCount = view.findViewById(R.id.tvCharCount);

            //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            //getSupportActionBar().setCustomView(R.layout.actionbar_title);
            //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.twitter_blue)));
            etCompose.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Fires right as the text is being changed (even supplies the range of text)
                    tvCharCount.setText(String.valueOf((280 - s.length()) + " characters remaining"));
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // Fires right before text is changing
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Fires right after the text has changed
                }
            });

            // Show soft keyboard automatically and request from focus to field
            etCompose.requestFocus();
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }


    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.Progress_Bar);
        // Extract the action-view from the menu item
        ProgressBar v = (ProgressBar) miActionProgressItem.getActionView();
        // Return to finish
        super.onPrepareOptionsMenu(menu);
    }
}

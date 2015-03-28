package com.unetresgrossebite.myartgallery;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class ArtistActivity extends ActionBarActivity {
    private String dname, artist_id = null;
    final private Boolean debug = false;

    private void qREST() throws JSONException {
        String url = "artists/" + this.dname + "/";
        myRestClient client = new myRestClient();

        if (this.debug) {
            Toast.makeText(ArtistActivity.this, myRestClient.getAbsoluteUrl(url),
                    Toast.LENGTH_SHORT).show();
        }

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Unexpected object received",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    TextView title = (TextView) findViewById(R.id.artist_name);
                    ListView data = (ListView) findViewById(R.id.artist_data);
                    JSONObject artistdata = response.getJSONObject(0);
                    ArrayList itemsReturned = new ArrayList<String>();
                    ArrayAdapter itemsAdapter = null;
                    String tmp = null;

                    if (title == null || data == null) { return; }
                    if (artistdata.has("firstname")) {
                        tmp = renderFirstname(artistdata.getString("firstname")) + " "
                                + renderLastname(artistdata.getString("lastname"));
                    } else {
                        tmp = renderLastname(artistdata.getString("lastname"));
                    }
                    title.setText(tmp);

                    if (artistdata.has("dstart")) {
                        if (artistdata.has("dstop")) {
                            String a, b;
                            a = artistdata.getString("dstart");
                            b = artistdata.getString("dstop");

                            if (a.equals(b)) {
                                tmp = "Active in " + a;
                            }
                            else {
                                tmp = "Active from " + a + " to " + b;
                            }
                        }
                        else {
                            tmp = "Active in " + artistdata.getString("dstart");
                        }
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("priceidx")) {
                        String lookup = artistdata.getString("priceidx");
                        if (new String("growing").equals(lookup)
                            || new String("decreasing").equals(lookup)) {
                            tmp = "Prices are globally " + lookup;
                        } else {
                            tmp = "Mostly sold: " + lookup;
                        }
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("turnover")) {
                        tmp = "Turnover: " + artistdata.getString("turnover");
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("rank")) {
                        tmp = "Rank: " + artistdata.getString("rank");
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("bestcountry")) {
                        if (artistdata.has("bestamount")) {
                            tmp = "Mostly sold in " + artistdata.getString("bestcountry") + " ("
                                    + artistdata.getString("bestamount") + ")";
                        } else {
                            tmp = "Mostly sold in " + artistdata.getString("bestcountry");
                        }
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("id")) {
                        tmp = "Search for related artworks";
                        artist_id = artistdata.getString("id");
                        itemsReturned.add(tmp);
                    }
                    if (debug) {
                        Toast.makeText(getApplicationContext(), itemsReturned.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                    itemsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, itemsReturned);
                    data.setAdapter(itemsAdapter);
                } catch (JSONException e) {
                    String error = "Error parsing server's response [" + e.toString() + "]";
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        this.dname = getIntent().getExtras().getString("dname");

        try {
            qREST();
        } catch (JSONException e) {
            String error = "Error parsing server's response [" + e.toString() + "]";
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) { return true; }

        return super.onOptionsItemSelected(item);
    }

    private String renderFirstname(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private String renderLastname(String input) {
        return input.toUpperCase();
    }
}
package com.unetresgrossebite.myartgallery;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class ArtworkActivity extends ActionBarActivity {
    private String dname, artist_id = null;
    final private Boolean debug = false;
    final private String datefmt = "dd/MM/yyyy";

    private void qREST() throws JSONException {
        String url = "artworks/" + this.dname + "/";
        myRestClient client = new myRestClient();

        if (artist_id != null) {
            url += "?authorid=" + artist_id;
        }

        if (this.debug) {
            Toast.makeText(ArtworkActivity.this, myRestClient.getAbsoluteUrl(url),
                    Toast.LENGTH_SHORT).show();
        }

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (debug) {
                    Toast.makeText(getApplicationContext(), "Unexpected object received: "
                            + response.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected object received",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    TextView title = (TextView) findViewById(R.id.artwork_name);

                    if (response.length() == 0) {
                        title.setText("no records in this base yet");
                        return;
                    }

                    ListView data = (ListView) findViewById(R.id.artwork_data);
                    JSONObject artworkdata = response.getJSONObject(0);
                    ArrayList itemsReturned = new ArrayList<String>();
                    ArrayAdapter itemsAdapter = null;
                    String tmp = null;
                    String[] prices = new String[]{ "hammer", "lowest", "highest", "premium"};

                    if (title == null || data == null) { return; }
                    if (artworkdata.has("title")) {
                        tmp = capitalize(artworkdata.getString("title"));
                    } else { tmp = "Artwork not found"; }
                    title.setText(tmp);

                    if (artworkdata.has("discipline")) {
                        if (artworkdata.has("type")) {
                            tmp = artworkdata.getString("discipline") + "-"
                                    + artworkdata.getString("type");
                        } else { tmp = artworkdata.getString("discipline"); }
                        itemsReturned.add(tmp);
                    }
                    if (artworkdata.has("technique")) {
                        itemsReturned.add(artworkdata.getString("technique"));
                    }
                    if (artworkdata.has("distinctions")) {
                        itemsReturned.add(artworkdata.getString("distinctions"));
                    }
                    if (artworkdata.has("authordn")) {
                        itemsReturned.add("by " + capitalize(artworkdata.getString("authordn")));
                    }
                    if (artworkdata.has("completed")) {
                        if (artworkdata.has("started")) {
                            itemsReturned.add("Started on " + artworkdata.getString("started"));
                        }
                        itemsReturned.add("Completed on " + artworkdata.getString("completed"));
                    }
                    if (artworkdata.has("auctionhouse")) {
                        itemsReturned.add("Sold in " + artworkdata.getString("auctionhouse"));
                    }
                    if (artworkdata.has("lotid")) {
                        itemsReturned.add("Lot ID: " + artworkdata.getString("lotid"));
                    }
                    if (artworkdata.has("selldate")) {
                        itemsReturned.add( "Sold on " + DateFormat.format(datefmt,
                                artworkdata.getInt("selldate")).toString());
                    }
                    for (String what : prices ) {
                        String field = what + "price";
                        if (artworkdata.has(field)) {
                            itemsReturned.add(capitalize(what) + " price: "
                                    + artworkdata.getString(field));
                        }
                    }
                    if (artworkdata.has("authorid")) {
                        artist_id = artworkdata.getString("authorid");
                        itemsReturned.add("Search for artworks from the same author");
                    }
                    if (debug) {
                        Toast.makeText(getApplicationContext(), itemsReturned.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                    itemsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.list_item, R.id.list_item_data, itemsReturned);
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
        setContentView(R.layout.activity_artwork);
        this.dname = getIntent().getExtras().getString("dname");
        if (getIntent().getExtras().getString("artistid") != null) {
            this.artist_id = getIntent().getExtras().getString("artistid");
        }

        ListView list = (ListView) findViewById(R.id.artwork_data);
        if (list != null) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ListView list = (ListView) findViewById(R.id.artwork_data);
                        String item = list.getItemAtPosition(position).toString();
                        Intent showResult = null;

                        if (item.equals("Search for artworks from the same author")
                                && artist_id != null) {
                            showResult = new Intent(ArtworkActivity.this, SearchActivity.class);
                            showResult.putExtra("base", "artworks");
                            showResult.putExtra("artistid", artist_id);
                        }
                        if (showResult != null) {
                            startActivity(showResult);
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(),
                                "No application can handle this request", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }

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
        getMenuInflater().inflate(R.menu.menu_artwork, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) { return true; }

        return super.onOptionsItemSelected(item);
    }

    private String capitalize(final String str) {
        if (str.isEmpty() == true) { return str; }

        final char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            char ch = buffer[i];
            if (ch == ' ') { capitalizeNext = true; }
            else if (capitalizeNext && ch >= 'a' && ch <= 'z') {
                buffer[i] = (char)(ch - 32);
                capitalizeNext = false;
            } else { capitalizeNext = false; }
        }

        return new String(buffer);
    }
}

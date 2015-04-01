package com.unetresgrossebite.myartgallery;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
                if (debug) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.err_unexpected_object) + ": " + response.toString(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.err_unexpected_object), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    TextView title = (TextView) findViewById(R.id.artist_name);

                    if (response.length() == 0) {
                        title.setText(getString(R.string.msg_no_records_in_db));
                        return;
                    }

                    ListView data = (ListView) findViewById(R.id.artist_data);
                    JSONObject artistdata = response.getJSONObject(0);
                    ArrayList itemsReturned = new ArrayList<String>();
                    ArrayAdapter itemsAdapter = null;
                    String tmp = null;

                    if (title == null || data == null) { return; }
                    if (artistdata.has("firstname")) {
                        tmp = capitalize(artistdata.getString("firstname")) + " "
                                + artistdata.getString("lastname").toUpperCase();
                    } else { tmp = artistdata.getString("lastname").toUpperCase(); }
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
                        if (lookup.equals("growing") || lookup.equals("decreasing")) {
                            tmp = getString(R.string.msg_sell_tendency) + " " + lookup;
                        } else {
                            tmp = getString(R.string.msg_mostly_sold) + ": " + lookup;
                        }
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("turnover")) {
                        String wk = artistdata.getString("turnover");
                        String currency = wk.substring(wk.length() - 1);
                        String value = wk.substring(0, wk.length() - 1);

                        tmp = getString(R.string.msg_turnover) + ": " + renderCurrency(value)
                                + " (" + currency + ")";
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("rank")) {
                        itemsReturned.add(getString(R.string.msg_rank) + ": "
                                + artistdata.getString("rank"));
                    }
                    if (artistdata.has("bestcountry")) {
                        tmp = getString(R.string.msg_mostly_sold_in) + " "
                                + artistdata.getString("bestcountry");
                        if (artistdata.has("bestamount")) {
                            tmp += " (" + artistdata.getString("bestamount") + ")";
                        }
                        itemsReturned.add(tmp);
                    }
                    if (artistdata.has("id")) {
                        artist_id = artistdata.getString("id");
                        itemsReturned.add(getString(R.string.msg_search_related_artworks));
                    }
                    if (debug) {
                        Toast.makeText(getApplicationContext(), itemsReturned.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                    itemsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.list_item, R.id.list_item_data, itemsReturned);
                    data.setAdapter(itemsAdapter);
                } catch (JSONException e) {
                    String error = getString(R.string.err_parsing_server_response)
                            + " [" + e.toString() + "]";
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

        ListView list = (ListView) findViewById(R.id.artist_data);
        if (list != null) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ListView list = (ListView) findViewById(R.id.artist_data);
                        String item = list.getItemAtPosition(position).toString();
                        Intent showResult = null;

                        if (item.equals(getString(R.string.msg_search_related_artworks)) && artist_id != null) {
                            showResult = new Intent(ArtistActivity.this, SearchActivity.class);
                            showResult.putExtra("base", "artworks");
                            showResult.putExtra("artistid", artist_id);
                        }
                        if (showResult != null) {
                            startActivity(showResult);
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.err_unhandled_intent), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }

        try {
            qREST();
        } catch (JSONException e) {
            String error = getString(R.string.err_parsing_server_response)
                    + " [" + e.toString() + "]";
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

    private String renderCurrency(String input) {
        int len = input.length();

        if (len > 3) {
            String tmp = input.substring(len - 3);
            return renderCurrency(input.substring(0, len - 3)) + "," + tmp;
        }

        return input;
    }
}
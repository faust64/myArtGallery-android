package com.unetresgrossebite.myartgallery;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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

public class SearchActivity extends ActionBarActivity {
    String base = null, type = null, pattern = "";
    int timestamp_start = 0, timestamp_stop = 0, cursor = 0, shown = 0, responsePerPage = 20;
    ArrayAdapter itemsAdapter = null;
    Boolean bottom_reached = false, debug = false;

    public void qREST() throws JSONException {
        String url, cursorurl = "", filterurl;
        myRestClient client = new myRestClient();

        if (cursor > 0) { cursorurl = "/+" + Integer.toString(cursor * responsePerPage); }
        if (new String("").equals(pattern)) {
            if (new String("events").equals(base)) { url = base + cursorurl + "/"; }
            else { url = "top/" + base + cursorurl + "/"; }
        } else { url = "search/" + base + cursorurl + "/" + pattern + "/"; }
        if (type != null) { filterurl = url + "?type=" + type; }
        else { filterurl = url; }

        if (debug) {
            Toast.makeText(SearchActivity.this, myRestClient.getAbsoluteUrl(filterurl),
                    Toast.LENGTH_SHORT).show();
        }

        client.get(filterurl, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Unexpected object received",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    ListView view = (ListView) findViewById(R.id.list);
                    TextView qmsg = (TextView) findViewById(R.id.empty);
                    ArrayList itemsReturned = new ArrayList<String>();
                    int len;

                    if (view == null || qmsg == null) { return; }
                    if (cursor == 0 && response.length() == 0) {
                        qmsg.setText("no records in this base yet");
                        bottom_reached = true;
                        return;
                    } else if (response.length() == 0) {
                        bottom_reached = true;
                        return;
                    }
                    if (response.length() > responsePerPage) { len = responsePerPage; }
                    else {
                        len = response.length();
                        bottom_reached = true;
                    }

                    String[] responseArray = new String[len];

                    for (int i = 0; i < len; i++) {
                        JSONObject iterate = response.getJSONObject(i);
                        String dname;

                        if (iterate.has("lastname")) {
                            if (iterate.has("firstname")) {
                                dname = renderFirstname(iterate.getString("firstname"))
                                      + " " + renderLastname(iterate.getString("lastname"));
                            } else {
                                dname = renderLastname(iterate.getString("lastname"));
                            }
                        } else if (iterate.has("title")) {
                            dname = renderFirstname(iterate.getString("title"));
                        } else if (iterate.has("dname")) {
                            dname = renderLastname(iterate.getString("dname"));
                        } else { dname = "Unrecognized object structure"; }
                        responseArray[i] = dname;
                    }

                    if (cursor == 0) {
                        qmsg.setText("");
                        itemsReturned.addAll(Arrays.asList(responseArray));
                        itemsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                android.R.layout.simple_list_item_1, itemsReturned);
                        view.setAdapter(itemsAdapter);
                    } else {
                        itemsReturned.addAll(Arrays.asList(responseArray));
                        itemsAdapter.addAll(itemsReturned);
//                      itemsAdapter.notifyDataSetChanged(); wtf?
                    }
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
        setContentView(R.layout.activity_search);

            base = getIntent().getExtras().getString("base");
            if (new String("events").equals(base)) {
                if (getIntent().getExtras().getString("type") != null) {
                    type = getIntent().getExtras().getString("type");
                }
            }
            if (getIntent().getExtras().getString("pattern") != null) {
                pattern = getIntent().getExtras().getString("pattern");
            }
            if (getIntent().getExtras().getInt("page") > 0) {
                cursor = getIntent().getExtras().getInt("page");
            }
            if (getIntent().getExtras().getInt("start") > 0) {
                timestamp_start = getIntent().getExtras().getInt("start");
            }
            if (getIntent().getExtras().getInt("stop") > 0) {
                timestamp_stop = getIntent().getExtras().getInt("stop");
            }

            ListView list = (ListView) findViewById(R.id.list);
            if (list != null) {
                list.setOnScrollListener(new EndlessScrollListener() {
                    public void onLoadMore(int page, int totalItemsCount) {
                        if (bottom_reached) { return; }
                        try {
                            shown = totalItemsCount;
                            cursor = page;
                            qREST();
                        } catch (JSONException e) {
                            String error = "Error parsing server's response [" + e.toString() + "]";
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        ListView list = (ListView) findViewById(R.id.list);
                        String item = list.getItemAtPosition(position).toString();
                        Toast.makeText(getApplicationContext(),
                                "You selected : " + item, Toast.LENGTH_SHORT).show();
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
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String renderFirstname(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    private String renderLastname(String input) {
        return input.toUpperCase();
    }
}
package com.unetresgrossebite.myartgallery;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private String base = null, type = null, pattern = "";
    private int timestamp_start = 0, timestamp_stop = 0, cursor = 0, responsePerPage = 20;
    private ArrayAdapter itemsAdapter = null;
    Boolean bottom_reached = false;
    final private Boolean debug = false;

    private void qREST() throws JSONException {
        String url, cursorurl = "", filterurl;
        myRestClient client = new myRestClient();

        //context.getResources().getConfiguration().locale.getDisplayCountry(), ou getCountry();
        if (cursor > 0) { cursorurl = "/+" + Integer.toString(cursor * responsePerPage); }
        if (new String("").equals(pattern)) {
            if (new String("events").equals(base)) { url = base + cursorurl + "/"; }
            else { url = "top/" + base + cursorurl + "/"; }
        } else { url = "search/" + base + cursorurl + "/" + pattern + "/"; }
        if (type != null) { filterurl = url + "?type=" + type; }
        else { filterurl = url; }

        if (this.debug) {
            Toast.makeText(SearchActivity.this, myRestClient.getAbsoluteUrl(filterurl),
                    Toast.LENGTH_SHORT).show();
        }

        client.get(filterurl, null, new JsonHttpResponseHandler() {
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
                        } else if (iterate.has("dname")) {
                            dname = renderFirstname(iterate.getString("dname").replaceAll("-", " "));
                        } else { dname = "Unrecognized object structure"; }
                        responseArray[i] = dname;
                    }

                    if (cursor == 0) {
                        qmsg.setText("");
                        itemsReturned.addAll(Arrays.asList(responseArray));
                        itemsAdapter = new ArrayAdapter<String>(getApplicationContext(),
                                R.layout.list_item, R.id.list_item_data, itemsReturned);
                        view.setAdapter(itemsAdapter);
                    } else {
                        itemsReturned.addAll(Arrays.asList(responseArray));
                        itemsAdapter.addAll(itemsReturned);
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

        this.base = getIntent().getExtras().getString("base");
        if (new String("events").equals(base)) {
            if (getIntent().getExtras().getString("type") != null) {
                this.type = getIntent().getExtras().getString("type");
            }
        }
        if (getIntent().getExtras().getString("pattern") != null) {
            this.pattern = getIntent().getExtras().getString("pattern");
        }
        if (getIntent().getExtras().getInt("page") > 0) {
            this.cursor = getIntent().getExtras().getInt("page");
        }
        if (getIntent().getExtras().getInt("start") > 0) {
            this.timestamp_start = getIntent().getExtras().getInt("start");
        }
        if (getIntent().getExtras().getInt("stop") > 0) {
            this.timestamp_stop = getIntent().getExtras().getInt("stop");
        }

        EditText searchbox = (EditText) findViewById(R.id.searchbox);
        if (searchbox != null) {
            searchbox.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    EditText searchbox = (EditText) findViewById(R.id.searchbox);
                    String read = searchbox.getText().toString();
                    String translate = read.replaceAll(" ", "-").toLowerCase();

                    if (translate.equals(pattern)) {
                        return;
                    }
                    pattern = translate;
                    cursor = 0;
                    try {
                        qREST();
                    } catch (JSONException e) {
                        String error = "Error parsing server's response [" + e.toString() + "]";
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }
        ListView list = (ListView) findViewById(R.id.list);
        if (list != null) {
            list.setOnScrollListener(new EndlessScrollListener() {
                public void onLoadMore(int page, int totalItemsCount) {
                    if (bottom_reached) { return; }
                    try {
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
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView list = (ListView) findViewById(R.id.list);
                    String item = list.getItemAtPosition(position).toString();
                    Intent showResult = null;

                    if (base.equals("artists")) {
                        showResult = new Intent(SearchActivity.this, ArtistActivity.class);
                    } else if (base.equals("artworks")) {
                        Toast.makeText(getApplicationContext(),
                                "FIXME renderArtwork: " + renderDname(item), Toast.LENGTH_SHORT).show();
                    } else if (base.equals("events")) {
                        showResult = new Intent(SearchActivity.this, EventActivity.class);
                    }

                    if (showResult != null) {
                        showResult.putExtra("dname", renderDname(item));
                        startActivity(showResult);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Unrecognized base: '" + base + "'", Toast.LENGTH_SHORT).show();
                    }
                    if (debug == true) {
                        Toast.makeText(getApplicationContext(),
                                "You selected : " + renderDname(item), Toast.LENGTH_SHORT).show();
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

    private String renderDname(String input) {
        String tmp1 = input.toLowerCase().replaceAll(" ", "-").replaceAll("æ", "ae");
        String tmp2 = tmp1.replaceAll("ç", "c").replaceAll("[ūúǔùüǖǘǚǜ]", "u");
        String tmp3 = tmp2.replaceAll("[āáǎà]", "a").replaceAll("[ēéěèë]", "e");
        String tmp4 = tmp3.replaceAll("[īíǐì]", "i").replaceAll("[ōóǒòö]", "o");

        return tmp4;
    }
}
package com.unetresgrossebite.myartgallery;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
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

public class EventActivity extends ActionBarActivity {
    private String dname, link, maps;
    final private String datefmt = "dd/MM/yyyy";
    final private Boolean debug = false;

    private void qREST() throws JSONException {
        String url = "events/" + this.dname + "/";
        myRestClient client = new myRestClient();

        if (this.debug) {
            Toast.makeText(EventActivity.this, myRestClient.getAbsoluteUrl(url),
                    Toast.LENGTH_SHORT).show();
        }

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject eventdata) {
                try {
                    TextView title = (TextView) findViewById(R.id.event_title);

                    if (eventdata.length() == 0) {
                        title.setText("no records in this base yet");
                        return;
                    }

                    ListView data = (ListView) findViewById(R.id.event_data);
                    ArrayList itemsReturned = new ArrayList<String>();
                    ArrayAdapter itemsAdapter = null;
                    String tmp = null;

                    if (title == null || data == null) { return; }
                    if (eventdata.has("title")) {
                        tmp = renderFirstname(eventdata.getString("title"));
                    } else {
                        tmp = renderFirstname(eventdata.getString("dname"));
                    }
                    title.setText(tmp);

                    if (eventdata.has("starts")) {
                        if (eventdata.has("stops")) {
                            long a, b;

                            a = eventdata.getLong("starts") * 1000;
                            b = eventdata.getLong("stops") * 1000;
                            if (a == b) {
                                tmp = "The " + DateFormat.format(datefmt, a).toString();
                            }
                            else {
                                tmp = "From " + DateFormat.format(datefmt, a).toString()
                                        + " to " + DateFormat.format(datefmt, b).toString();
                            }
                        }
                        else {
                            tmp = "The "+ DateFormat.format(datefmt, eventdata.getLong("start") * 1000).toString();
                        }
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("country")) {
                        if (eventdata.has("city")) {
                            if (eventdata.has("direction")) {
                                tmp = eventdata.getString("direction") + " -- "
                                        + eventdata.getString("city") + " ("
                                        + eventdata.getString("country") + ")";
                            } else {
                                tmp = eventdata.getString("city") + " ("
                                        + eventdata.getString("country") + ")";
                            }
                        } else if (eventdata.has("direction")) {
                            tmp = eventdata.getString("direction") + " ("
                                    + eventdata.getString("country") + ")";
                        } else {
                            tmp = eventdata.getString("country");
                        }
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("tel")) {
                        tmp = "Phone: " + eventdata.getString("tel");
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("url")) {
                        tmp = "Gallery site link";
                        link = eventdata.getString("url");
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("maps")) {
                        tmp = "Show in GoogleMaps";
                        maps = eventdata.getString("maps");
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("group")) {
                        boolean value = eventdata.getBoolean("group");
                        if (value) { tmp = "(Group show)"; }
                        else { tmp = "(Solo show)"; }
                        itemsReturned.add(tmp);
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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (debug) {
                    Toast.makeText(getApplicationContext(), "Unexpected object received: "
                            + response.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Unexpected object received",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
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
        getMenuInflater().inflate(R.menu.menu_event, menu);
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
}
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

import java.util.HashMap;

public class SearchActivity extends ActionBarActivity {
    static String base = null, type = null, pattern = "";
    static int timestamp_start = 0, timestamp_stop = 0, cursor = 0;

    public void qREST() throws JSONException {
        String url, baseurl = new String("search/") + base + pattern;
        myRestClient client = new myRestClient();

        if (pattern == "") {
            url = new String("top/artists/");
        } else if (cursor > 0) {
            url = baseurl + new String("/+") + Integer.toString(cursor);
        } else { url = baseurl; }

//      Toast.makeText(SearchActivity.this, myRestClient.getAbsoluteUrl(url), Toast.LENGTH_SHORT).show();

        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), "Unexpected object received",
                        Toast.LENGTH_SHORT).show();
/*                try {
                    Toast.makeText(getApplicationContext(), response.toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    String error = "Error parsing server's response [" + e.toString() + "]";
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
*/            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    HashMap<String, String> responseMap = new HashMap<String,String>();
                    String[] responseArray = new String[response.length()];
                    ListView view = (ListView) findViewById(R.id.list);
                    TextView qmsg = (TextView) findViewById(R.id.empty);

                    if (view == null) { return; }
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject iterate = response.getJSONObject(i);
                        String dname, id;

                        id = response.getJSONObject(i).getString("id");
                        if (iterate.has("lastname")) {
                            if (iterate.has("firstname")) {
                                dname = renderFirstname(iterate.getString("firstname"))
                                      + " " + renderLastname(iterate.getString("lastname"));
                            } else {
                                dname = renderLastname(iterate.getString("lastname"));
                            }
                        } else if (iterate.has("dname")) {
                            dname = renderLastname(iterate.getString("dname"));
                        } else { dname = "Unrecognized object structure"; }
                        responseMap.put(id, dname);
                        responseArray[i] = dname;
//                      Toast.makeText(getApplicationContext(), dname, Toast.LENGTH_LONG).show();
                    }

                    ArrayAdapter items = new ArrayAdapter<String>(getApplicationContext(),
                            android.R.layout.simple_list_item_1, responseArray);
                    qmsg.setText("");
                    view.setAdapter(items);
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
        if (base == null) {
            base = getIntent().getExtras().getString("base");
            if (base == "events") {
                if (getIntent().getExtras().getString("type") != null) {
                    type = getIntent().getExtras().getString("type");
                }
            }
            if (getIntent().getExtras().getString("pattern") != null) {
                pattern = new String("/") + getIntent().getExtras().getString("pattern") + new String("/");
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
//          Toast.makeText(SearchActivity.this, base, Toast.LENGTH_SHORT).show();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
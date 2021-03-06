package com.unetresgrossebite.myartgallery;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
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

public class EventActivity extends ActionBarActivity {
    private String dname, link = null, maps = null, phonenr = null, title = null, descr = null, where = null;
    private long timestamp_start = 0, timestamp_stop = 0;
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
                    TextView titleview = (TextView) findViewById(R.id.event_title);

                    if (eventdata.length() == 0) {
                        titleview.setText(getText(R.string.msg_no_records_in_db));
                        return;
                    }

                    TextView descrview = (TextView) findViewById(R.id.event_descr);
                    ListView data = (ListView) findViewById(R.id.event_data);
                    ArrayList itemsReturned = new ArrayList<String>();
                    ArrayAdapter itemsAdapter = null;
                    String tmp = null;

                    if (titleview == null || data == null) { return; }

                    if (eventdata.has("starts")) {
                        timestamp_start = eventdata.getLong("starts") * 1000;
                        if (eventdata.has("stops")) {
                            timestamp_stop = eventdata.getLong("stops") * 1000;
                        } else { timestamp_stop = timestamp_start; }
                        if (timestamp_start == timestamp_stop) {
                            tmp = getString(R.string.msg_the_date)+ " "
                                    + DateFormat.format(datefmt, timestamp_start).toString();
                        }
                        else {
                            tmp = getString(R.string.msg_from_date) + " "
                                    + DateFormat.format(datefmt, timestamp_start).toString()
                                    + " " + getString(R.string.msg_to_date) + " "
                                    + DateFormat.format(datefmt, timestamp_stop).toString();
                        }
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("country")) {
                        if (eventdata.has("city")) {
                            if (eventdata.has("direction")) {
                                where = eventdata.getString("direction") + ", "
                                        + eventdata.getString("city");
                                tmp = where + " (" + eventdata.getString("country") + ")";
                            } else {
                                where = eventdata.getString("city");
                                tmp = where + " (" + eventdata.getString("country") + ")";
                            }
                        } else if (eventdata.has("direction")) {
                            where = eventdata.getString("direction");
                            tmp = where + " (" + eventdata.getString("country") + ")";
                        } else {
                            tmp = where = eventdata.getString("country");
                        }
                        itemsReturned.add(tmp);
                    }
                    if (eventdata.has("tel")) {
                        phonenr = eventdata.getString("tel");
                        itemsReturned.add(getString(R.string.msg_phone) + ": " + phonenr);
                    }
                    if (eventdata.has("url")) {
                        link = eventdata.getString("url");
                        itemsReturned.add(getString(R.string.msg_gallery_link));
                    }
                    if (eventdata.has("maps")) {
                        maps = eventdata.getString("maps");
                        itemsReturned.add(getString(R.string.msg_show_in_gmaps));
                    }
                    if (eventdata.has("title")) {
                        title = renderFirstname(eventdata.getString("title"));
                    } else {
                        title = renderFirstname(eventdata.getString("dname"));
                    }
                    if (eventdata.has("expo")) {
                        if (eventdata.getBoolean("expo") == true) {
                            descr = getString(R.string.msg_expo);
                        }
                    } else if (eventdata.has("auction")) {
                        if (eventdata.getBoolean("auction") == true) {
                            descr = getString(R.string.msg_auction);
                        }
                    }
                    if (eventdata.has("group")) {
                        if (descr != null) { descr += ", "; } else { descr = ""; }
                        if (eventdata.getBoolean("group") == true) {
                            descr = getString(R.string.msg_group_show);
                        } else { descr = getString(R.string.msg_solo_show); }
                    }
                    titleview.setText(title);
                    descrview.setText(descr);
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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (debug) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.err_unexpected_object) + ": " + response.toString(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.err_unexpected_object), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        this.dname = getIntent().getExtras().getString("dname");

        ListView list = (ListView) findViewById(R.id.event_data);
        if (list != null) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        ListView list = (ListView) findViewById(R.id.event_data);
                        String item = list.getItemAtPosition(position).toString();
                        Intent showResult = null;

                        if (item.equals(getString(R.string.msg_show_in_gmaps)) && maps != null) {
                            showResult = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://maps.google.com/" + maps));
                        } else if (item.equals(getString(R.string.msg_gallery_link))
                                && link != null) {
                            showResult = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://" + link));
                        } else if (item.contains(getString(R.string.msg_phone) + ": ")) {
                            showResult = new Intent(Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + phonenr));
                        } else if (item.matches(getString(R.string.msg_from_date) + " [0-9/]* "
                                + getString(R.string.msg_to_date) + " [0-9/]*") && timestamp_start > 0) {
                            showResult = new Intent(Intent.ACTION_EDIT);
                            showResult.setType("vnd.android.cursor.item/event");
                            showResult.putExtra("beginTime", timestamp_start * 1000);
                            showResult.putExtra("allDay", true);
                            showResult.putExtra("endTime", timestamp_stop * 1000);
                            showResult.putExtra("title", title);
                        } else if (item.matches("The [0-9/]*") && timestamp_start > 0) {
                            showResult = new Intent(Intent.ACTION_EDIT);
                            showResult.setType("vnd.android.cursor.item/event");
                            showResult.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                    timestamp_start * 1000);
                            showResult.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,
                                    CalendarContract.Events.ALL_DAY);
                            showResult.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                    (timestamp_stop + 24 * 60 * 60) * 1000);
                            showResult.putExtra(CalendarContract.Events.TITLE, title);
                            showResult.putExtra(CalendarContract.Events.AVAILABILITY,
                                    CalendarContract.Events.AVAILABILITY_BUSY);
                            if (descr != null) {
                                showResult.putExtra(CalendarContract.Events.DESCRIPTION, descr);
                            }
                            if (where != null) {
                                showResult.putExtra(CalendarContract.Events.EVENT_LOCATION, where);
                            }
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
package com.unetresgrossebite.myartgallery;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class IndexActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_index, menu);
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

    public void doSearchArtist(View view) {
        Intent searchArtist = new Intent(IndexActivity.this, SearchActivity.class);

        searchArtist.putExtra("base", "artists");
        startActivity(searchArtist);
    }
    public void doSearchArtwork(View view) {
        Toast.makeText(IndexActivity.this, "artwork",
                Toast.LENGTH_SHORT).show();
    }
    public void doSearchEvent(View view) {
        Toast.makeText(IndexActivity.this, "event",
                Toast.LENGTH_SHORT).show();
    }
    public void myArtGallery(View view) {
        Toast.makeText(IndexActivity.this, "salut gallerie",
                Toast.LENGTH_SHORT).show();
    }
}
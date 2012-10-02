package com.airbai.noisemap;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {
	public final static String EXTRA_MESSAGE = "com.airbai.noisemap.MESSAGE";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TabHost tabHost=(TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("Tab 1");

        TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("Tab 2");
        spec2.setContent(R.id.tab2);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        
        
        ActionBar bar = getActionBar();  
        bar.addTab(bar.newTab().setText("Tab 1"));  
        bar.addTab(bar.newTab().setText("Tab 2"));  
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM  
                  | ActionBar.DISPLAY_USE_LOGO);  
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);  
        bar.setDisplayShowHomeEnabled(true);  
        bar.setDisplayShowTitleEnabled(false);  
        bar.show();  
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void sendMessage(View view){
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
}

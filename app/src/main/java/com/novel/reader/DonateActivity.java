package com.novel.reader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.vending.billing.InAppBillingForNovel;
import com.novel.reader.util.Setting;

public class DonateActivity extends ActionBarActivity {
	
	private Button donate_btn;
	private Button validate_btn;
    private InAppBillingForNovel iap;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.setApplicationActionBarTheme(this);
        setContentView(R.layout.layout_donate);
        
        final ActionBar ab = getSupportActionBar();
        ab.setTitle("贊助小說王");
        ab.setDisplayHomeAsUpEnabled(true);

        findViews();
        setListener();
        iap = new InAppBillingForNovel(DonateActivity.this);

    }

	private void setListener() {
		donate_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	iap.launchSubscriptionFlow();
            }
        });
		validate_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	iap = new InAppBillingForNovel(DonateActivity.this);
            }
        });
		
		
	}

	private void findViews() {
		donate_btn = (Button) findViewById(R.id.donate_button);
		validate_btn = (Button) findViewById(R.id.validate_button);
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        if (iap != null && iap.mHelper != null) {
        	try {
        		iap.mHelper.dispose();
        	}catch (IllegalArgumentException ex){
                ex.printStackTrace();
            }finally{}
        	
        	iap.mHelper = null;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (iap.mHelper == null) return;

        if (!iap.mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
            finish();
        }
		return true;
    }

	

}

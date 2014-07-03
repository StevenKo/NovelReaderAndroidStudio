package com.android.vending.billing;

import java.util.ArrayList;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;
import com.novel.reader.ArticleActivity;
import com.novel.reader.R;
import com.novel.reader.util.Setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;




public class InAppBillingForNovel {
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkOpAaja9nKG95GKvPA55/LkosuIGm6iQJP3879Qan50WWxZJLXjThsYSxYwAF4QCEmFYPTWcu78E3h178AwW/vC7vaFjKmG38BnSVuVg0zKck3LYyyLTTmcvXmLgkcZEUs1EVUXj1ykAf2c0QrJ6ngS4J0Sli45uk9XtBBi5hSNwbSt6JgtG4HXPwghPxF1ZPmJPBn6X2yXlJFSnVRIsJQx49c7GAmMyogLniMywpyTMHg+T7zTEUZl9MIGoW3P3nB73Dr9InrO7xs3UdsqOHbiM/GC3oGzsi3ZLAbsPfWVtVdX/nPMC4uTK2U/MePLv00/SdVZ9jJPNvP1fT+STmQIDAQAB";
    static final String TAG = "iapp";
    public IabHelper mHelper;
    Context mContext;
    public  boolean mIsYearSubscription = false;
    
    String IAP_subscription = "year_subscription_1";
    
    public InAppBillingForNovel(Context context)
    {
    	mContext = context;
    	mHelper = new IabHelper(context, base64EncodedPublicKey);
    	mHelper.enableDebugLogging(true);
    	
    	mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }
    
    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("失敗: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(mContext);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    
    public void launchSubscriptionFlow(){
    	mHelper.launchSubscriptionPurchaseFlow((Activity) mContext, IAP_subscription, 1001,
                mPurchaseFinishedListener, "");
    }
    
    public void queryProducts(){
    	ArrayList<String> additionalSkuList = new ArrayList<String>();
    	additionalSkuList.add(IAP_subscription);
    	mHelper.queryInventoryAsync(true, additionalSkuList, mQueryFinishedListener);
    }
    
    IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new  IabHelper.QueryInventoryFinishedListener(){

		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
			if (result.isFailure()) {
			  return;
            }
			String applePrice = inv.getSkuDetails(IAP_subscription).getPrice();
		}
    	
    };
	
    
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        

		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");
            Purchase premiumPurchase = inventory.getPurchase(IAP_subscription);
            mIsYearSubscription = (premiumPurchase != null && premiumPurchase.getPurchaseState() == 0);
            
            if(mIsYearSubscription){
            	Setting.saveSetting(Setting.keyYearSubscription, 1, mContext);
            	alert("經驗證已購買，十分感謝你！");
            }
            else
            	Setting.saveSetting(Setting.keyYearSubscription, 0, mContext);
            
            Log.d(TAG, "User is " + (mIsYearSubscription ? IAP_subscription : "not year_subscription"));
            


            
        }
    };

    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
            	
            	if(!mIsYearSubscription)
            		alert(mContext.getResources().getString(R.string.buy_fail));
                return;
            }
            
            if (purchase.getSku().equals(IAP_subscription)) {
            	alert(mContext.getResources().getString(R.string.restart_novel));
            }

            Log.d(TAG, "Purchase successful.");

        }
    };    
    
}

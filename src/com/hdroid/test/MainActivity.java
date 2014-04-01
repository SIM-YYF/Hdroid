package com.hdroid.test;

import com.hdroid.R;
import com.hdroid.upgrade.UpdateFormat;
import com.hdroid.upgrade.UpdateManager;
import com.hdroid.upgrade.UpdateOptions;
import com.hdroid.upgrade.UpdatePeriod;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private String str;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
        
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Integer versionCode = pinfo.versionCode; // 1
        String versionName = pinfo.versionName; // 1.0

		
		Button  btn_version = (Button) this.findViewById(R.id.btn_version);
		btn_version.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				versionUpgrade();
			}
		});
		
	}

	protected void versionUpgrade() {
		UpdateManager manager = new UpdateManager(this);

        UpdateOptions options = new UpdateOptions.Builder(this)
                .checkUrl("https://raw.github.com/snowdream/android-autoupdate/master/docs/test/updateinfo.xml")
                .updateFormat(UpdateFormat.XML)
                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
                .checkPackageName(false)
                .build();
//
//        UpdateOptions options = new UpdateOptions.Builder(this)
//                .checkUrl("https://raw.github.com/snowdream/android-autoupdate/master/docs/test/updateinfo.json")
//                .updateFormat(UpdateFormat.JSON)
//                .updatePeriod(new UpdatePeriod(UpdatePeriod.EACH_TIME))
//                .checkPackageName(true)
//                .build();

        manager.check(this, options);
	}
}

package dk.diku.bumptest;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpConnectFailedReason;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.bluetooth.*;

public class MainActivity extends Activity {

	public static final String BUMP_INTENT = "bump_intent";

	
	private static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	// Used to distinguish Bump activity results from other activity results
	private static final int BUMP_API_REQUEST_CODE = 1025;

	private Button btnStartBumpAPI;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initializeButtons();
	}

	private void initializeButtons() {
		btnStartBumpAPI = (Button)findViewById(R.id.btnStartBumpAPI);
		btnStartBumpAPI.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent bump = new Intent(MainActivity.this, BumpAPI.class);
				bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				startActivityForResult(bump, BUMP_API_REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BUMP_API_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Intent connected = new Intent(this, ConnectedActivity.class);
				connected.putExtra(BumpAPI.EXTRA_CONNECTION, data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION));
				connected.putExtra(BUMP_INTENT, data);
				startActivity(connected);
			} else if (data != null) {
				// Failed to connect, obtain the reason
				BumpConnectFailedReason reason =
					(BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				Toast.makeText(this, "Failed to connect with Bump.\n" + reason.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}	
}
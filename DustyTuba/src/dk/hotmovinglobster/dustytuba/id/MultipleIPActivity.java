package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

// FIXME: Does not return properly... might be the way we call it.

/**
 * An activity allowing an end user to select from multiple identity providers
 * 
 * Expects a string array extra with key BtAPI.EXTRA_IP_PROVIDERS with string
 * values of identity providers available for selection
 */
public class MultipleIPActivity extends Activity {
	
	/**
	 * List of string identifiers of identity providers
	 */
	private String[] providers = {};
	/**
	 * List of nice names of identity providers
	 */
	private String[] providerNames = {};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		
		// Populate string arrays
		providers = intent.getStringArrayExtra( BtAPI.EXTRA_IP_PROVIDERS );
		providerNames = new String[ providers.length ];
		for (int i = 0; i < providers.length; i++ ) {
			providerNames[i] = BtAPI.stringToIdProviderName( this, providers[i] );
		}
		
		// Build selection dialog
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle( BtAPI.res( this, "string", "dustytuba_select_identity_provider" ) );
		builder.setItems( providerNames, new DialogInterface.OnClickListener() {
			// When an identity provider is selected, the providers activity
			// should be launched
		    public void onClick(DialogInterface dialog, int item) {
		    	Intent i = BtAPI.getIntent( MultipleIPActivity.this, providers[item], intent.getExtras() );
		    	startActivityForResult( i, 0 );
		    }
		});
		
		// When user presses back button, activity should cancel
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				setResult( RESULT_CANCELED );
				finish();
			}
		});
		final AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		// Upon return from the launched identity provider activity,
		// the data should just be forwarded to GenericIPActivity
		setResult( resultCode, data );
		finish();
	}
	
}

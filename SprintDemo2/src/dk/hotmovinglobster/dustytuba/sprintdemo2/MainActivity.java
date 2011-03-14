package dk.hotmovinglobster.dustytuba.sprintdemo2;

import com.bumptech.bumpapi.*;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
//import android.bluetooth.*;
import android.content.Intent;

public class MainActivity extends Activity implements BumpAPIListener {
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_BUMP = 3;
	private static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	
	private enum ProtocolState { NONE, SERVER_RANDOM_NUMBER, BLUETOOTH_MAC, BLUETOOTH_NAME };
	private ProtocolState protocolState = ProtocolState.NONE;
	private ByteArrayList protocolBuffer = new ByteArrayList(64);
	
	private static final byte PROTOCOL_SERVER_RANDOM_NUMBER = 1;
	private static final byte PROTOCOL_BLUETOOTH_MAC = 2;
	private static final byte PROTOCOL_BLUETOOTH_NAME = 3;
	
	private TextView lblMyBTMac;
	private TextView lblMyBTName;
	private TextView lblMyBTConnType;
	private TextView lblOtherBTMac;
	private TextView lblOtherBTName;
	private TextView lblOtherBTConnType;
	
	private Button btnConnectBump;
	
	//private String MyMAC = null;
	private java.util.Random rnd = new java.util.Random();
	private float serverRandomNumber = rnd.nextFloat();
	private float otherServerRandomNumber;
	private String otherBluetoothMAC;
	private String otherBluetoothName;
	private boolean isServer;
	
	private BumpConnection bConn = null;
	
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeViews();
        
        //if (mBluetoothAdapter == null) {
        //	Toast.makeText(this, "Bluetooth not available on this device!", Toast.LENGTH_LONG).show();
        //}
    }

	private void initializeViews() {
		lblMyBTMac = (TextView)findViewById(R.id.lblMyBTMac);
        lblMyBTName = (TextView)findViewById(R.id.lblMyBTName);
        lblMyBTConnType = (TextView)findViewById(R.id.lblMyBTConnType);
        lblOtherBTMac = (TextView)findViewById(R.id.lblOtherBTMac);
        lblOtherBTName = (TextView)findViewById(R.id.lblOtherBTName);
        lblOtherBTConnType = (TextView)findViewById(R.id.lblOtherBTConnType);
        
        btnConnectBump = (Button)findViewById(R.id.btnConnectBump);
        btnConnectBump.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent bump = new Intent(MainActivity.this, BumpAPI.class);
				bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				startActivityForResult(bump, REQUEST_BUMP);
			}
		});
	}
    
    @Override
    public void onStart() {
    	super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        	BluetoothActivated();
        }
    }
    
    private String getBluetoothAddress() {
    	return mBluetoothAdapter.getAddress();
    	/*
    	if (MyMAC == null) {
        	MyMAC = "00:00:00:00:00:" + rnd.nextInt(10) + rnd.nextInt(10);
    	}
    	return MyMAC;
    	*/
    }
    
    private String getBluetoothName() {
    	return mBluetoothAdapter.getName();
    	//return MyMAC.substring(15, 17);
    }
    
    private void BluetoothActivated() {
        lblMyBTMac.setText( "MAC: " + getBluetoothAddress() );
        lblMyBTName.setText( "Name: " + getBluetoothName() );
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_ENABLE_BT) {
    		if (resultCode == RESULT_OK) {
    			BluetoothActivated();
    		} else {
    			Toast.makeText(this, "Bluetooth was not enabled. Cannot fetch information", Toast.LENGTH_LONG).show();
    		}
    	} else if (requestCode == REQUEST_BUMP) {
			if (resultCode == RESULT_OK) {
				bConn = data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				bConn.setListener( this );
				Log.i( "SPRINTDEMO" , "Obtained connection through bump");
				sendBluetoothInfo();
			} else if (data != null) {
				// Failed to connect, obtain the reason
				BumpConnectFailedReason reason =
					(BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				Toast.makeText(this, "Failed to connect with Bump.\n" + reason.toString(), Toast.LENGTH_LONG).show();
			}
    	}
    }

	@Override
	public void bumpDataReceived(byte[] arg0) {
		Log.i("SPRINTDEMO", "Received: " + new String(arg0));
		Log.i("SPRINTDEMO", "State prior: " + protocolState.toString() );
		for (int i = 0; i < arg0.length; i++)
			byteReceived( arg0[i] );
		Log.i("SPRINTDEMO", "State posterior: " + protocolState.toString() );
		//Toast.makeText(this, "Bump received: " + new String(arg0), Toast.LENGTH_SHORT).show();
		
	}
	
	private void byteReceived(byte arg0) {
		Log.i("SPRINTDEMO", "Byte: " + arg0 + " (" + (int)arg0 + "), State: " + protocolState.toString() );
		if (protocolState == ProtocolState.NONE) {
			switch (arg0) {
				case PROTOCOL_SERVER_RANDOM_NUMBER:
					protocolState = ProtocolState.SERVER_RANDOM_NUMBER;
					break;
				case PROTOCOL_BLUETOOTH_MAC:
					protocolState = ProtocolState.BLUETOOTH_MAC;
					break;
				case PROTOCOL_BLUETOOTH_NAME:
					protocolState = ProtocolState.BLUETOOTH_NAME;
					break;
			}
		} else {
			protocolBuffer.add( arg0 );
			if (protocolState == ProtocolState.SERVER_RANDOM_NUMBER) {
				if ( protocolBuffer.size() == 4 ) {
					otherServerRandomNumber = ByteArrayTools.toFloat( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherServerNumberObtained();
				}
			} else if (protocolState == ProtocolState.BLUETOOTH_MAC) {
				if ( protocolBuffer.size() == 17 ) {
					otherBluetoothMAC = new String( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherBluetoothMACObtained();
				}
			} else if (protocolState == ProtocolState.BLUETOOTH_NAME) {
				if ( (int)arg0 == 0 ) {
					otherBluetoothName = new String( protocolBuffer.toArray() );
					otherBluetoothName = otherBluetoothName.substring(0, otherBluetoothName.length() - 1);
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherBluetoothNameObtained();
				}
			}
		}
	}

    private void sendBluetoothInfo() {
    	ByteArrayList byl = new ByteArrayList();
    	byl.add( PROTOCOL_SERVER_RANDOM_NUMBER );
    	byl.addAll( ByteArrayTools.toByta( serverRandomNumber ) );
    	byl.add( PROTOCOL_BLUETOOTH_MAC );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothAddress() ) );
    	byl.add( PROTOCOL_BLUETOOTH_NAME );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothName() ) );
    	byl.add( (byte)0 );
    	bConn.send( byl.toArray() );
    	Log.i("SPRINTDEMO", "Sent bluetooth info" );
    }
    
	private void otherServerNumberObtained() {
		if ( otherServerRandomNumber == serverRandomNumber ) {
	    	bConn.send( new byte[]{PROTOCOL_SERVER_RANDOM_NUMBER} );
	    	bConn.send( ByteArrayTools.toByta( serverRandomNumber ) );
		} else {
			if ( otherServerRandomNumber < serverRandomNumber ) {
				isServer = true;
				lblMyBTConnType.setText("Connection type: Server");
				lblOtherBTConnType.setText("Connection type: Client");
			} else {
				isServer = false;
				lblMyBTConnType.setText("Connection type: Client");
				lblOtherBTConnType.setText("Connection type: Server");
			}
		}
	}
	
    private void otherBluetoothMACObtained() {
		lblOtherBTMac.setText(otherBluetoothMAC);
	}

    private void otherBluetoothNameObtained() {
		lblOtherBTName.setText(otherBluetoothName);
	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		Toast.makeText(this, "Bump disconnected", Toast.LENGTH_LONG).show();
		// TODO Auto-generated method stub
		
	}
}
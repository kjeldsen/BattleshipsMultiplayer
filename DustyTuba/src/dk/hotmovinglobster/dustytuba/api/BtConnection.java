package dk.hotmovinglobster.dustytuba.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import dk.hotmovinglobster.dustytuba.tools.ByteArrayTools;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class BtConnection {
	
	private final BluetoothSocket mSocket;
	private final InputStream mInStream;
	private final OutputStream mOutStream;
	private BtAPIListener mListener;
	private Handler mHandler;
	
	private final BluetoothThread thread;
	
	private static BtConnection smConn = null;
	
	public static BtConnection getConnection() {
		return smConn;
	}
	
	public static void setConnection(BtConnection conn) {
		smConn = conn;
	}
	
	// TODO: protect?
	/**
	 * Instantiate a new Bluetooth Connection object. Only to be used internally
	 * 
	 */
	public BtConnection (BluetoothSocket socket){
		mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mInStream = tmpIn;
        mOutStream = tmpOut;
        
        thread = new BluetoothThread();
    }
	
	public void startListening() {
		thread.start();
	}
	
	public void stopListening() {
		thread.stop();
	}
	
	/** 
	 * Sets the btAPIListener for this connection
	 * @param l The listener to set 
	 */
	public void setListener(BtAPIListener l) {
		setListener(l, new Handler());
	}
	
	/** 
	 * Sets the btAPIListener for this connection defining a Handler on which the calls will be made
	 * @param l The listener to set
	 * @param handler A handler to which the calls will be made 
	 */
	public void setListener(BtAPIListener l, Handler handler) {
		mListener = l;
		mHandler = handler;
	}
	
	/** 
	 * Send a chunk to the other user
	 * @param chunk The data to send
	 */
	public void send(final byte[] chunk)
	{
    	Log.v(BtAPI.LOG_TAG, "BtConnection: Asked to send "+chunk.length+" bytes ("+ByteArrayTools.toString(chunk)+")");
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				thread.write(chunk);
			}
		});
	}
	
	/**
	 * Send a string to the other user
	 * @param str The string to send
	 */
	public void send(final String str) {
		send( ByteArrayTools.toByta( str ) );
	}
	
	/** 
	 * Disconnect from the API service 
	 */
	public void disconnect(){}
	
	private class BluetoothThread extends Thread {
		public BluetoothThread() {
		}
		
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()

	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mInStream.read(buffer);
	                byte[] result = new byte[bytes];
	                for (int i = 0; i < bytes; i++) {
	                	result[i] = buffer[i];
	                }
	                Log.v(BtAPI.LOG_TAG, "BtConnection: Read "+bytes+" bytes from InputStream ("+ByteArrayTools.toString(result)+")");
	                final byte[] finalResult = result;
	                // Send the obtained bytes to the UI Activity
	                mHandler.post(new Runnable() {
						
						@Override
						public void run() {
			                mListener.btDataReceived(finalResult);
						}
					});
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }

	    /* Call this from the main Activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mOutStream.write(bytes);
	        } catch (IOException e) { }
	    }

	    /* Call this from the main Activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mSocket.close();
	            mListener.btDisconnect(BtAPI.BtDisconnectReason.END_USER_QUIT);
	        } catch (IOException e) { }
	    }
	}

}
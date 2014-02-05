package com.example.apptest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This is the main Activity that displays the current chat session.
 */
public class MainActivity extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Message content sent from the BluetoothService
    public static final int MESSAGE_SOC = 1;
    public static final int MESSAGE_MAX_TEMPERATURE = 2;
    public static final int MESSAGE_MIN_TEMPERATURE = 3;
    public static final int MESSAGE_VOLTAGE = 4;
    public static final int MESSAGE_CURRENT = 5;



    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Information of the incoming data.

    // ID for the data of the SOC (state of charge) and Battery Cell Minimum Temperature and Battery Cell Maximum Temperature of the Battery
    private static final String ID_SOC_TEMPERATURE = "0161";
    private static final int LENGTH_ID_SOC_TEMPERATURE= 14;

    // ID for the data of the current and voltage of the Battery
    private static final String ID_VOLTAGE_CURRENT = "0326";
    private static final int LENGTH_ID_VOLTAGE_CURRENT = 16;


    // Layout Views
    //private ListView mConversationView;
    //private ListView socView;
    //private ListView maxTempView;
    //private ListView minTempView;
    //private ListView voltageView;
    //private ListView currentView;

    private TextView socView;
    private TextView maxTempView;
    private TextView minTempView;
    private TextView voltageView;
    private TextView currentView;

    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    //private ArrayAdapter<String> mConversationArrayAdapter;
    private ArrayAdapter<String> socArrayAdapter;
    private ArrayAdapter<String> maxTempArrayAdapter;
    private ArrayAdapter<String> minTempArrayAdapter;
    private ArrayAdapter<String> voltageArrayAdapter;
    private ArrayAdapter<String> currentArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.fragment_main);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        socArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        maxTempArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        minTempArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        voltageArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        currentArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        //socView = (ListView) findViewById(R.id.stateOfCharge);
        //maxTempView = (ListView) findViewById(R.id.maxTemperature);
        //minTempView = (ListView) findViewById(R.id.minTemperature);
        //voltageView = (ListView) findViewById(R.id.voltage);
        //currentView = (ListView) findViewById(R.id.current);

        socView = (TextView) findViewById(R.id.textView);
        maxTempView = (TextView) findViewById(R.id.textView2);
        minTempView = (TextView) findViewById(R.id.textView3);
        voltageView = (TextView) findViewById(R.id.textView4);
        currentView = (TextView) findViewById(R.id.textView5);

       // socView.setAdapter(socArrayAdapter);
       // maxTempView.setAdapter(maxTempArrayAdapter);
       // minTempView.setAdapter(minTempArrayAdapter);
       // voltageView.setAdapter(voltageArrayAdapter);
       // currentView.setAdapter(currentArrayAdapter);

        // Initialize the compose field with a listener for the return key
        //mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        //mSendButton = (Button) findViewById(R.id.button_send);
/*        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });*/

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    if(D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };

    private final void setStatus(int resId) {
        final android.app.ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final android.app.ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            socArrayAdapter.clear();
                            maxTempArrayAdapter.clear();
                            minTempArrayAdapter.clear();
                            voltageArrayAdapter.clear();
                            currentArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String full_message = bytesToHex(readBuf);
                    if (findMessage(full_message, ID_SOC_TEMPERATURE) > 0)
                    {
                        socView.setText(messageUpdate(full_message, findMessage(full_message, ID_SOC_TEMPERATURE), MESSAGE_SOC) + " %");
                        maxTempView.setText(messageUpdate(full_message, findMessage(full_message, ID_SOC_TEMPERATURE), MESSAGE_MAX_TEMPERATURE) + " °C");
                        minTempView.setText(messageUpdate(full_message, findMessage(full_message, ID_SOC_TEMPERATURE), MESSAGE_MIN_TEMPERATURE) + " °C");
                    } else if ( findMessage(full_message, ID_VOLTAGE_CURRENT) > 0) {
                        voltageView.setText(messageUpdate(full_message, findMessage(full_message, ID_VOLTAGE_CURRENT), MESSAGE_VOLTAGE) + " V");
                        currentView.setText(messageUpdate(full_message, findMessage(full_message, ID_VOLTAGE_CURRENT), MESSAGE_CURRENT) + " A");
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte [] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[ v >>> 4 ];
            hexChars[j * 2 + 1] = hexArray [v & 0x0F];
        }
        return new String(hexChars);
    }

    public int findMessage (String messageBuf, String address) {
        int i;
        for ( i = 0 ; i < 10; i ++ )
        {
            if (messageBuf.substring(i, i + 4).equals(address))
            {
                return i + 4;
            }
        }
        return 0;
    }

    public static String messageUpdate (String messageBuf, int messagePosition, int messageToUpdate) {
        // full message of one address, which contains several parameter messages
        String full_message;
        double message_value = 0;
        switch (messageToUpdate) {
            // the message contains soc, max temperature and min temperature
            case MESSAGE_SOC:
                full_message = messageBuf.substring(messagePosition, messagePosition + LENGTH_ID_SOC_TEMPERATURE);
                // message of soc is at the 4th byte of the byte string
                // resolution: 1.2 Offset: 0
                message_value = Integer.parseInt(full_message.substring(6, 8), 16) * 1.2;
                break;
            case MESSAGE_MAX_TEMPERATURE:
                full_message = messageBuf.substring(messagePosition, messagePosition + LENGTH_ID_SOC_TEMPERATURE);
                // message of max temperature is at the 7th byte of the byte string
                // resolution: 1.0 Offset: -50
                message_value = Integer.parseInt(full_message.substring(12, 14), 16) - 50;
                break;
            case MESSAGE_MIN_TEMPERATURE:
                full_message = messageBuf.substring(messagePosition, messagePosition + LENGTH_ID_SOC_TEMPERATURE);
                // message of min temperature is at the 3th byte of the byte string
                // resolution: 1.0 Offset: -50
                message_value = Integer.parseInt(full_message.substring(4, 6), 16) - 50;
                break;
            // the message contains voltage and current
            case MESSAGE_VOLTAGE:
                full_message = messageBuf.substring(messagePosition, messagePosition + LENGTH_ID_VOLTAGE_CURRENT);
                // message of min temperature is at the 7.7 ~ 8.0 byte of the byte string
                // resolution: 0.1  Offset: 0V
                message_value = Integer.parseInt(full_message.substring(12, 16), 16) * 0.1;
                break;
            case MESSAGE_CURRENT:
                full_message = messageBuf.substring(messagePosition, messagePosition + LENGTH_ID_VOLTAGE_CURRENT);
                // message of voltage is at the 5.7 ~ 6.0th bit of the byte string
                // resolution: 0.01 Offset: -327A
                message_value = Integer.parseInt(full_message.substring(8, 12), 16) * 0.01 - 327;
                break;

        }

        return String.valueOf(message_value).substring(0, 4);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
            case R.id.secure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            case R.id.insecure_connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                //ensureDiscoverable();
                mChatService.stop();
                return true;
        }
        return false;
    }

}

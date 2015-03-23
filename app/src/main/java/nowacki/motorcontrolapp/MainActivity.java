package nowacki.motorcontrolapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    private static int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                btAdapter.enable();
            }
        }

        Intent wykrywaczIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        wykrywaczIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(wykrywaczIntent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    BluetoothSocket nxtSocket;

    public void ClickPolacz(View view) {
        final String nxtMACadress = "00:16:53:14:46:E6";
        BluetoothDevice nxtDevice = btAdapter.getRemoteDevice(nxtMACadress);
        try {String a ="";
            nxtSocket = nxtDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            try {
                nxtSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch(IOException e){}
    }
    public void Dane(int motor, int speed){
        InputStream nxtInputStream;
        OutputStream nxtOutStream = null;
        try {
            nxtInputStream = nxtSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            nxtOutStream = nxtSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte message[] = new byte[12];
        int messageLength = message.length;
        byte DIRECT_COMMAND_NOREPLY = (byte) 0x80;
        byte SET_OUTPUT_STATE = 0x04;
        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = SET_OUTPUT_STATE;
        message[2] = (byte)motor;
        message[3] = (byte)speed; // Range: -100 to 100
        message[4] = 0x03; //MOTORON + BREAK
        message[5] = 0x01; // Regulation mode
        message[6] = 0x00;  // Turn Ratio
        message[7] = 0x20; // RunState
        message[8] = 0; // TachoLimit = run forever
        message[9] = 0;
        message[10] = 0;
        message[11] = 0;

        try{
            nxtOutStream.write(messageLength);
            nxtOutStream.write(messageLength>>8);
            nxtOutStream.write(message,0,messageLength);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void ClickWyslij(View view) {
        SeekBar sb = (SeekBar)findViewById(R.id.seekBar);
        Dane(0, 50);
        Dane(1, 50);
    }

    public void ClickStop(View view) {
        Dane(0,0);
        Dane(1,0);
    }

    public void ClickLewo(View view) {
        Dane(1,50);
        Dane(0,0);
    }

    public void ClickPrawo(View view) {
        Dane(0, 50);
        Dane(1, 0);
    }

    public void ClickTyl(View view) {
        Dane(0, -50);
        Dane(1, -50);
    }
}
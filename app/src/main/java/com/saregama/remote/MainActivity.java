package com.saregama.remote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_power)
    ImageButton btnPower;
    @BindView(R.id.btn_setting)
    ImageButton btnSetting;
    @BindView(R.id.btn_radio)
    Button      btnRadio;
    @BindView(R.id.btn_geetmala)
    Button      btnGeetmala;
    @BindView(R.id.btn_play)
    ImageButton btnPlay;
    @BindView(R.id.btn_next)
    ImageButton btnNext;
    @BindView(R.id.btn_prev)
    ImageButton btnPrev;
    @BindView(R.id.btn_vol_up)
    ImageButton btnVolUp;
    @BindView(R.id.btn_vol_down)
    ImageButton btnVolDown;

    BluetoothSocket btSocket = null;
    @BindView(R.id.cmd_text)
    AppCompatTextView cmdText;
    @BindView(R.id.btn_send)
    AppCompatButton   btnSend;
    @BindView(R.id.text)
    AppCompatEditText text;
    private      boolean isBtConnected = false;
    static final UUID    myUUID        = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter myBluetooth = null;
    String           address     = null;


    AlertDialog progress;
    private Set<BluetoothDevice> pairedDevices;
    ListView devicelist;

    private void pairedDevicesList() {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> localObject = new ArrayList();
        ArrayList<String> addresses   = new ArrayList<>();
        if (this.pairedDevices.size() > 0) {
            Iterator localIterator = this.pairedDevices.iterator();
            while (localIterator.hasNext()) {
                BluetoothDevice localBluetoothDevice = (BluetoothDevice) localIterator.next();
                ((ArrayList) localObject).add(localBluetoothDevice.getName() + "\n" + localBluetoothDevice.getAddress());
//                address = localBluetoothDevice.getAddress();
                ((ArrayList) addresses).add(localBluetoothDevice.getAddress());

            }
        }

        Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();


//        localObject = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, (List) localObject);
//        this.devicelist.setAdapter((ListAdapter) localObject);
//        this.devicelist.setOnItemClickListener(this.myListClickListener);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Select One Name:-");
        String[]       devies = localObject.toArray(new String[localObject.size()]);
        final String[] addre  = localObject.toArray(new String[addresses.size()]);

        builderSingle.setItems(devies, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                address = addre[which];
            }
        });


        builderSingle.show();


    }

    private void Disconnect() {
        if (this.btSocket != null) {
        }
        try {
            btSocket.close();
            finish();
            return;
        } catch (IOException localIOException) {
            msg("Error");
        }
    }

    private void msg(String paramString) {
        Toast.makeText(getApplicationContext(), paramString, Toast.LENGTH_LONG).show();
    }

    private void turnOffLed() {
        sendCommand("0");
    }

    private void turnOnLed() {
        sendCommand("1");
    }

    private void sendCommand(String cmd) {
        if (btSocket != null && !TextUtils.isEmpty(cmd)) {
            try {
                btSocket.getOutputStream().write(cmd.toString().getBytes());
                return;
            } catch (IOException localIOException) {
                msg("Error");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progress = new AlertDialog.Builder(this).create();
        progress.setTitle("Connecting");

        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null) {
            msg("Bluetooth Device Not Available");
            finish();
        }

        if (!this.myBluetooth.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }


    }

    @OnClick({R.id.btn_send,R.id.btn_power, R.id.btn_setting, R.id.btn_radio, R.id.btn_geetmala, R.id.btn_play, R.id.btn_next, R.id.btn_prev, R.id.btn_vol_up, R.id.btn_vol_down})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_power:
                new ConnectBT().execute();
                break;
            case R.id.btn_setting:
                startActivityForResult(new Intent(this, DeviceListActivity.class), 200);
                break;
            case R.id.btn_radio:
                turnOnLed();
                break;
            case R.id.btn_geetmala:
                turnOffLed();
                break;
            case R.id.btn_play:
                break;
            case R.id.btn_next:
                break;
            case R.id.btn_prev:
                break;
            case R.id.btn_vol_up:
                break;
            case R.id.btn_vol_down:
                break;
            case R.id.btn_send:
                String command = text.getText().toString();
                if (!TextUtils.isEmpty(command)) {
                    sendCommand("*"+command+"#");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK) {
            if (data != null) {
                address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            }
        }
    }


    private class ConnectBT
            extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        private ConnectBT() {
        }

        protected Void doInBackground(Void... paramVarArgs) {
            try {
                if ((btSocket == null) || (!isBtConnected)) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device;
                    device = myBluetooth.getRemoteDevice(address);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException ex) {
                this.ConnectSuccess = false;

            }
            return null;
        }

        protected void onPostExecute(Void paramVoid) {
            super.onPostExecute(paramVoid);
            if (!this.ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                progress.dismiss();
            }
        }

        protected void onPreExecute() {
            progress.show();
        }
    }


}

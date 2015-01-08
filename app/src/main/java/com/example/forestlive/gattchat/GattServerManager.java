package com.example.forestlive.gattchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

public class GattServerManager {

    private final String TAG = "GattServerManger";

    interface onGattServerManagerListener{
        public enum GattTYPE {
            SucADV,
            FilADV,
            StopADV,
            CONNECT,
            BEING,
            DISCONNECT,
        }

        public void onManageStateChange(GattTYPE type);
    }

    private onGattServerManagerListener myLisnaer = null;



    // Bluetooth
    private BluetoothManager mManager = null;
    private BluetoothAdapter mAdapter = null;

    // Perferal Advertiser
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser = null;

    // Server
    private BluetoothGattServer mGattServer = null;

    private Context mContext = null;

    public GattServerManager(Context context,onGattServerManagerListener listener){
        this.mContext = context;
        this.myLisnaer = listener;

        initBLE();
    }

    private void initBLE() {
        mManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = mManager.getAdapter();

        mBluetoothLeAdvertiser = mAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.d(TAG, "not LE Advertiser");
            Toast.makeText(mContext, "not LE Advertiser", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.d(TAG, "AdvertiseCallback onStartSuccess");
            // interface
            myLisnaer.onManageStateChange(onGattServerManagerListener.GattTYPE.SucADV);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.d(TAG, "AdvertiseCallback onStartFailure");
            // interface
            myLisnaer.onManageStateChange(onGattServerManagerListener.GattTYPE.FilADV);
        }
    };

    public void startAdvertise() {
        BluetoothGattService nameService = null;

        mGattServer = mManager.openGattServer(mContext,mGattServerVallback);
        nameService = new BluetoothGattService(UUID.fromString(Info.UUID_SAMPLE_NAME_SERVICE), BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic nameCharacteristic = new BluetoothGattCharacteristic(
                UUID.fromString(Info.UUID_SAMPLE_NAME_CHARACTERISTIC),
                BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE
        );
        nameService.addCharacteristic(nameCharacteristic);
        mGattServer.addService(nameService);

        mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(), createAdvData(), mAdvertiseCallback);
    }

    public void stopAdvertise(){
        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);

        // interface
        myLisnaer.onManageStateChange(onGattServerManagerListener.GattTYPE.StopADV);
    }

    private BluetoothGattServerCallback mGattServerVallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d("OUT", "切断 : " + device.getAddress());
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d("OUT", "接続 : " + device.getAddress());
                    break;
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG,"onCharacteristicReadRequest");
            characteristic.setValue("HELLO2");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            Log.d(TAG,"onCharacteristicWriteRequest");
            characteristic.setValue("HELLO");
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }
    };

    private AdvertiseData createAdvData() {

        AdvertiseData.Builder builder = new AdvertiseData.Builder();

        // サービスのUUIDの追加
//        builder.addServiceUuid() ;
        builder.addServiceUuid(ParcelUuid.fromString(Info.UUID_SAMPLE_NAME_SERVICE));

        /**
         * @param manufacturerId Manufacturer ID assigned by Bluetooth SIG.
         * https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
         */
        /**@param manufacturerSpecificData Manufacturer specific data
         * Manufacturer Specific Data(マニュファクチャラ スペシフィック データ)は、それぞれの企業の任意デー
        タに使われます。AD type は 0xFFです。Ad Dataは、先頭2オクテットが Bluetooth SIGが企業に発行した識
        別子、そして任意長のバイナリ・データが続きます。企業の識別子はCompany Identifiers documentsにリスト
        があります。 位置ビーコンのような、非接続で周囲の不特定多数のBluetooth LEデバイスに同報するときに、デ
        ータの格納に使えます。
         */

        // 格納方法
        byte mLeManufacturerData[] = {(byte) 0x4C, (byte) 0x00, (byte) 0x02, (byte) 0x15, (byte) 0x15, (byte) 0x15, (byte) 0x15};
        builder.addManufacturerData(0x3103 + 1, mLeManufacturerData);

        // 某BeaconなパケをAdvするためにはfalseにする必要があります。
        builder.setIncludeTxPowerLevel(false);

        // スキャン時のnameを表示する．
        builder.setIncludeDeviceName(true);

        return builder.build();
    }

    /**
     * AdvertiseSettingsに必要な４つの設定項目
     * 電波強度
     * 接続の設定
     * 検知可能状態の設定
     * 制御の設定
     */
    private AdvertiseSettings createAdvSettings() {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();

        /**強度の設定
         * ADVERTISE_TX_POWER_ULTRA_LOW
         * ADVERTISE_TX_POWER_LOW
         * ADVERTISE_TX_POWER_MEDIUM
         * ADVERTISE_TX_POWER_HIGH
         */
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);

        /** 接続可能か非接続かを解いています．
         true:接続可能
         flase:非接続
         */
        builder.setConnectable(true);

        /**接続時間の設定
         * 0の場合，接続時間を無効とする．
         * 最大の設定時間は180sです．
         */
        builder.setTimeout(0);

        /** 広告電力と待ち時間を制御するモード
         * ADVERTISE_MODE_LOW_POWER
         * ADVERTISE_MODE_BALANCED
         * ADVERTISE_MODE_LOW_LATENCY
         */
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);

        return builder.build();
    }
}

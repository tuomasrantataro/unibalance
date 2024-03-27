package com.unibalance.bleconnection

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.facebook.react.modules.core.ReactChoreographer.CallbackType
import com.unibalance.Manager
import java.math.BigInteger
import java.util.UUID

const val BLE_ADDRESS = "02:99:57:34:EF:FE"
const val SERVICE_UUID = "19b10000-e8f2-537e-4f6c-d104768a1214"
const val CHARACTERISTIC_UUID  = "19b10001-e8f2-537e-4f6c-d104768a1214"
const val NOTIFICATIONS_DESC_UUID = 0x2902
const val DESC = "00002902-0000-1000-8000-00805f9b34fb"

class BLEManager {

    companion object {
        private val TAG = "BLEManager"

        private var bluetooth: BluetoothManager? = null
        private var scanner: BluetoothLeScanner? = null
        private var selectedDevice : BluetoothDevice? = null
        private var gatt: BluetoothGatt? = null
        private var services: List<BluetoothGattService> = emptyList()
        private var sleepService: BluetoothGattService? = null
        private var sleepCharacteristic: BluetoothGattCharacteristic? = null

        private var ctx: Context? = null
        fun start(context: Context) {
            Log.d(TAG, "start called")
            ctx = context
            bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            scanner = bluetooth?.adapter?.bluetoothLeScanner

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG, "no ble permissions")
                return
            }
            scanner?.startScan(scanCallback)
            Log.d(TAG, "scan started")

            while (selectedDevice == null){
                Thread.sleep(1000)
                Log.d(TAG, "selectedDevice is null")
            }
            scanner?.stopScan(scanCallback)
            gatt = selectedDevice?.connectGatt(context, false, gattCallback)

            while (gatt == null) {
                Thread.sleep(1000)
                Log.d(TAG, "gatt is null")
            }
            Log.d(TAG, "gatt set, discovering services, $gatt")
            //gatt?.discoverServices()


            var tries = 0
            while (services.isEmpty()) {
                Thread.sleep(5000)
                tries += 1
                Log.d(TAG, "no services found:  $tries")
                if (tries > 10) {
                    break
                }
            }

            sleepService = gatt?.getService(UUID.fromString(SERVICE_UUID))
            Log.d(TAG, "sleep service: $sleepService")

            sleepCharacteristic = sleepService?.getCharacteristic(UUID.fromString(
                CHARACTERISTIC_UUID))
            Log.d(TAG, "sleep characteristic: $sleepCharacteristic")

            gatt?.setCharacteristicNotification(sleepCharacteristic, true)
            val CLIENT_CONFIG_DESCRIPTOR = UUID.fromString(DESC)
            val desc = sleepCharacteristic?.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
            desc?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt?.writeDescriptor(desc)
        }

        fun reset(context: Context) {
            Log.d(TAG, "stop scanning and clear manager and scanner")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.d(TAG, "no permission to stop ble scan")
                return
            }
            scanner?.stopScan(scanCallback)
            gatt?.disconnect()
            gatt = null
            services = emptyList()
            selectedDevice = null
            scanner = null
            bluetooth = null

            Manager.stop(ctx!!)
        }

        private val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                if (result != null) {
                    Log.d(TAG, "ad: ${result.device.address}  na: ${result.device.name}")
                    if (result.device.address == BLE_ADDRESS) {
                        selectedDevice = result.device
                        Log.d(TAG, "device set: ${selectedDevice}")
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.d(TAG, "Scan failed")
            }
        }

        private val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d(TAG, "gattCallback")
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    // handle error
                    Log.d(TAG, "Gatt connect error")
                    return
                }

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // handle connect
                    Log.d(TAG, "gatt connected, ${gatt}")
                    gatt?.discoverServices()

                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                Log.d(TAG, "oncServicesDiscovered")
                super.onServicesDiscovered(gatt, status)
                services = gatt.services
                Log.d(TAG, "services discovered: $services")
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                super.onCharacteristicChanged(gatt, characteristic, value)
                val num = BigInteger(value).toInt()
                Log.v(TAG, "char read: $num")
                if (ctx != null) {
                    if (num == 1 && !Manager.started) {
                        Log.v(TAG, "start alarm")
                        Manager.start(ctx!!, "asfd")
                    }
                }
            }

            override fun onDescriptorWrite(
                gatt: BluetoothGatt?,
                descriptor: BluetoothGattDescriptor?,
                status: Int
            ) {
                super.onDescriptorWrite(gatt, descriptor, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "descriptor write successful")
                } else {
                    Log.d(TAG, "descriptor write failed")
                }
            }
        }
    }
}
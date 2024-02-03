/*
package ru.astar.btarduinoapp

import android.Manifest
import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pixplicity.easyprefs.library.Prefs
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    OnItemClickListener, View.OnClickListener {
    private var frameMessage: FrameLayout? = null
    private var frameControls: LinearLayout? = null
    private var frameLedControls: RelativeLayout? = null
    private var btnDisconnect: Button? = null
    private var switchRedLed: Switch? = null
    private var switchIc: Switch? = null
    private var switchEnableBt: Switch? = null
    private var btnEnableSearch: Button? = null
    private var pbProgress: ProgressBar? = null
    private var listBtDevices: ListView? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var listAdapter: BtListAdapter? = null
    private var bluetoothDevices: ArrayList<BluetoothDevice>? = null
    private var btnPlus: Button? = null
    private var btnMinus: Button? = null
    private var btnUpload: Button? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    var save: String? = null
    private var btnBtnPlusAddress: Button? = null
    private var btnBtnMinusAddress: Button? = null
    private var address: TextView? = null

    protected fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        frameMessage = findViewById(R.id.frame_message)
        frameControls = findViewById(R.id.frame_control)
        switchEnableBt = findViewById(R.id.switch_enable_bt)
        btnEnableSearch = findViewById(R.id.btn_enable_search)
        pbProgress = findViewById(R.id.pb_progress)
        listBtDevices = findViewById(R.id.lv_bt_device)
        frameLedControls = findViewById(R.id.frameLedControls)
        btnDisconnect = findViewById(R.id.btn_disconnect)
        switchIc = findViewById(R.id.switch_ic)
        switchRedLed = findViewById(R.id.switch_led_red)
        btnPlus = findViewById(R.id.btn_plus)
        btnMinus = findViewById(R.id.btn_minus)
        btnUpload = findViewById(R.id.button_upload)
        btnBtnPlusAddress = findViewById(R.id.plus_address)
        btnBtnMinusAddress = findViewById(R.id.minus_address)
        address = findViewById(R.id.address)
        btnPlus!!.setOnClickListener(this)
        btnMinus!!.setOnClickListener(this)
        switchEnableBt!!.setOnCheckedChangeListener(this)
        btnEnableSearch!!.setOnClickListener(this)
        listBtDevices!!.onItemClickListener = this
        btnDisconnect!!.setOnClickListener(this)
        switchIc!!.setOnCheckedChangeListener(this)
        switchRedLed!!.setOnCheckedChangeListener(this)
        bluetoothDevices = ArrayList()
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val btPlus = View.OnClickListener {
            address!!.text = (Integer.valueOf(address!!.text.toString()) + 1).toString()
            //                    address.setText("123");
        }
        btnBtnPlusAddress!!.setOnClickListener(btPlus)
        val btMinus =
            View.OnClickListener {
                address!!.text = (Integer.valueOf(address!!.text.toString()) - 1).toString()
            }
        btnBtnMinusAddress!!.setOnClickListener(btMinus)
        val upload = View.OnClickListener {
            connectedThread!!.write(
                Integer.valueOf(address!!.text.toString()).toString()
            )
        }
        btnUpload!!.setOnClickListener(upload)
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "onCreate: " + getString(R.string.bluetooth_not_supported))
            finish()
        }
        if (bluetoothAdapter.isEnabled()) {
            showFrameControls()
            switchEnableBt!!.isChecked = true
            setListAdapter(BT_BOUNDED)
        }
        if (!Prefs.getString("timetable", "").isEmpty()) {
            val s: String = Prefs.getString("timetable", "")
            enableLedSave(s)
        }
    }

    protected fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        if (connectThread != null) {
            connectThread!!.cancel()
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
        }
    }

    override fun onClick(v: View) {
        if (v == btnEnableSearch) {
            enableSearch()
        } else if (v == btnDisconnect) {
            // TODO отключение от устройства
            if (connectedThread != null) {
                connectedThread!!.cancel()
            }
            if (connectThread != null) {
                connectThread!!.cancel()
            }
            showFrameControls()
        } else if (v == btnPlus) {
            enableLedPlus(BT_PLUS)
        } else if (v == btnMinus) {
            enableLedMinus(BT_MINUS)
        } else if (v == btnUpload) {
            uploadData()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent == listBtDevices) {
            val device = bluetoothDevices!![position]
            if (device != null) {
                connectThread = ConnectThread(device)
                connectThread!!.start()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView == switchEnableBt) {
            enableBt(isChecked)
            if (!isChecked) {
                showFrameMessage()
            }
        } else if (buttonView == switchRedLed) {
            // TODO включение или отключение красного светодиода
            enableLed(LED_RED, isChecked)
        } else if (buttonView == switchIc) {
            enableLed2(LED_RED, isChecked)
        }
    }

    protected fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == REQ_ENABLE_BT) {
            if (resultCode == RESULT_OK && bluetoothAdapter!!.isEnabled) {
                showFrameControls()
                setListAdapter(BT_BOUNDED)
            } else if (resultCode == RESULT_CANCELED) {
                enableBt(true)
            }
        }
    }

    private fun showFrameMessage() {
        frameMessage!!.visibility = View.VISIBLE
        frameLedControls!!.visibility = View.GONE
        frameControls!!.visibility = View.GONE
    }

    private fun showFrameControls() {
        frameMessage!!.visibility = View.GONE
        frameLedControls!!.visibility = View.GONE
        frameControls!!.visibility = View.VISIBLE
    }

    private fun showFrameLedControls() {
        frameLedControls!!.visibility = View.VISIBLE
        frameMessage!!.visibility = View.GONE
        frameControls!!.visibility = View.GONE
    }

    private fun enableBt(flag: Boolean) {
        if (flag) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ActivityCompat.startActivityForResult(intent, REQ_ENABLE_BT)
        } else {
            bluetoothAdapter!!.disable()
        }
    }

    private fun setListAdapter(type: Int) {
        bluetoothDevices!!.clear()
        var iconType: Int = R.drawable.ic_bluetooth_bounded_device
        when (type) {
            BT_BOUNDED -> {
                bluetoothDevices = boundedBtDevices
                iconType = R.drawable.ic_bluetooth_bounded_device
            }
            BT_SEARCH -> iconType = R.drawable.ic_bluetooth_search_device
        }
        listAdapter = BtListAdapter(this, bluetoothDevices, iconType)
        listBtDevices!!.adapter = listAdapter
    }

    private val boundedBtDevices: ArrayList<BluetoothDevice>
        private get() {
            val deviceSet = bluetoothAdapter!!.bondedDevices
            val tmpArrayList = ArrayList<BluetoothDevice>()
            if (deviceSet.size > 0) {
                for (device in deviceSet) {
                    tmpArrayList.add(device)
                }
            }
            return tmpArrayList
        }

    private fun enableSearch() {
        if (bluetoothAdapter!!.isDiscovering) {
            bluetoothAdapter!!.cancelDiscovery()
        } else {
            accessLocationPermission()
            bluetoothAdapter!!.startDiscovery()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    btnEnableSearch.setText(R.string.stop_search)
                    pbProgress!!.visibility = View.VISIBLE
                    setListAdapter(BT_SEARCH)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    btnEnableSearch.setText(R.string.start_search)
                    pbProgress!!.visibility = View.GONE
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) {
                        bluetoothDevices!!.add(device)
                        listAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    /**
     * Запрос на разрешение данных о местоположении (для Marshmallow 6.0)
     */
    private fun accessLocationPermission() {
        val accessCoarseLocation: Int =
            this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val accessFineLocation: Int =
            this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val listRequestPermission: MutableList<String> = ArrayList()
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!listRequestPermission.isEmpty()) {
            val strRequestPermission = listRequestPermission.toTypedArray()
            this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOC -> if (grantResults.size > 0) {
                for (gr in grantResults) {
                    // Check if request is granted or not
                    if (gr != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                }
                //TODO - Add your code here to start Discovery
            }
            else -> return
        }
    }

    inner class ConnectThread(device: BluetoothDevice) : Thread() {
        private var bluetoothSocket: BluetoothSocket? = null
        private var success = false

        init {
            try {
                val method = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf<Class<*>?>(
                        Int::class.javaPrimitiveType
                    )
                )
                bluetoothSocket = method.invoke(device, 1) as BluetoothSocket
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                bluetoothSocket!!.connect()
                success = true
            } catch (e: IOException) {
                e.printStackTrace()
                UiThreadStatement.runOnUiThread(Runnable {
                    Toast.makeText(
                        this@MainActivity,
                        "Не могу соединиться!",
                        Toast.LENGTH_SHORT
                    ).show()
                })
                cancel()
            }
            if (success) {
                connectedThread = ConnectedThread(bluetoothSocket)
                connectedThread!!.start()
                UiThreadStatement.runOnUiThread(Runnable { showFrameLedControls() })
            }
        }

        val isConnect: Boolean
            get() = bluetoothSocket!!.isConnected

        fun cancel() {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    inner class ConnectedThread(bluetoothSocket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        init {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                inputStream = bluetoothSocket.inputStream
                outputStream = bluetoothSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }
            this.inputStream = inputStream
            this.outputStream = outputStream
        }

        override fun run() {}
        fun write(command: String) {
            val bytes = command.toByteArray()
            if (outputStream != null) {
                try {
                    outputStream.write(bytes)
                    outputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun cancel() {
            try {
                inputStream!!.close()
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun enableLed2(ledRed: Int, isChecked: Boolean) {
        if (connectedThread != null && connectThread!!.isConnect) {
            var command = ""
            when (ledRed) {
                LED_RED -> command = if (isChecked) "00" else "11"
                else -> {}
            }
            connectedThread!!.write("command")
        }
    }

    private fun enableLedPlus(ledRed: Int) {
        if (connectedThread != null && connectThread!!.isConnect) {
            var command = ""
            when (ledRed) {
                BT_PLUS -> command = "+"
                else -> {}
            }
            connectedThread!!.write(command)
        }
    }

    //    ---------------------------------------------------------------------------------------------------------------------
    private fun uploadData() {
        if (connectedThread != null && connectThread!!.isConnect) {
            val command = ""
            connectedThread!!.write("1")
        }
    }

    private fun addressPlus() {
        address!!.setText(Integer.valueOf(address!!.text.toString()) + 1)
        address!!.text = "123"
    }

    private fun enableLedMinus(ledRed: Int) {
        if (connectedThread != null && connectThread!!.isConnect) {
            var command = ""
            when (ledRed) {
                BT_MINUS -> command = "-"
                else -> {}
            }
            connectedThread!!.write(command)
        }
    }

    private fun enableLedSave(save: String) {
        if (connectedThread != null && connectThread!!.isConnect) {
            connectedThread!!.write(save)
        }
    }

    private fun enableLed(led: Int, state: Boolean) {
        if (connectedThread != null && connectThread!!.isConnect) {
            var command = ""
            when (led) {
                LED_RED -> command = if (state) "1" else "0"
                else -> {}
            }
            connectedThread!!.write(command)
        }
    }

    fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.time_table -> {
                val timeTable = Intent(this, TimeTable::class.java)
                ContextCompat.startActivity(timeTable)
            }
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun onResume() {
        super.onResume()
        if (connectedThread != null && connectThread!!.isConnect) {
            var command = ""
            command = Prefs.getString("timetable", "")
            connectedThread!!.write(command)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val REQUEST_CODE_LOC = 1
        private const val REQ_ENABLE_BT = 10
        const val BT_BOUNDED = 21
        const val BT_SEARCH = 22
        const val LED_RED = 30
        const val BT_PLUS = 31
        const val BT_MINUS = 32
    }
}
*/
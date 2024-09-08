package com.example.aguadeoromanagement.fragments

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.Navigation
import com.example.aguadeoromanagement.ManagementApplication
import com.example.aguadeoromanagement.R
import com.example.aguadeoromanagement.enums.InventoryCheckState
import com.example.aguadeoromanagement.screens.CheckInventoryScreen
import com.example.aguadeoromanagement.screens.InventoryScreen
import com.example.aguadeoromanagement.ui.theme.ManagementTheme
import com.example.aguadeoromanagement.viewmodels.InventoryViewModel
import com.reader.bluetooth.lib.builder.vh.vh88.VH88IBluetoothRFIDReader
import com.reader.bluetooth.lib.listener.ConnectCallback
import kotlinx.coroutines.*
import java.lang.reflect.Method

//import com.example.screens.PaymentListScreen

class InventoryFragment : Fragment() {

    private val REQUEST_BLUETOOTH_PERMISSION = 2
    lateinit var connect: () -> Unit
    private var bluetoothDevice: BluetoothDevice? = null
    var connectThread: ConnectThread? = null

    var startScanLocation: () -> Unit = {}
    var stopScanLocation: () -> Unit = {}
    var showPopup: () -> Unit = {}
    var finishCheck: () -> Unit = {}
    var scanLocationStarted = false
    var stopTimer: () -> Unit = {}
    var updateInventoryCheck: (newState: InventoryCheckState) -> Unit = { }
    lateinit var mainMenu: Menu
    var continueInventoryCheck: () -> Unit = {}
    var saveLocation: () -> Unit = {}
    var showSummaryLocation: () -> Unit = {}
    var saveCheck: () -> Unit = {}
    var startTransfer: () -> Unit = {}

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_inventory, menu)
        mainMenu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_connect_reader -> {
                if (connectThread?.connected == false) {
                    connectThread?.run { title ->
                        try {
                            requireActivity().runOnUiThread {
                                item.title = title
                            }
                        } catch (e: Exception) {

                        }

                    }
                    connectThread?.connected = true
                } else {
                    connectThread?.closeConnection()

                    Log.e("thread already started", "")
                }
            }

            R.id.action_init_location -> {
                if (connectThread?.connected == true) {
                    scanLocationStarted = !scanLocationStarted
                    if (scanLocationStarted) {
                        item.title = "Stop scan"
                        startScanLocation()
                    } else {
                        item.title = "Start Scan"
                        stopScanLocation()
                    }
                } else {
                    Toast.makeText(requireContext(), "Connect reader first", Toast.LENGTH_LONG)
                        .show()
                }
            }

            R.id.action_update_location -> {
                if (ManagementApplication.isAdmin()) {
                    showPopup()
                } else {
                    Toast.makeText(
                        ManagementApplication.getAppContext(),
                        "You must be admin, please log in",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            R.id.action_finish_check -> {
                finishCheck()
            }

            R.id.action_start_check_inventory -> {
                updateInventoryCheck(InventoryCheckState.check_started)
            }

            R.id.action_continue_check_inventory -> {
                updateInventoryCheck(InventoryCheckState.check_started)
                continueInventoryCheck()
            }

            R.id.action_save_location -> {
                saveLocation()
            }

            R.id.action_show_location_summary -> {
                showSummaryLocation()
            }

            R.id.action_start_transfer -> {
                startTransfer()
            }
//
        }
        return super.onOptionsItemSelected(item)

    }

    fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method =
                device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            //                throw IllegalStateException(e)
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        DisconnectThread(bluetoothDevice, requireContext()).run()
        Log.e("destroy", ":((")
        connectThread?.closeConnection()
        stopTimer()
//        connectThread?.destroy()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 0)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // Device doesn't support Bluetooth, handle accordingly.


        if (!bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled, request to turn it on.
            // You should implement the code to request Bluetooth enable here.
            Log.e("enabled", "not")

        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            Log.e("paired device !", deviceName)
            if (deviceName == "RFID0") {
                bluetoothDevice = device

                connectThread =
                    ConnectThread(
                        device,
                        requireContext(),
                    )
            }
        }

        return ComposeView(requireContext()).apply {

            setContent {
                ManagementTheme {
                    val viewModel: InventoryViewModel = viewModel()
                    val state = viewModel.inventoryListState.value
                    val coroutineScope = rememberCoroutineScope()


                    LaunchedEffect(key1 = true, block = {
                        viewModel.viewModelScope.launch {
                            val currentCheckID = viewModel.retrieveCurrentCheck()
//                            Log.e("main menu", mainMenu.toString())
                            if (currentCheckID == -1) {

                                mainMenu.findItem(R.id.action_continue_check_inventory).isVisible =
                                    false
                                mainMenu.findItem(R.id.action_start_check_inventory).isVisible =
                                    true
                            } else {
                                mainMenu.findItem(R.id.action_start_check_inventory).isVisible =
                                    false
                                mainMenu.findItem(R.id.action_continue_check_inventory).isVisible =
                                    true

                            }

                        }

                    })

                    stopTimer = { viewModel.timer.cancel() }


                    connectThread?.updateInventory = { newTag: String ->
                        viewModel.updateInventory(
                            newTag
                        )
                    }
                    connectThread?.updateConnectionStatus = { status: Boolean ->
                        viewModel.updateConnectionStatus(
                            status
                        )
                    }

                    startTransfer = {
                        coroutineScope.launch {
                            if (viewModel.startTransfer()) {
                                mainMenu.findItem(R.id.action_start_transfer).title =
                                    "Finish Transfer"
                            } else {
                                mainMenu.findItem(R.id.action_start_transfer).title =
                                    "Start Transfer"
                            }

                        }
                    }

                    startScanLocation = {
                        viewModel.scanningLocation.value = !viewModel.scanningLocation.value
                        viewModel.startTimer()
                    }
                    stopScanLocation = {
                        viewModel.scanningLocation.value = !viewModel.scanningLocation.value

                        viewModel.timer.cancel()
                    }

                    showPopup = {
                        viewModel.showPopup.value = true
                    }

                    finishCheck = {
                        viewModel.goToCheckInventory()
                    }

                    updateInventoryCheck =
                        { newState -> viewModel.inventoryCheckState.value = newState }

                    continueInventoryCheck = { viewModel.continueInventoryCheck() }

                    saveLocation = { viewModel.saveLocation() }

                    showSummaryLocation = { viewModel.showPopupSummaryLocation.value = true }

                    saveCheck = {
                        coroutineScope.launch {
                            viewModel.saveCheck()
                        }
                    }

                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.BLUETOOTH
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(android.Manifest.permission.BLUETOOTH),
                            REQUEST_BLUETOOTH_PERMISSION
                        )
                    } else {
                        // Bluetooth permission is already granted, proceed with the app setup
                    }

                    if (!viewModel.showCheckScreen.value) {
                        InventoryScreen(
//                            inventoryList = state.inventoryList,
//                            newItems = viewModel.newInventoryList.value,
//                            loading = state.loading,
                            deviceConnected = viewModel.deviceConnected.value,
//                            currentItem = state.currentItem,
//                            currentLocation = state.currentLocation,
//                            totalLocation = state.totalLocation,
//                            locationList = state.allLocations,
                            inventoryState = state,
                            scanTag = { inventory ->
                                viewModel.updateCurrentItem(inventory)
                            },
                            updateInventoryItem = { inventory ->
                                withContext(Dispatchers.IO) {
                                    return@withContext viewModel.updateInventoryItem(inventory)
                                }
                            },
                            getInventoryFromRfidTag = { tag: String ->
                                viewModel.getInventoryFromRfidTag(
                                    tag
                                )
                            },
                            fetchNewLocation = { newVal: Int ->
                                coroutineScope.launch {
                                    viewModel.fetchLocationInventory(newVal)
                                }
                            },

                            scanningLocation = viewModel.scanningLocation.value,
                            detectedCodes = viewModel.detectedInventoryCodes.value,
                            removeNewItem = { code: String, tag: String ->
                                viewModel.removeNewItem(code, tag)
                            },
                            newInventoryList = viewModel.newInventoryList.value,
                            updateLocation = { ok: Boolean ->
                                if (ok) {
                                    coroutineScope.launch {
                                        viewModel.updateLocation(state.currentLocation + 1)
                                    }
                                }
                                viewModel.showPopup.value = false
                            },
                            showPopup = viewModel.showPopup.value,
                            finishLocation = { viewModel.saveLocation() },
                            insertingItem = viewModel.insertingItems.value,
                            inventoryCheckState = viewModel.inventoryCheckState.value,
                            addRemoveInventory = { inventory, add ->
                                viewModel.addRemoveCode(
                                    inventory,
                                    add
                                )
                            },
                            showPopupSummaryLocation = viewModel.showPopupSummaryLocation.value,
                            locationItemNumber = viewModel.locationItemsNumber.value,
                            dismissSummary = { viewModel.showPopupSummaryLocation.value = false },
                            navigateToProduct = { inventoryCode: String ->
                                val action =
                                    InventoryFragmentDirections.actionInventoryToSingleInventory(
                                        inventoryCode
                                    )
                                Navigation.findNavController(
                                    requireView()
                                ).navigate(action)
                            },
                            showMissingLocation = viewModel.showMissingLocation.value,
                            changeShowMissing = {
                                coroutineScope.launch {
                                    viewModel.changeShowMissing()
                                }
                            },
                            addRemoveSelected = { code, remove ->
                                viewModel.addRemoveSelected(
                                    code,
                                    remove
                                )
                            },
                            validateSelected = { validate, index ->
                                coroutineScope.launch {
                                    viewModel.validateSelected(validate, index)
                                }
                            }


                        )
                    } else {
                        CheckInventoryScreen(
                            scannedData = viewModel.scannedLocationData.value.filter { it.value.scanned },
//                            scannedData = viewModel.scannedLocationData.value,
                            viewModel.imageLinks.value,
                            updateLocation = {
                                coroutineScope.launch {
                                    viewModel.validateCheck()
                                }
                            },
                            savingInventoryReport = viewModel.savingInventoryReport.value,
                            totalLocation = state.totalLocation,
                            locations = state.allLocations,
                            goToProduct = { inventoryCode ->
                                val action =
                                    InventoryFragmentDirections.actionInventoryToSingleInventory(
                                        inventoryCode
                                    )
                                Navigation.findNavController(
                                    requireView()
                                ).navigate(action)
                            },
                            closeCheck = {
                                coroutineScope.launch {
                                    viewModel.closeCheck()
                                }
                            },
                            loading = viewModel.checkScreenLoading.value
                        )
                    }
                }
            }
        }
    }
}

class ConnectThread(
    private val device: BluetoothDevice,
    val context: Context,
) {
    var updateInventory: (newTag: String) -> Unit = {}
    var updateConnectionStatus: (connected: Boolean) -> Unit = {}

    val bluetoothReader = VH88IBluetoothRFIDReader(context, device)
    var connected = false
    fun run(updateTitle: (title: String) -> Unit) {
        Log.e("hey ?", bluetoothReader.device.toString())
        connected = true


        val callback = object : ConnectCallback {
            override fun onConnect() {

                updateTitle("Disconnect Reader")
                updateConnectionStatus(true)
                connected = true
                bluetoothReader.addBluetoothReadTagListener {


                    Log.e("tag ", it.epc)
//                    inventory.rfidTag = it.epc
                    updateInventory(it.epc)


                }
                Log.e("connected", "yes")
            }

            override fun onDisconnect() {
                updateTitle("Connect Reader")

                connected = false
                updateConnectionStatus(false)

                Log.e("disconnected", "adios")
            }

            override fun onError() {
                updateTitle("Connect Reader")


                connected = false
                updateConnectionStatus(false)

                Log.e("error", ":(")
            }
        }

        bluetoothReader.connect(callback)
//        sleep(5000)
//        bluetoothReader.close()
    }

    fun closeConnection() {
        Log.e("closeConnection", "on ferme")
        Log.e("he", bluetoothReader.device.toString())

        bluetoothReader.close()
    }

}
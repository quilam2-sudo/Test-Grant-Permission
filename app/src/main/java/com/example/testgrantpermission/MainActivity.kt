package com.example.testgrantpermission

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.MenuProvider
import com.example.testgrantpermission.ui.theme.TestGrantPermissionTheme

import com.datalogic.device.app.AppManager

val TAG: String = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestGrantPermissionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(16.dp)) {
                        Greeting(
                            name = "Datalogic"
                        )
                        Body()
                    }
                }
            }
        }
    }

    override fun addMenuProvider(
        p0: MenuProvider,
        p1: androidx.lifecycle.LifecycleOwner,
        p2: androidx.lifecycle.Lifecycle.State
    ) {
        TODO("Not yet implemented")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun Body(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appManager = AppManager(context.applicationContext)

    val permission = Manifest.permission.SCHEDULE_EXACT_ALARM
    // --- STATE IMPLEMENTATION ---
    // This allows the UI to update when the value changes
    var permissionStatus by remember { mutableStateOf("Unknown") }

    permissionStatus = checkPermission(context)

    Text(permission, style = MaterialTheme.typography.bodyLarge,
        modifier = modifier.padding(bottom = 16.dp))

    Text("AppOps Management", style = MaterialTheme.typography.labelLarge)
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Button(modifier = modifier.weight(1.3f),
            onClick = {
                val res = appManager.setModeAppOps(context.packageName,
                    listOf(permission), AppOpsManager.MODE_ALLOWED)
                Log.i(TAG, "Grant permission - setModeAppOps - res: $res")

                permissionStatus = checkPermission(context)
            }) {
            Text(
                text = "setModeAppOps"
            )
        }
        Button(modifier = Modifier.weight(0.7f),
            onClick = {
                //  Note: Revoke permission does not work, the permission will be granted again immediately after revoking.
                val res = appManager.setModeAppOps(context.packageName,
                    listOf(permission), AppOpsManager.MODE_DEFAULT)
                Log.i(TAG, "Revoke permission - setModeAppOps - res: $res")

                permissionStatus = checkPermission(context)
            }) {
            Text(
                text = "Revoke"
            )
        }
    }

    Button(modifier = modifier.fillMaxWidth(),
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Not needed on API < 31", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text(text = "Request from User")
    }

    Button(modifier = modifier.fillMaxWidth(),
        onClick = {
            permissionStatus = checkPermission(context)
        }
    ) {
        Text(text = "Check")
    }

    Row(modifier = Modifier.fillMaxWidth()
        .padding(16.dp)) {
        Text(
            text = "Permission Status: ",
            modifier = modifier
                .weight(1.3f)
        )
        Text(
            text = permissionStatus,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = modifier.weight(0.7f)
        )
    }

}

fun checkPermission(context: Context): String {
    Log.i(TAG, "Checking permission...")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    // UPDATE THE STATE HERE
    var permissionStatus = if (isGranted) "GRANTED" else "DENIED"
    Log.d(TAG, "UI Updated to: $permissionStatus")

    return permissionStatus
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestGrantPermissionTheme {
        Greeting("Datalogic")
    }
}
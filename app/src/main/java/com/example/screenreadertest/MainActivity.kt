package com.example.screenreadertest

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.unit.sp
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.screenreadertest.ui.theme.ScreenreadertestTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth


class MainActivity : ComponentActivity() {
    companion object {
        public const val REQUEST_CODE_EXPORT = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenreadertestTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        context = this,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EXPORT && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                DeliveryEventManager.exportJsonToUri(this@MainActivity, uri)
                Toast.makeText(this@MainActivity, "백업 완료!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun MainScreen(
    context: Context,
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var stats by remember { mutableStateOf(viewModel.getThisMonthStats(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                stats = viewModel.getThisMonthStats(context)
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD488))
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(24.dp))
        Image(
            painter = painterResource(id = R.drawable.title),
            contentDescription = "그돈씨 TITLE",
            modifier = Modifier
                .height(150.dp)
                .padding(bottom = 12.dp)
        )
        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("멈춘 횟수", stats["stopCount"] ?: "-", "회", fontSize = 18.sp) {
                context.startActivity(Intent(context, StopDetailActivity::class.java))
            }
            InfoCard("아낀 금액", stats["savedAmount"] ?: "-", "원", fontSize = 18.sp) {
                context.startActivity(Intent(context, SavedAmountDetailActivity::class.java))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InfoCard("배달한 횟수", stats["orderCount"] ?: "-", "회",fontSize = 18.sp){
                context.startActivity(Intent(context, StopDetailActivity::class.java))
            }
            InfoCard("배달한 금액", stats["orderAmount"] ?: "-", "원",fontSize = 18.sp)
             {
                context.startActivity(Intent(context, SavedAmountDetailActivity::class.java))
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                openAccessibilitySettings(context)

                // Test Dummy
//                DeliveryEventManager.insertDummyData(context)

                // Reset Data
//                DeliveryEventManager.resetAllEvents(context)
                      },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF444444),
                contentColor = Color.White
            )
        ) {
            Text("접근성 설정 열기", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "ordered_data_backup.json")
            }
            (context as? Activity)?.startActivityForResult(intent, MainActivity.REQUEST_CODE_EXPORT)
        },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF444444),
                contentColor = Color.White
            )
        ) {
            Text("백업 파일 저장하기", fontSize = 18.sp)
        }
    }
}

fun openAccessibilitySettings(context: Context) {
    val auth = Firebase.auth

    auth.signInAnonymously()
        .addOnCompleteListener()
        { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("LoginUser", "로그인 성공")
                val user = auth.currentUser
            } else {
                // If sign in fails, display a message to the user.
                Log.d("LoginUser", "로그인 실패")
            }

            Log.d("LoginUser", "UID: ${auth.currentUser?.uid ?: "No user"}")
        }
    Log.d("AccessibilityService", "접근성 설정 버튼 클릭")
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

@Composable
fun InfoCard(
    label: String,
    number: String,
    unit: String = "",
    fontSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    val numberFontSize = remember(fontSize) { TextUnit(fontSize.value + 12, fontSize.type) }

    Card(
        modifier = Modifier
            .size(width = 150.dp, height = 150.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD9823F)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = number,
                    fontSize = numberFontSize, // 🔥 이게 핵심
                    fontWeight = FontWeight.Bold,

                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = fontSize,

                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val context = LocalContext.current
    ScreenreadertestTheme {
        MainScreen(context = context)
    }
}


package ir.dakhlokharj.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = lightColorScheme(primary = Indigo)) {
                    Root()
                }
            }
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun Root(vm: MainVM = viewModel()) {
    val items = listOf(
        NavItem("خانه", Icons.Default.Home),
        NavItem("تراکنش‌ها", Icons.Default.Menu),
        NavItem("اقساط", Icons.Default.Info),
        NavItem("اهداف", Icons.Default.Star)
    )
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                items.forEachIndexed { i, it ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) }
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (tab) {
                0 -> HomeScreen(vm)
                1 -> TxScreen(vm)
                2 -> InstallmentsScreen(vm)
                else -> GoalsScreen(vm)
            }
        }
    }
}

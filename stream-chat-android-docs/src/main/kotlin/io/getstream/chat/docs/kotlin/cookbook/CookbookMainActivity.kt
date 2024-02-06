package io.getstream.chat.docs.kotlin.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.getstream.chat.docs.kotlin.cookbook.ui.CustomChannelListScreen
import io.getstream.chat.docs.kotlin.cookbook.ui.CustomMessageListScreen
import io.getstream.chat.docs.kotlin.cookbook.ui.theme.CookbookTheme
import io.getstream.chat.docs.kotlin.cookbook.utils.connectUser
import io.getstream.chat.docs.kotlin.cookbook.utils.initChatClient
import io.getstream.chat.docs.kotlin.cookbook.utils.userCredentials
import kotlinx.coroutines.launch

class CookbookMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()

        setContent {
            val navController = rememberNavController()

            CookbookTheme {
                NavHost(
                    navController = navController,
                    startDestination = AppScreens.CustomChannelList.route
                ) {
                    composable(AppScreens.CustomChannelList.route) {
                        CustomChannelListScreen(
                            navigateToMessageList = { cid ->
                                navController.navigate(AppScreens.CustomMessageList.routeWithArg(cid))
                            }
                        )
                    }

                    composable(AppScreens.CustomMessageList.route) { backStackEntry ->
                        CustomMessageListScreen(
                            cid = backStackEntry.arguments?.getString("cid")
                        )
                    }
                }
            }
        }
    }

    private fun init() {
        initChatClient(userCredentials.apiKey, this.applicationContext)
        lifecycleScope.launch { connectUser() }
    }
}

enum class AppScreens(val route: String) {
    CustomChannelList("channel_list"),
    CustomMessageList("message_list/{cid}");

    fun routeWithArg(argValue: Any): String = when (this) {
        CustomMessageList -> this.route.replace("{cid}", argValue.toString())
        else -> this.route
    }
}

package io.getstream.chat.docs.kotlin.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.docs.kotlin.cookbook.ui.CustomChannelListScreen
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
            CookbookTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    CustomChannelListScreen()
                }
            }
        }
    }

    private fun init() {
        initChatClient(userCredentials.apiKey, this.applicationContext)
        lifecycleScope.launch { connectUser() }
    }
}
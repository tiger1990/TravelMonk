package com.travelmonk.testui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelmonk.core.design.system.color.BackgroundLight
import com.travelmonk.core.design.system.color.TravelBlue
import com.travelmonk.core.design.system.theme.TravelMonkTheme

data class Message(val author: String, val body: String)

@Preview(name = "Bookings – Light", showBackground = true)
@Preview(
    name = "Bookings – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TestUiElements() {
    val message = Message("JetBrains", "This is sample message")
    MessageCard(message)
}

@Composable
fun MessageCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(android.R.drawable.ic_menu_add),
            contentDescription = "Contact profile picture",
            modifier = Modifier.size(40.dp).border(5.dp, TravelBlue)
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        MessageBody(msg = msg)
    }
}

@Composable
fun MessageBody(msg: Message) {
    Column(modifier = Modifier.padding(1.dp).background(BackgroundLight)
    ) {
        Text(text = msg.author)
        Spacer(modifier = Modifier.height(1.dp))
        Text(text = msg.body)
    }
}

@Preview(name = "Bookings – Light", showBackground = true)
@Preview(
    name = "Bookings – Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TestUiContentPreview() {
    TravelMonkTheme {
        MessageCard(
            msg = Message(
                "JetBrains",
                "This is sample message"
            )
        )
    }
}
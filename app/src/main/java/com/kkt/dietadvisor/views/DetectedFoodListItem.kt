package com.kkt.dietadvisor.views

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkt.dietadvisor.R
import com.kkt.dietadvisor.models.DetectedFoodItem
import com.kkt.dietadvisor.ui.theme.DietAdvisorTheme

@Composable
fun DetectedFoodListItem(foodItem: DetectedFoodItem) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = 3.dp,
                horizontal = 3.dp
            )
    ) {

        // TODO: This is a hacky solution by ChatGPT and will probably break something later on
        // TODO: Find a better way of getting the imageHeight to be the same as the divider
        var imageHeight by remember { mutableIntStateOf(0) }
        Image(
            painter = foodItem.image,
            contentDescription = foodItem.foodName,
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    imageHeight = coordinates.size.height
                }
        )

        // Not bothering to use the spacer anymore since it looked ugly
//        Spacer(
//            modifier = Modifier
//                .width(1.dp)
//                .height(with(LocalDensity.current) { imageHeight.toDp() })
//                .background(Color.Gray)
//        )

        Text(
            text = foodItem.foodName,
            modifier = Modifier
                .padding(horizontal = 20.dp)
        )

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            onClick = {
                // TODO: Implement Dialogue with Drop Down box to select from predefined list
                // TODO: Implement options as an array resource and not as an in-memory object
            }
        ) {
            Text(text = "Edit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetectedFoodListItemPreview() {
    DietAdvisorTheme {
        val item = DetectedFoodItem(
            foodName = "Apple",
            image = painterResource(id = R.drawable.ic_launcher_foreground)
        )
        DetectedFoodListItem(foodItem = item)
    }
}
package com.gymfuel.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.gymfuel.app.R

@Composable
internal fun FoodArtwork(
    name: String,
    imageReference: String?,
    modifier: Modifier = Modifier,
) {
    val localImage = when {
        imageReference?.endsWith("food_chicken_breast") == true -> R.drawable.food_chicken_breast
        imageReference?.endsWith("food_white_rice") == true -> R.drawable.food_white_rice
        imageReference?.endsWith("food_rolled_oats") == true -> R.drawable.food_rolled_oats
        else -> null
    }
    when {
        localImage != null -> Image(
            painter = painterResource(localImage),
            contentDescription = "$name image",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
        imageReference != null -> AsyncImage(
            model = imageReference,
            contentDescription = "$name image",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
        else -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

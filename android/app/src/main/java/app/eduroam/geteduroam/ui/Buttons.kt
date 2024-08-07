package app.eduroam.geteduroam.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrimaryButton(
    text: String, onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    buttonTextColor: Color = Color.White,
    buttonBorderColor: Color = Color.Transparent,
) = Button(
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(CornerSize(16.dp)),
    border = BorderStroke(1.dp, buttonBorderColor),
    colors = ButtonDefaults.buttonColors(containerColor = buttonBackgroundColor),
    modifier = modifier.defaultMinSize(minWidth = 180.dp, minHeight = 52.dp)
) {
    Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = buttonTextColor, fontWeight = FontWeight.SemiBold, fontSize = 20.sp
        ),
    )
}

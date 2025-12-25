package com.japygo.modakmodak.ui.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White

@Composable
fun QuantitySelectionDialog(
    itemName: String,
    pricePerUnit: Int, // 0 for usage (no price)
    maxQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var quantity by remember { mutableStateOf(1) }
    val totalPrice = pricePerUnit * quantity

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = itemName,
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .background(SurfaceHighlight, CircleShape)
                            .size(40.dp),
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = White)
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    Text(
                        text = "$quantity",
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.width(24.dp))

                    IconButton(
                        onClick = { if (quantity < maxQuantity) quantity++ },
                        enabled = quantity < maxQuantity,
                        modifier = Modifier
                            .background(
                                if (quantity < maxQuantity) FireOrange else SurfaceHighlight,
                                CircleShape,
                            )
                            .size(40.dp),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = if (quantity < maxQuantity) White else TextSecondary,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (pricePerUnit > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.shop_total_price) + ": ",
                            color = FireOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Coins",
                            tint = FireOrange,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalPrice",
                            color = FireOrange,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.common_cancel),
                            color = TextSecondary,
                        )
                    }

                    Button(
                        onClick = { onConfirm(quantity) },
                        colors = ButtonDefaults.buttonColors(containerColor = FireOrange),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            androidx.compose.ui.res.stringResource(com.japygo.modakmodak.R.string.common_confirm),
                            color = White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

package com.japygo.modakmodak.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.japygo.modakmodak.R
import com.japygo.modakmodak.data.entity.Inventory
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.components.ModakCoinBadge
import com.japygo.modakmodak.ui.components.ModakTopBar
import com.japygo.modakmodak.ui.components.ModakSnackbarHost
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White

@Composable
fun BagScreen(
    navController: NavController,
    viewModel: ShopViewModel,
) {
    val user by viewModel.user.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel.shopEvent) {
        viewModel.shopEvent.collect { event ->
            val message = when (event) {
                is ShopViewModel.ShopEvent.Used -> context.getString(R.string.shop_used_item)
                is ShopViewModel.ShopEvent.UseFailed -> context.getString(R.string.shop_use_failed)
                else -> null
            }
            if (message != null) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    var showUseDialog by remember { mutableStateOf(false) }
    var selectedInventoryItem by remember { mutableStateOf<Inventory?>(null) }

    if (showUseDialog && selectedInventoryItem != null) {
        val item = selectedInventoryItem!!
        val nameResId = when (item.itemId) {
            "wood_twig" -> R.string.item_wood_twig_name
            "wood_log" -> R.string.item_wood_log_name
            "wood_big" -> R.string.item_wood_big_name
            "magic_blue" -> R.string.item_magic_blue_name
            else -> null
        }
        val localizedName = nameResId?.let { stringResource(it) } ?: item.itemId

        QuantitySelectionDialog(
            itemName = localizedName,
            pricePerUnit = 0, // Usage doesn't cost coins
            maxQuantity = item.quantity,
            confirmButtonText = stringResource(R.string.common_use),
            onDismiss = { showUseDialog = false },
            onConfirm = { quantity ->
                viewModel.useItem(item, quantity)
                showUseDialog = false
            },
        )
    }

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = BackgroundDark,
        topBar = {
            ModakTopBar(
                coins = user?.currentCoin ?: 0,
                level = user?.fireLevel ?: 1,
                exp = user?.fireExp ?: 0,
                fireColorHex = user?.fireColor ?: "#FFFF9500",
                showLevel = false
            )
        },
        bottomBar = { ModakBottomBar(navController, "bag") },
        snackbarHost = { ModakSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            InventoryGrid(
                inventory = inventory,
                shopItems = shopItems,
                onUseClick = {
                    selectedInventoryItem = it
                    showUseDialog = true
                },
            )
        }
    }
}


@Composable
fun InventoryGrid(
    inventory: List<Inventory>,
    shopItems: List<ShopItem>,
    onUseClick: (Inventory) -> Unit,
) {
    if (inventory.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                stringResource(R.string.shop_inventory_empty),
                color = TextSecondary,
                fontSize = 16.sp,
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            items(inventory) { invItem ->
                val shopItem = shopItems.find { it.id == invItem.itemId }
                if (shopItem != null) {
                    InventoryItemCard(invItem, shopItem, onUseClick)
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    inventory: Inventory,
    shopItem: ShopItem,
    onUseClick: (Inventory) -> Unit,
) {

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(
                1.dp,
                FireOrange.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp),
            )
            .clickable { onUseClick(inventory) }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A160D)),
            contentAlignment = Alignment.Center,
        ) {
            val resId = when (shopItem.imageUrl) {
                "shop_item_wood_twig" -> R.drawable.shop_item_wood_twig
                "shop_item_wood_log" -> R.drawable.shop_item_wood_log
                "shop_item_wood_big" -> R.drawable.shop_item_wood_big
                "shop_item_magic_powder" -> R.drawable.shop_item_magic_powder
                else -> null
            }

            if (resId != null) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = shopItem.name,
                    modifier = Modifier.fillMaxSize(0.8f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text("📦", fontSize = 40.sp)
            }

            // Quantity badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(FireOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (inventory.quantity > 99) "99+" else "x${inventory.quantity}",
                    color = BackgroundDark,
                    fontSize = when {
                        inventory.quantity > 99 -> 8.sp
                        inventory.quantity < 10 -> 12.sp
                        else -> 9.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val nameResId = when (shopItem.id) {
            "wood_twig" -> R.string.item_wood_twig_name
            "wood_log" -> R.string.item_wood_log_name
            "wood_big" -> R.string.item_wood_big_name
            "magic_blue" -> R.string.item_magic_blue_name
            else -> null
        }
        val descResId = when (shopItem.id) {
            "wood_twig" -> R.string.item_wood_twig_desc
            "wood_log" -> R.string.item_wood_log_desc
            "wood_big" -> R.string.item_wood_big_desc
            "magic_blue" -> R.string.item_magic_blue_desc
            else -> null
        }

        Text(
            nameResId?.let { stringResource(it) } ?: shopItem.name,
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        Text(
            descResId?.let { stringResource(it) } ?: shopItem.description,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.padding(top = 2.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceHighlight.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.common_use),
                    color = FireOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

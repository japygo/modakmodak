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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayCircle
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
import com.japygo.modakmodak.data.entity.ShopItem
import com.japygo.modakmodak.ui.components.ModakBottomBar
import com.japygo.modakmodak.ui.components.ModakSnackbarHost
import com.japygo.modakmodak.ui.components.ModakTopBar
import com.japygo.modakmodak.ui.theme.BackgroundDark
import com.japygo.modakmodak.ui.theme.FireOrange
import com.japygo.modakmodak.ui.theme.SurfaceDark
import com.japygo.modakmodak.ui.theme.SurfaceHighlight
import com.japygo.modakmodak.ui.theme.TextSecondary
import com.japygo.modakmodak.ui.theme.White
import com.japygo.modakmodak.utils.findActivity
import kotlinx.coroutines.delay

@Composable
fun ShopScreen(
    navController: NavController,
    viewModel: ShopViewModel,
) {
    val user by viewModel.user.collectAsState()
    val shopItems by viewModel.shopItems.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    var isAdProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.shopEvent) {
        viewModel.shopEvent.collect { event ->
            if (event is ShopViewModel.ShopEvent.AdRewardEarned || 
                event is ShopViewModel.ShopEvent.AdLoadFailed || 
                event is ShopViewModel.ShopEvent.AdDismissed) {
                isAdProcessing = false
            }
            if (event is ShopViewModel.ShopEvent.AdDismissed) return@collect

            val message = when (event) {
                is ShopViewModel.ShopEvent.Bought -> {
                    val nameResId = when (event.itemId) {
                        "wood_twig" -> R.string.item_wood_twig_name
                        "wood_log" -> R.string.item_wood_log_name
                        "wood_big" -> R.string.item_wood_big_name
                        "magic_blue" -> R.string.item_magic_blue_name
                        else -> null
                    }
                    val localizedName = nameResId?.let { context.getString(it) } ?: event.itemId
                    context.getString(R.string.shop_bought_item, localizedName)
                }
                is ShopViewModel.ShopEvent.BuyFailed -> {
                    val nameResId = when (event.itemId) {
                        "wood_twig" -> R.string.item_wood_twig_name
                        "wood_log" -> R.string.item_wood_log_name
                        "wood_big" -> R.string.item_wood_big_name
                        "magic_blue" -> R.string.item_magic_blue_name
                        else -> null
                    }
                    val localizedName = nameResId?.let { context.getString(it) } ?: event.itemId
                    context.getString(R.string.shop_buy_failed, localizedName)
                }
                is ShopViewModel.ShopEvent.Used -> context.getString(R.string.shop_used_item)
                is ShopViewModel.ShopEvent.UseFailed -> context.getString(R.string.shop_use_failed)
                is ShopViewModel.ShopEvent.AdRewardEarned -> context.getString(
                    R.string.ad_shop_free_coins_toast,
                    event.amount,
                )
                is ShopViewModel.ShopEvent.AdLoadFailed -> {
                    if (!event.wasAdLoaded) context.getString(R.string.ad_shop_ads_loading)
                    else context.getString(R.string.ad_load_failed)
                }
                is ShopViewModel.ShopEvent.AdDismissed -> null
            }
            if (message != null) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ShopItem?>(null) }

    if (showBuyDialog && selectedItem != null) {
        val item = selectedItem!!
        val maxAffordable = if (item.price > 0) (user?.currentCoin ?: 0) / item.price else 99
        val realMax = maxOf(1, minOf(99, maxAffordable))

        val nameResId = when (item.id) {
            "wood_twig" -> R.string.item_wood_twig_name
            "wood_log" -> R.string.item_wood_log_name
            "wood_big" -> R.string.item_wood_big_name
            "magic_blue" -> R.string.item_magic_blue_name
            else -> null
        }
        val localizedName = nameResId?.let { stringResource(it) } ?: item.name

        QuantitySelectionDialog(
            itemName = localizedName,
            pricePerUnit = item.price,
            maxQuantity = realMax,
            confirmButtonText = stringResource(R.string.common_buy),
            onDismiss = { showBuyDialog = false },
            onConfirm = { quantity ->
                viewModel.buyItem(item, quantity)
                showBuyDialog = false
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
                showLevel = false,
            )
        },
        bottomBar = { ModakBottomBar(navController, "shop") },
        snackbarHost = { ModakSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            val dailyAdCount by viewModel.dailyAdCount.collectAsState()
            val isAdLoaded by viewModel.isAdLoaded.collectAsState()
            val activity = context.findActivity()
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            FreeCoinCard(
                limit = 3,
                current = dailyAdCount,
                isAdLoaded = isAdLoaded,
                isProcessing = isAdProcessing,
                onWatchAd = {
                    if (activity != null) {
                        isAdProcessing = true
                        viewModel.watchAdForCoins(activity = activity)
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShopGrid(
                items = shopItems,
                onBuyClick = {
                    selectedItem = it
                    showBuyDialog = true
                },
            )
        }
    }
}

@Composable
fun FreeCoinCard(
    limit: Int,
    current: Int,
    isAdLoaded: Boolean,
    isProcessing: Boolean,
    onWatchAd: () -> Unit,
) {
    val isLimitReached = current >= limit
    val isEnabled =
        !isLimitReached // Button always clickable (if limit not reached) to handle clicks/feedback
    val isReady = isAdLoaded // Visual state tracking

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(
                1.dp,
                if (isReady) FireOrange.copy(alpha = 0.5f) else White.copy(alpha = 0.1f),
                RoundedCornerShape(24.dp),
            )
            .clickable(enabled = isEnabled && !isProcessing) {
                onWatchAd()
            }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                stringResource(R.string.ad_shop_free_coins_btn),
                color = if (isReady) White else TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (isLimitReached) stringResource(
                    R.string.ad_shop_free_coins_limit,
                    current,
                    limit,
                )
                else if (!isReady) stringResource(R.string.ad_shop_ads_loading)
                else stringResource(R.string.ad_shop_free_coins_limit, current, limit),
                color = if (isReady) FireOrange else TextSecondary,
                fontSize = 14.sp,
            )
        }

        // Icon/Button visual
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (isReady) FireOrange else SurfaceHighlight.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            if (isProcessing) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = White,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    androidx.compose.material.icons.Icons.Rounded.PlayCircle,
                    contentDescription = null,
                    tint = if (isReady) White else White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}


@Composable
fun ShopGrid(
    items: List<ShopItem>,
    onBuyClick: (ShopItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        items(items) { item ->
            ShopItemCard(item = item, onBuyClick = { onBuyClick(item) })
        }
    }
}

@Composable
fun ShopItemCard(item: ShopItem, onBuyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(1.dp, White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .clickable(onClick = onBuyClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Item Image with Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1A160D)),
            contentAlignment = Alignment.Center,
        ) {
            val resId = when (item.imageUrl) {
                "shop_item_wood_twig" -> R.drawable.shop_item_wood_twig
                "shop_item_wood_log" -> R.drawable.shop_item_wood_log
                "shop_item_wood_big" -> R.drawable.shop_item_wood_big
                "shop_item_magic_powder" -> R.drawable.shop_item_magic_powder
                else -> null
            }

            if (resId != null) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize(0.8f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text("📦", fontSize = 40.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val nameResId = when (item.id) {
            "wood_twig" -> R.string.item_wood_twig_name
            "wood_log" -> R.string.item_wood_log_name
            "wood_big" -> R.string.item_wood_big_name
            "magic_blue" -> R.string.item_magic_blue_name
            else -> null
        }
        val descResId = when (item.id) {
            "wood_twig" -> R.string.item_wood_twig_desc
            "wood_log" -> R.string.item_wood_log_desc
            "wood_big" -> R.string.item_wood_big_desc
            "magic_blue" -> R.string.item_magic_blue_desc
            else -> null
        }

        Text(
            nameResId?.let { stringResource(it) } ?: item.name,
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )

        Text(
            descResId?.let { stringResource(it) } ?: item.description,
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
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = FireOrange,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${item.price}",
                    color = FireOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}





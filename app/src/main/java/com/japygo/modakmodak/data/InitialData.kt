package com.japygo.modakmodak.data

import com.japygo.modakmodak.data.entity.ShopItem

val InitialShopItems = listOf(
    ShopItem(
        id = "wood_twig",
        name = "잔가지 묶음",
        price = 50,
        type = "EXP",
        value = 20,
        description = "작지만 소중한 불씨",
        imageUrl = "shop_item_wood_twig"
    ),
    ShopItem(
        id = "wood_log",
        name = "마른 장작",
        price = 200,
        type = "EXP",
        value = 100,
        description = "소박하지만 든든한 온기",
        imageUrl = "shop_item_wood_log"
    ),
    ShopItem(
        id = "wood_big",
        name = "우량 통나무",
        price = 500,
        type = "EXP",
        value = 300,
        description = "묵직하게 머무는 열기",
        imageUrl = "shop_item_wood_big"
    ),
    ShopItem(
        id = "magic_blue",
        name = "오로라 가루",
        price = 1000,
        type = "COLOR",
        value = 0,
        description = "신비로운 운명의 불꽃",
        imageUrl = "shop_item_magic_powder"
    )
)

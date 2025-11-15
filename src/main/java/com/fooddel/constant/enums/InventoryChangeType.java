package com.fooddel.constant.enums;

/**
 * 在庫変更タイプを定義する列挙型
 */
public enum InventoryChangeType {
    ADD,      // 追加
    SUBTRACT, // 減算
    RESERVE,  // 予約
    RELEASE   // 予約解除
}

package com.fooddel.constant.enums;

/**
 * 注文ステータスを定義する列挙型
 */
public enum OrderStatus {
    PENDING,    // 保留中
    CONFIRMED,  // 確認済み
    PREPARING,  // 準備中
    DELIVERY,   // 配送中
    COMPLETED,  // 完了
    CANCELLED   // キャンセル済み
}

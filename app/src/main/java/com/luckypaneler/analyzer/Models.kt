package com.luckypaneler.analyzer

/**
 * カード情報
 */
data class CardInfo(
    val index: Int,
    val row: Int,
    val col: Int,
    var cardType: CardType = CardType.NORMAL,
    var pairId: Int? = null
)

/**
 * カードタイプ
 */
enum class CardType {
    NORMAL,  // 通常カード
    STAR,    // 星（+1ターン）
    SKULL    // ドクロ（避けるべき）
}

/**
 * ペア情報
 */
data class PairInfo(
    val pairIndex: Int,
    val card1Index: Int,
    val card2Index: Int
)

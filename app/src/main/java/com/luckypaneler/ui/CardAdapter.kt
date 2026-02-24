package com.luckypaneler.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luckypaneler.analyzer.CardInfo
import com.luckypaneler.analyzer.CardType
import com.luckypaneler.databinding.ItemCardBinding

/**
 * カードグリッド表示用アダプター
 */
class CardAdapter(
    private var cards: MutableList<CardInfo>,
    private val onCardSwapped: (Int, Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    companion object {
        // ペアカラー（視認性の高い配色）
        private val PAIR_COLORS = listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
            "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
            "#F8B500", "#00CED1", "#FF69B4", "#32CD32"
        )
        
        // 選択時のハイライト色
        private const val SELECTED_BACKGROUND = "#FFFFFF"
        private const val SELECTED_OVERLAY = "#60FFFFFF"
    }

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var onCardClickListener: ((Int) -> Unit)? = null

    fun setOnCardClickListener(listener: (Int) -> Unit) {
        onCardClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], position == selectedPosition)
        holder.itemView.setOnClickListener {
            onCardClickListener?.invoke(holder.adapterPosition)
        }
    }

    override fun getItemCount() = cards.size

    fun updateCards(newCards: List<CardInfo>) {
        cards.clear()
        cards.addAll(newCards)
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position
        if (oldPosition != RecyclerView.NO_POSITION) notifyItemChanged(oldPosition)
        if (selectedPosition != RecyclerView.NO_POSITION) notifyItemChanged(selectedPosition)
    }

    fun swapItems(fromPosition: Int, toPosition: Int) {
        val fromCard = cards[fromPosition]
        val toCard = cards[toPosition]
        
        cards[fromPosition] = toCard.copy(index = fromPosition)
        cards[toPosition] = fromCard.copy(index = toPosition)
        
        selectedPosition = RecyclerView.NO_POSITION
        notifyItemChanged(fromPosition)
        notifyItemChanged(toPosition)
        
        onCardSwapped(fromPosition, toPosition)
    }

    class CardViewHolder(private val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(card: CardInfo, isSelected: Boolean) {
            binding.cardThumbnail.visibility = View.GONE
            binding.overlay.visibility = View.VISIBLE
            binding.cardLabel.visibility = View.VISIBLE
            
            val (strokeColor, strokeWidth, backgroundColor, labelText, labelSize) = getCardStyle(card)
            
            binding.cardLabel.text = labelText
            binding.cardLabel.textSize = labelSize
            
            if (isSelected) {
                // 選択時: 明るい背景とパルス効果
                binding.cardRoot.setStrokeColor(Color.WHITE)
                binding.cardRoot.setStrokeWidth(14)
                binding.cardRoot.cardElevation = 16f
                binding.overlay.setBackgroundColor(Color.parseColor(SELECTED_OVERLAY))
                binding.root.scaleX = 1.05f
                binding.root.scaleY = 1.05f
            } else {
                binding.cardRoot.setStrokeColor(strokeColor)
                binding.cardRoot.setStrokeWidth(strokeWidth)
                binding.cardRoot.cardElevation = 4f
                binding.overlay.setBackgroundColor(backgroundColor)
                binding.root.scaleX = 1.0f
                binding.root.scaleY = 1.0f
            }
        }
        
        /**
         * カードタイプに応じたスタイルを取得
         */
        private fun getCardStyle(card: CardInfo): CardStyle {
            return when (card.cardType) {
                CardType.STAR -> CardStyle(
                    strokeColor = Color.parseColor("#FFD700"),
                    strokeWidth = 8,
                    backgroundColor = Color.parseColor("#3D3D00"),
                    labelText = "⭐",
                    labelSize = 36f
                )
                CardType.SKULL -> CardStyle(
                    strokeColor = Color.parseColor("#FF4444"),
                    strokeWidth = 8,
                    backgroundColor = Color.parseColor("#3D0000"),
                    labelText = "💀",
                    labelSize = 36f
                )
                CardType.NORMAL -> {
                    if (card.pairId != null) {
                        val colorIdx = (card.pairId!! - 1) % PAIR_COLORS.size
                        val pairColor = Color.parseColor(PAIR_COLORS[colorIdx])
                        CardStyle(
                            strokeColor = pairColor,
                            strokeWidth = 10,
                            backgroundColor = adjustBrightness(pairColor, 0.3f),
                            labelText = card.pairId!!.toString(),
                            labelSize = 40f
                        )
                    } else {
                        // ブランクカード
                        CardStyle(
                            strokeColor = Color.GRAY,
                            strokeWidth = 3,
                            backgroundColor = Color.parseColor("#2C2C2E"),
                            labelText = "?",
                            labelSize = 32f
                        )
                    }
                }
            }
        }
        
        private fun adjustBrightness(color: Int, factor: Float): Int {
            val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
            val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
            val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
            return Color.rgb(r, g, b)
        }
        
        /**
         * カードスタイルのデータクラス
         */
        private data class CardStyle(
            val strokeColor: Int,
            val strokeWidth: Int,
            val backgroundColor: Int,
            val labelText: String,
            val labelSize: Float
        )
    }
}

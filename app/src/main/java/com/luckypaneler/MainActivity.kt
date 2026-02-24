package com.luckypaneler

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.luckypaneler.analyzer.CardInfo
import com.luckypaneler.analyzer.CardType
import com.luckypaneler.analyzer.PairInfo
import com.luckypaneler.databinding.ActivityMainBinding
import com.luckypaneler.ui.CardAdapter

/**
 * カードシャッフル追跡アプリのメインアクティビティ
 * 
 * フロー:
 * 1. グリッドサイズを選択（3x4, 4x4, 4x5, 4x6）
 * 2. カードペアを設定（2枚ずつタップ）
 * 3. 残り2枚から星をタップ（もう1枚は自動でドクロ）
 * 4. シャッフルモード（入れ替わったカードを追跡）
 */
class MainActivity : AppCompatActivity() {

    /** アプリのフェーズ */
    private enum class Phase {
        GRID_SELECT,   // グリッドサイズ選択
        PAIR_INPUT,    // ペア入力
        STAR_SELECT,   // 星を選択（残り2枚から）
        SHUFFLE        // シャッフル追跡
    }

    private lateinit var binding: ActivityMainBinding
    
    private var phase = Phase.GRID_SELECT
    private var gridCols = 0
    private var currentPairId = 1
    private var firstSelected = -1
    
    private val cards = mutableListOf<CardInfo>()
    private val pairs = mutableListOf<PairInfo>()
    private var adapter: CardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        // グリッドサイズ選択
        binding.btn3x4.setOnClickListener { initGrid(3, 4) }
        binding.btn4x4.setOnClickListener { initGrid(4, 4) }
        binding.btn4x5.setOnClickListener { initGrid(4, 5) }
        binding.btn4x6.setOnClickListener { initGrid(4, 6) }
        
        // リセット
        binding.resetButton.setOnClickListener { reset() }
        binding.completeButton.setOnClickListener { reset() }
    }

    /** グリッドを初期化 */
    private fun initGrid(rows: Int, cols: Int) {
        gridCols = cols
        phase = Phase.PAIR_INPUT
        currentPairId = 1
        firstSelected = -1
        
        cards.clear()
        pairs.clear()
        repeat(rows * cols) { i ->
            cards.add(CardInfo(index = i, row = i / cols, col = i % cols))
        }
        
        showGrid()
        updateStatus("ペア${currentPairId}: 2枚をタップ")
        toast("${rows}×${cols}グリッド")
    }

    /** グリッドを表示 */
    private fun showGrid() {
        binding.gridSizeSelector.visibility = View.GONE
        binding.controlArea.visibility = View.VISIBLE
        binding.bottomButtonContainer.visibility = View.VISIBLE
        binding.emptyStateText.visibility = View.GONE
        binding.cardRecyclerView.visibility = View.VISIBLE
        binding.completeButton.visibility = View.GONE
        
        binding.cardRecyclerView.layoutManager = GridLayoutManager(this, gridCols)
        adapter = CardAdapter(cards.toMutableList()) { _, _ -> }
        adapter?.setOnCardClickListener { onCardClick(it) }
        binding.cardRecyclerView.adapter = adapter
    }

    /** カードタップ処理 */
    private fun onCardClick(position: Int) {
        when (phase) {
            Phase.PAIR_INPUT -> handlePairInput(position)
            Phase.STAR_SELECT -> handleStarSelect(position)
            Phase.SHUFFLE -> handleShuffle(position)
            else -> {}
        }
    }

    /** ペア入力処理 */
    private fun handlePairInput(pos: Int) {
        val card = cards[pos]
        
        if (card.pairId != null) {
            toast("ペア${card.pairId}です")
            return
        }
        
        if (firstSelected == -1) {
            firstSelected = pos
            adapter?.setSelectedPosition(pos)
            updateStatus("ペア${currentPairId}: 2枚目をタップ")
        } else if (firstSelected == pos) {
            firstSelected = -1
            adapter?.setSelectedPosition(-1)
            updateStatus("ペア${currentPairId}: 2枚をタップ")
        } else {
            // ペア確定
            cards[firstSelected].pairId = currentPairId
            cards[pos].pairId = currentPairId
            pairs.add(PairInfo(currentPairId, firstSelected, pos))
            
            adapter?.updateCards(cards)
            adapter?.setSelectedPosition(-1)
            firstSelected = -1
            currentPairId++
            
            checkRemainingCards()
        }
    }

    /** 残りカードをチェック */
    private fun checkRemainingCards() {
        val remaining = cards.count { it.pairId == null }
        when {
            remaining > 2 -> updateStatus("ペア${currentPairId}: 2枚をタップ")
            remaining == 2 -> {
                phase = Phase.STAR_SELECT
                updateStatus("⭐星のカードをタップ")
                toast("星を選んでください")
            }
            else -> startShuffle()
        }
    }

    /** 星選択処理 */
    private fun handleStarSelect(pos: Int) {
        if (cards[pos].pairId != null) {
            toast("未設定のカードを選んでください")
            return
        }
        
        cards[pos].cardType = CardType.STAR
        cards.find { it.pairId == null && it.index != pos }?.cardType = CardType.SKULL
        
        adapter?.updateCards(cards)
        startShuffle()
    }

    /** シャッフルモード開始 */
    private fun startShuffle() {
        phase = Phase.SHUFFLE
        firstSelected = -1
        binding.completeButton.visibility = View.VISIBLE
        updateStatus("シャッフル後: 入れ替わった2枚をタップ")
        toast("入れ替わったカードをタップ")
    }

    /** シャッフル時のスワップ処理 */
    private fun handleShuffle(pos: Int) {
        if (firstSelected == -1) {
            firstSelected = pos
            adapter?.setSelectedPosition(pos)
            updateStatus("2枚目をタップ")
        } else if (firstSelected == pos) {
            firstSelected = -1
            adapter?.setSelectedPosition(-1)
            updateStatus("シャッフル後: 入れ替わった2枚をタップ")
        } else {
            // スワップ実行
            adapter?.swapItems(firstSelected, pos)
            val temp = cards[firstSelected]
            cards[firstSelected] = cards[pos]
            cards[pos] = temp
            
            firstSelected = -1
            adapter?.setSelectedPosition(-1)
            updateStatus("シャッフル後: 入れ替わった2枚をタップ")
        }
    }

    /** リセット */
    private fun reset() {
        phase = Phase.GRID_SELECT
        gridCols = 0
        currentPairId = 1
        firstSelected = -1
        cards.clear()
        pairs.clear()
        adapter = null
        
        binding.gridSizeSelector.visibility = View.VISIBLE
        binding.controlArea.visibility = View.GONE
        binding.bottomButtonContainer.visibility = View.GONE
        binding.emptyStateText.visibility = View.VISIBLE
        binding.cardRecyclerView.visibility = View.GONE
    }

    private fun updateStatus(text: String) {
        binding.statusText.text = text
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

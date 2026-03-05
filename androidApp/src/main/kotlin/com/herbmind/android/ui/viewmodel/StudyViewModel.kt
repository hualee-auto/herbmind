package com.herbmind.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.data.model.Herb
import com.herbmind.data.repository.HerbRepository
import com.herbmind.domain.study.SM2Algorithm
import com.herbmind.domain.study.StudyProgress
import com.herbmind.domain.study.StudyStatistics
import com.herbmind.domain.study.StudyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 复习卡片数据 - 包含学习进度和完整药材信息
 */
data class ReviewCardData(
    val progress: StudyProgress,
    val herb: Herb? = null
)

/**
 * 学习界面 ViewModel
 */
class StudyViewModel(
    private val studyUseCase: StudyUseCase,
    private val herbRepository: HerbRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
        loadTodayReviews()
        loadNewHerbs()
        loadStudyRecords()
    }

    /**
     * 加载学习统计
     */
    fun loadStatistics() {
        viewModelScope.launch {
            studyUseCase.getStudyStatistics().collect { stats ->
                _uiState.update { it.copy(statistics = stats) }
            }
        }
    }

    /**
     * 加载今日待复习列表
     */
    fun loadTodayReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReviews = true) }
            studyUseCase.getTodayReviewList().collect { list ->
                // 同时加载完整药材信息
                val cards = list.map { progress ->
                    val herb = herbRepository.getHerbById(progress.herbId).first()
                    ReviewCardData(progress, herb)
                }
                _uiState.update {
                    it.copy(
                        todayReviews = list,
                        reviewCards = cards,
                        isLoadingReviews = false
                    )
                }
            }
        }
    }

    /**
     * 加载新学习推荐
     */
    fun loadNewHerbs() {
        viewModelScope.launch {
            studyUseCase.getNewHerbs(limit = 5).collect { list ->
                _uiState.update { it.copy(newHerbs = list) }
            }
        }
    }

    /**
     * 加载学习记录（用于热力图）
     */
    fun loadStudyRecords() {
        viewModelScope.launch {
            // 模拟学习记录数据，实际应从数据库加载
            val records = generateMockStudyRecords()
            _uiState.update { it.copy(studyRecords = records) }
        }
    }

    /**
     * 生成模拟学习记录（用于演示）
     */
    private fun generateMockStudyRecords(): List<String> {
        val records = mutableListOf<String>()
        val today = java.time.LocalDate.now()

        // 生成最近6个月的随机学习记录
        for (i in 0..180) {
            val date = today.minusDays(i.toLong())
            // 随机生成学习记录，概率约60%
            if (kotlin.random.Random.nextFloat() < 0.6f) {
                records.add(date.toString())
            }
        }

        return records
    }

    /**
     * 开始学习新药
     */
    fun startStudying(herbId: String) {
        viewModelScope.launch {
            studyUseCase.startStudying(herbId).collect { result ->
                result.onSuccess {
                    // 开始学习成功，刷新列表
                    loadStatistics()
                    loadNewHerbs()
                    loadTodayReviews()
                }
            }
        }
    }

    /**
     * 进入复习模式
     */
    fun startReviewMode() {
        val reviews = _uiState.value.todayReviews
        if (reviews.isNotEmpty()) {
            _uiState.update { 
                it.copy(
                    isReviewMode = true,
                    currentReviewIndex = 0,
                    showAnswer = false
                )
            }
        }
    }

    /**
     * 显示答案
     */
    fun showAnswer() {
        _uiState.update { it.copy(showAnswer = true) }
    }

    /**
     * 提交复习评分
     */
    fun submitReview(rating: ReviewRating) {
        val currentHerb = _uiState.value.currentReviewHerb ?: return
        
        viewModelScope.launch {
            studyUseCase.submitReview(
                herbId = currentHerb.herbId,
                rating = rating.toDomainRating()
            ).collect { result ->
                result.onSuccess { progress ->
                    // 记录本次复习结果
                    _uiState.update { state ->
                        state.copy(
                            lastReviewResult = ReviewResult(
                                herbId = currentHerb.herbId,
                                herbName = currentHerb.herbName,
                                rating = rating.toDomainRating(),
                                newInterval = progress.interval,
                                nextReview = progress.nextReviewAt
                            )
                        )
                    }
                    
                    // 进入下一个
                    moveToNextReview()
                }
            }
        }
    }

    /**
     * 跳过当前（稍后再复习）
     */
    fun skipCurrent() {
        moveToNextReview()
    }

    /**
     * 退出复习模式
     */
    fun exitReviewMode() {
        _uiState.update { 
            it.copy(
                isReviewMode = false,
                currentReviewIndex = 0,
                showAnswer = false,
                lastReviewResult = null
            )
        }
        // 刷新统计数据
        loadStatistics()
        loadTodayReviews()
    }

    private fun moveToNextReview() {
        val currentIndex = _uiState.value.currentReviewIndex
        val totalReviews = _uiState.value.todayReviews.size
        
        if (currentIndex < totalReviews - 1) {
            _uiState.update { 
                it.copy(
                    currentReviewIndex = currentIndex + 1,
                    showAnswer = false
                )
            }
        } else {
            // 复习完成
            _uiState.update { 
                it.copy(
                    isReviewMode = false,
                    reviewCompleted = true
                )
            }
        }
    }

    fun dismissReviewCompleted() {
        _uiState.update { it.copy(reviewCompleted = false) }
        loadStatistics()
        loadTodayReviews()
    }

    /**
     * 加载当前复习卡片的完整药材信息
     */
    private fun loadCurrentHerbDetail() {
        val current = _uiState.value.currentReviewHerb ?: return
        viewModelScope.launch {
            herbRepository.getHerbById(current.herbId).first()?.let { herb ->
                _uiState.update { state ->
                    val updatedCards = state.reviewCards.toMutableList()
                    val index = state.currentReviewIndex
                    if (index < updatedCards.size) {
                        updatedCards[index] = updatedCards[index].copy(herb = herb)
                    }
                    state.copy(reviewCards = updatedCards)
                }
            }
        }
    }
}

/**
 * UI 状态
 */
data class StudyUiState(
    val statistics: StudyStatistics = StudyStatistics(0, 0, 0, 0, 0f, 0, 0, 0),
    val todayReviews: List<StudyProgress> = emptyList(),
    val reviewCards: List<ReviewCardData> = emptyList(),  // 包含完整药材信息
    val newHerbs: List<Herb> = emptyList(),
    val isLoadingReviews: Boolean = false,
    val studyRecords: List<String> = emptyList(),  // 学习记录日期列表 (yyyy-MM-dd)

    // 复习模式状态
    val isReviewMode: Boolean = false,
    val currentReviewIndex: Int = 0,
    val showAnswer: Boolean = false,
    val reviewCompleted: Boolean = false,
    val lastReviewResult: ReviewResult? = null
) {
    val currentReviewHerb: StudyProgress?
        get() = todayReviews.getOrNull(currentReviewIndex)

    val currentReviewCard: ReviewCardData?
        get() = reviewCards.getOrNull(currentReviewIndex)

    val remainingCount: Int
        get() = todayReviews.size - currentReviewIndex
}

/**
 * 复习结果
 */
data class ReviewResult(
    val herbId: String,
    val herbName: String,
    val rating: SM2Algorithm.Rating,
    val newInterval: Int,
    val nextReview: Long?
)

/**
 * 复习评分（用于 UI 层）
 */
enum class ReviewRating(val value: Int, val label: String, val emoji: String) {
    AGAIN(1, "生疏", "😵"),
    HARD(2, "困难", "😐"),
    GOOD(3, "适中", "😊"),
    EASY(4, "简单", "🤩");

    fun toDomainRating(): SM2Algorithm.Rating {
        return SM2Algorithm.Rating.fromValue(value)
    }
}
<template>
  <div class="recommendation-visualizer glass">
    <div class="visualizer-header">
      <h3 class="visualizer-title">推荐算法可视化</h3>
      <p class="visualizer-subtitle">了解推荐系统如何为您匹配内容</p>
    </div>
    
    <div class="algorithm-flow">
      <div class="flow-step">
        <div class="step-icon">👤</div>
        <div class="step-content">
          <h4 class="step-title">用户画像分析</h4>
          <p class="step-description">分析您的兴趣、目标、年龄等特征</p>
          <div class="user-traits">
            <span v-for="trait in userTraits" :key="trait" class="trait-tag">
              {{ trait }}
            </span>
          </div>
        </div>
      </div>
      
      <div class="flow-arrow">→</div>
      
      <div class="flow-step">
        <div class="step-icon">📊</div>
        <div class="step-content">
          <h4 class="step-title">内容匹配计算</h4>
          <p class="step-description">计算分享内容与您特征的匹配度</p>
          <div class="matching-factors">
            <div class="factor-bar">
              <span class="factor-name">运动类型</span>
              <div class="factor-progress">
                <div class="progress-fill" :style="{ width: contentScores.sportType + '%' }"></div>
              </div>
              <span class="factor-score">{{ contentScores.sportType }}%</span>
            </div>
            <div class="factor-bar">
              <span class="factor-name">难度等级</span>
              <div class="factor-progress">
                <div class="progress-fill" :style="{ width: contentScores.difficulty + '%' }"></div>
              </div>
              <span class="factor-score">{{ contentScores.difficulty }}%</span>
            </div>
            <div class="factor-bar">
              <span class="factor-name">标签匹配</span>
              <div class="factor-progress">
                <div class="progress-fill" :style="{ width: contentScores.tags + '%' }"></div>
              </div>
              <span class="factor-score">{{ contentScores.tags }}%</span>
            </div>
            <div class="factor-bar">
              <span class="factor-name">目标匹配</span>
              <div class="factor-progress">
                <div class="progress-fill" :style="{ width: contentScores.target + '%' }"></div>
              </div>
              <span class="factor-score">{{ contentScores.target }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="flow-arrow">→</div>
      
      <div class="flow-step">
        <div class="step-icon">👥</div>
        <div class="step-content">
          <h4 class="step-title">协同过滤</h4>
          <p class="step-description">基于相似用户的偏好进行推荐</p>
          <div class="collaborative-info">
            <div class="similar-users">
              <span class="info-label">找到相似用户：</span>
              <span class="info-value">{{ similarUserCount }}人</span>
            </div>
            <div class="collaborative-score">
              <span class="info-label">协同过滤分数：</span>
              <span class="info-value">{{ collaborativeScore }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="flow-arrow">→</div>
      
      <div class="flow-step final-step">
        <div class="step-icon">🎯</div>
        <div class="step-content">
          <h4 class="step-title">最终推荐分数</h4>
          <p class="step-description">综合计算得出最终推荐度</p>
          <div class="final-score">
            <div class="score-circle">
              <span class="score-value">{{ finalScore }}%</span>
              <span class="score-label">推荐度</span>
            </div>
            <div class="score-formula">
              <span class="formula-text">内容匹配(70%) + 协同过滤(30%)</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="visualizer-footer">
      <p class="footer-text">
        推荐系统会持续学习您的偏好，为您提供更精准的个性化推荐
      </p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RecommendationVisualizer',
  props: {
    userTraits: {
      type: Array,
      default: () => ['跑步', '减脂', '25岁', '男性']
    },
    contentScores: {
      type: Object,
      default: () => ({
        sportType: 85,
        difficulty: 70,
        tags: 60,
        target: 90
      })
    },
    similarUserCount: {
      type: Number,
      default: 5
    },
    collaborativeScore: {
      type: Number,
      default: 75
    },
    finalScore: {
      type: Number,
      default: 82
    }
  }
}
</script>

<style scoped>
.recommendation-visualizer {
  padding: 2rem;
  border-radius: var(--radius-xl);
  margin-bottom: 2rem;
}

.visualizer-header {
  text-align: center;
  margin-bottom: 2rem;
}

.visualizer-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 0.5rem;
}

.visualizer-subtitle {
  color: var(--text-secondary);
  margin: 0;
}

.algorithm-flow {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  overflow-x: auto;
  padding: 1rem 0;
}

.flow-step {
  flex: 1;
  min-width: 200px;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  text-align: center;
  border: 1px solid var(--border-color);
}

.step-icon {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.step-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 0.5rem;
}

.step-description {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin: 0 0 1rem;
  line-height: 1.4;
}

.user-traits {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.trait-tag {
  background: var(--primary-color);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-lg);
  font-size: 0.75rem;
  font-weight: 500;
}

.matching-factors {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.factor-bar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.factor-name {
  flex: 0 0 60px;
  font-size: 0.75rem;
  color: var(--text-secondary);
  text-align: left;
}

.factor-progress {
  flex: 1;
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--primary-dark));
  border-radius: var(--radius-sm);
  transition: width 0.3s ease;
}

.factor-score {
  flex: 0 0 30px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--primary-color);
  text-align: right;
}

.collaborative-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.similar-users, .collaborative-score {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.info-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--primary-color);
}

.final-step {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
}

.final-step .step-title,
.final-step .step-description {
  color: white;
}

.final-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.score-circle {
  width: 80px;
  height: 80px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
}

.score-value {
  font-size: 1.25rem;
  font-weight: 700;
  line-height: 1;
}

.score-label {
  font-size: 0.625rem;
  opacity: 0.8;
  margin-top: 0.25rem;
}

.score-formula {
  text-align: center;
}

.formula-text {
  font-size: 0.75rem;
  opacity: 0.8;
}

.flow-arrow {
  font-size: 1.5rem;
  color: var(--primary-color);
  font-weight: bold;
}

.visualizer-footer {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.footer-text {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .algorithm-flow {
    flex-direction: column;
    gap: 1rem;
  }
  
  .flow-arrow {
    transform: rotate(90deg);
  }
  
  .flow-step {
    min-width: auto;
  }
  
  .matching-factors {
    gap: 0.5rem;
  }
  
  .factor-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .factor-name {
    flex: none;
  }
  
  .factor-progress {
    width: 100%;
  }
  
  .factor-score {
    flex: none;
    align-self: flex-end;
  }
}
</style> 
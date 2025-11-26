<template>
  <div class="convention-page">
    <div class="page-header">
      <h1>社区公约</h1>
      <p>了解社区规范，共建和谐创作环境</p>
    </div>

    <!-- 公约概览 -->
    <div class="overview-section">
      <div class="overview-cards">
        <div class="overview-card">
          <div class="card-icon">📋</div>
          <div class="card-content">
            <h3>公约条款</h3>
            <div class="card-value">{{ totalClauses }}</div>
            <div class="card-label">条规范</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">⚖️</div>
          <div class="card-content">
            <h3>违规处理</h3>
            <div class="card-value">{{ violationActions }}</div>
            <div class="card-label">种处理方式</div>
          </div>
        </div>
        <div class="overview-card">
          <div class="card-icon">🛡️</div>
          <div class="card-content">
            <h3>保护机制</h3>
            <div class="card-value">{{ protectionMeasures }}</div>
            <div class="card-label">项保护措施</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 公约内容 -->
    <div class="convention-content">
      <div class="content-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本规范" name="basic">
            <div class="clause-list">
              <div v-for="clause in basicClauses" :key="clause.id" class="clause-item">
                <div class="clause-header">
                  <h3 class="clause-title">{{ clause.title }}</h3>
                  <el-tag :type="clause.severity" size="small">{{ clause.severityText }}</el-tag>
                </div>
                <p class="clause-content">{{ clause.content }}</p>
                <div class="clause-examples">
                  <h4>示例：</h4>
                  <ul>
                    <li v-for="example in clause.examples" :key="example">{{ example }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="内容规范" name="content">
            <div class="clause-list">
              <div v-for="clause in contentClauses" :key="clause.id" class="clause-item">
                <div class="clause-header">
                  <h3 class="clause-title">{{ clause.title }}</h3>
                  <el-tag :type="clause.severity" size="small">{{ clause.severityText }}</el-tag>
                </div>
                <p class="clause-content">{{ clause.content }}</p>
                <div class="clause-examples">
                  <h4>示例：</h4>
                  <ul>
                    <li v-for="example in clause.examples" :key="example">{{ example }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="版权保护" name="copyright">
            <div class="clause-list">
              <div v-for="clause in copyrightClauses" :key="clause.id" class="clause-item">
                <div class="clause-header">
                  <h3 class="clause-title">{{ clause.title }}</h3>
                  <el-tag :type="clause.severity" size="small">{{ clause.severityText }}</el-tag>
                </div>
                <p class="clause-content">{{ clause.content }}</p>
                <div class="clause-examples">
                  <h4>示例：</h4>
                  <ul>
                    <li v-for="example in clause.examples" :key="example">{{ example }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 违规处理 -->
    <div class="violation-section">
      <h2>违规处理</h2>
      <div class="violation-grid">
        <div v-for="violation in violations" :key="violation.id" class="violation-card">
          <div class="violation-header">
            <div class="violation-icon">{{ violation.icon }}</div>
            <div class="violation-level" :class="violation.level">{{ violation.levelText }}</div>
          </div>
          <div class="violation-content">
            <h3 class="violation-title">{{ violation.title }}</h3>
            <p class="violation-desc">{{ violation.description }}</p>
            <div class="violation-actions">
              <h4>处理措施：</h4>
              <ul>
                <li v-for="action in violation.actions" :key="action">{{ action }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 申诉流程 -->
    <div class="appeal-section">
      <h2>申诉流程</h2>
      <div class="appeal-steps">
        <div v-for="(step, index) in appealSteps" :key="step.id" class="step-item">
          <div class="step-number">{{ index + 1 }}</div>
          <div class="step-content">
            <h3 class="step-title">{{ step.title }}</h3>
            <p class="step-desc">{{ step.description }}</p>
            <div class="step-details">
              <span v-for="detail in step.details" :key="detail" class="step-detail">{{ detail }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 举报指南 -->
    <div class="report-section">
      <h2>举报指南</h2>
      <div class="report-guide">
        <div class="guide-card">
          <h3>如何举报违规内容</h3>
          <div class="guide-steps">
            <div v-for="(step, index) in reportSteps" :key="step.id" class="guide-step">
              <div class="step-icon">{{ step.icon }}</div>
              <div class="step-text">
                <h4>{{ step.title }}</h4>
                <p>{{ step.description }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="guide-card">
          <h3>举报类型</h3>
          <div class="report-types">
            <div v-for="type in reportTypes" :key="type.id" class="report-type">
              <div class="type-icon">{{ type.icon }}</div>
              <div class="type-content">
                <h4>{{ type.name }}</h4>
                <p>{{ type.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 联系支持 -->
    <div class="support-section">
      <h2>联系支持</h2>
      <div class="support-cards">
        <div class="support-card">
          <div class="support-icon">📞</div>
          <div class="support-content">
            <h3>客服热线</h3>
            <p>400-123-4567</p>
            <span class="support-time">工作时间：9:00-18:00</span>
          </div>
        </div>
        <div class="support-card">
          <div class="support-icon">💬</div>
          <div class="support-content">
            <h3>在线客服</h3>
            <p>24小时在线服务</p>
            <el-button type="primary" size="small">立即咨询</el-button>
          </div>
        </div>
        <div class="support-card">
          <div class="support-icon">📧</div>
          <div class="support-content">
            <h3>邮箱支持</h3>
            <p>support@example.com</p>
            <span class="support-time">24小时内回复</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 概览数据
const totalClauses = ref(25)
const violationActions = ref(8)
const protectionMeasures = ref(12)

// 当前标签页
const activeTab = ref('basic')

// 基本规范
const basicClauses = ref([
  {
    id: 1,
    title: '禁止发布违法违规内容',
    content: '严禁发布违反国家法律法规的内容，包括但不限于政治敏感、暴力恐怖、色情低俗等。',
    severity: 'danger',
    severityText: '严重',
    examples: [
      '发布政治敏感言论',
      '传播暴力恐怖信息',
      '发布色情低俗内容'
    ]
  },
  {
    id: 2,
    title: '禁止恶意刷量行为',
    content: '严禁使用任何技术手段或人工方式恶意刷取播放量、点赞数、评论数等数据。',
    severity: 'warning',
    severityText: '警告',
    examples: [
      '使用刷量软件',
      '雇佣水军刷数据',
      '恶意互刷行为'
    ]
  }
])

// 内容规范
const contentClauses = ref([
  {
    id: 1,
    title: '禁止发布虚假信息',
    content: '严禁发布虚假、误导性的信息，包括但不限于虚假新闻、谣言、伪科学等。',
    severity: 'danger',
    severityText: '严重',
    examples: [
      '传播虚假新闻',
      '发布伪科学内容',
      '恶意误导用户'
    ]
  },
  {
    id: 2,
    title: '禁止恶意营销',
    content: '严禁进行恶意营销行为，包括但不限于垃圾广告、诱导关注、虚假宣传等。',
    severity: 'warning',
    severityText: '警告',
    examples: [
      '发布垃圾广告',
      '诱导用户关注',
      '虚假宣传产品'
    ]
  }
])

// 版权保护
const copyrightClauses = ref([
  {
    id: 1,
    title: '尊重他人版权',
    content: '严禁未经授权使用他人的作品，包括但不限于音乐、视频、图片、文字等。',
    severity: 'danger',
    severityText: '严重',
    examples: [
      '未经授权使用他人音乐',
      '盗用他人视频内容',
      '使用他人图片未授权'
    ]
  },
  {
    id: 2,
    title: '保护原创内容',
    content: '鼓励原创内容创作，严厉打击抄袭、剽窃等侵权行为。',
    severity: 'warning',
    severityText: '警告',
    examples: [
      '直接抄袭他人内容',
      '剽窃他人创意',
      '未标注引用来源'
    ]
  }
])

// 违规处理
const violations = ref([
  {
    id: 1,
    icon: '⚠️',
    title: '轻微违规',
    description: '首次违规或情节较轻的违规行为',
    level: 'light',
    levelText: '轻微',
    actions: [
      '删除违规内容',
      '发送警告通知',
      '限制部分功能 24 小时'
    ]
  },
  {
    id: 2,
    icon: '🚫',
    title: '严重违规',
    description: '多次违规或情节严重的违规行为',
    level: 'serious',
    levelText: '严重',
    actions: [
      '删除所有违规内容',
      '账号封禁 7-30 天',
      '限制发布权限'
    ]
  },
  {
    id: 3,
    icon: '❌',
    title: '永久封禁',
    description: '严重违法或多次严重违规',
    level: 'permanent',
    levelText: '永久',
    actions: [
      '永久封禁账号',
      '删除所有内容',
      '禁止重新注册'
    ]
  }
])

// 申诉流程
const appealSteps = ref([
  {
    id: 1,
    title: '提交申诉',
    description: '在收到处理通知后 7 天内提交申诉申请',
    details: ['填写申诉表单', '提供相关证据', '说明申诉理由']
  },
  {
    id: 2,
    title: '审核处理',
    description: '平台将在 3 个工作日内审核申诉内容',
    details: ['人工审核', '核实证据', '评估申诉理由']
  },
  {
    id: 3,
    title: '结果通知',
    description: '通过邮件或站内信通知申诉结果',
    details: ['申诉成功：恢复账号', '申诉失败：维持原处理', '可申请二次申诉']
  }
])

// 举报步骤
const reportSteps = ref([
  {
    id: 1,
    icon: '👆',
    title: '选择举报内容',
    description: '点击视频右下角的举报按钮'
  },
  {
    id: 2,
    icon: '📝',
    title: '选择举报类型',
    description: '根据违规情况选择相应的举报类型'
  },
  {
    id: 3,
    icon: '💬',
    title: '填写举报说明',
    description: '详细说明举报原因，提供相关证据'
  },
  {
    id: 4,
    icon: '✅',
    title: '提交举报',
    description: '确认信息无误后提交举报申请'
  }
])

// 举报类型
const reportTypes = ref([
  {
    id: 1,
    icon: '🚫',
    name: '违法违规',
    description: '发布违法或违反平台规定的内容'
  },
  {
    id: 2,
    icon: '💬',
    name: '恶意评论',
    description: '发布恶意、辱骂、骚扰性评论'
  },
  {
    id: 3,
    icon: '📝',
    name: '虚假信息',
    description: '发布虚假、误导性信息'
  },
  {
    id: 4,
    icon: '🎵',
    name: '版权侵权',
    description: '未经授权使用他人作品'
  }
])
</script>

<style lang="scss" scoped>
.convention-page {
  padding: 0;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  
  h1 {
    font-size: 28px;
    margin: 0 0 8px 0;
    color: #333;
  }
  
  p {
    font-size: 16px;
    color: #666;
    margin: 0;
  }
}

.overview-section {
  margin-bottom: 32px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.overview-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
  
  .card-icon {
    font-size: 32px;
  }
  
  .card-content {
    h3 {
      font-size: 14px;
      color: #666;
      margin: 0 0 4px 0;
    }
    
    .card-value {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 2px;
    }
    
    .card-label {
      font-size: 12px;
      color: #999;
    }
  }
}

.convention-content, .violation-section, .appeal-section, .report-section, .support-section {
  margin-bottom: 32px;
  
  h2 {
    font-size: 20px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.clause-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.clause-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  
  .clause-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .clause-title {
      font-size: 16px;
      margin: 0;
      color: #333;
    }
  }
  
  .clause-content {
    font-size: 14px;
    color: #666;
    margin: 0 0 16px 0;
    line-height: 1.6;
  }
  
  .clause-examples {
    h4 {
      font-size: 14px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        font-size: 13px;
        color: #666;
        margin-bottom: 4px;
      }
    }
  }
}

.violation-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.violation-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
  .violation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .violation-icon {
      font-size: 24px;
    }
    
    .violation-level {
      font-size: 12px;
      padding: 4px 8px;
      border-radius: 12px;
      
      &.light {
        background: #fff7e6;
        color: #fa8c16;
      }
      
      &.serious {
        background: #fff1f0;
        color: #ff4d4f;
      }
      
      &.permanent {
        background: #f5f5f5;
        color: #666;
      }
    }
  }
  
  .violation-content {
    .violation-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .violation-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
    }
    
    .violation-actions {
      h4 {
        font-size: 14px;
        margin: 0 0 8px 0;
        color: #333;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        
        li {
          font-size: 13px;
          color: #666;
          margin-bottom: 4px;
        }
      }
    }
  }
}

.appeal-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.step-item {
  display: flex;
  gap: 16px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  
  .step-number {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #409eff;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    flex-shrink: 0;
  }
  
  .step-content {
    flex: 1;
    
    .step-title {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    .step-desc {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
    }
    
    .step-details {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .step-detail {
        font-size: 12px;
        color: #409eff;
        background: #ecf5ff;
        padding: 4px 8px;
        border-radius: 12px;
      }
    }
  }
}

.report-guide {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.guide-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  
  h3 {
    font-size: 16px;
    margin: 0 0 16px 0;
    color: #333;
  }
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guide-step {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  
  .step-icon {
    font-size: 20px;
    flex-shrink: 0;
  }
  
  .step-text {
    h4 {
      font-size: 14px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 13px;
      color: #666;
      margin: 0;
    }
  }
}

.report-types {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.report-type {
  display: flex;
  gap: 8px;
  align-items: flex-start;
  
  .type-icon {
    font-size: 16px;
    flex-shrink: 0;
  }
  
  .type-content {
    h4 {
      font-size: 13px;
      margin: 0 0 4px 0;
      color: #333;
    }
    
    p {
      font-size: 12px;
      color: #666;
      margin: 0;
    }
  }
}

.support-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.support-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
  
  .support-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }
  
  .support-content {
    h3 {
      font-size: 16px;
      margin: 0 0 8px 0;
      color: #333;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0 0 8px 0;
    }
    
    .support-time {
      font-size: 12px;
      color: #999;
    }
  }
}

@media (max-width: 768px) {
  .overview-cards,
  .violation-grid,
  .support-cards {
    grid-template-columns: 1fr;
  }
  
  .report-guide {
    grid-template-columns: 1fr;
  }
  
  .report-types {
    grid-template-columns: 1fr;
  }
}
</style>

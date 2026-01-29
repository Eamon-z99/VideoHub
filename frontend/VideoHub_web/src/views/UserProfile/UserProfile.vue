<template>
  <div class="user-profile-page">
    <!-- 顶部栏：不随滚动悬浮出现 + 顶部透明 -->
    <TopHeader :fixed-on-scroll="false" :transparent-at-top="true" />

    <!-- 顶部封面 + 个人信息 -->
    <header class="profile-header">
      <div class="profile-header-inner">
        <img class="cover" :src="coverImage" alt="cover" @error="onImageError" @load="onImageLoad" />
        <div class="header-inner user-profile-container">
          <div class="user-row">
            <img class="avatar" :src="avatar" alt="avatar" />
            <div class="user-meta">
              <div class="name-row">
                <div class="name">{{ nickname }}</div>
                <span class="badge level">LV5</span>
                <span class="badge vip">大会员</span>
              </div>
              <div class="sub-row">编辑个性签名</div>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体：左侧收藏夹列表 + 右侧内容 -->
    <main class="content-wrap">
      <!-- Tab -->
      <nav class="profile-tabs user-profile-container">
        <div class="tabs-left">
          <button
            v-for="t in tabs"
            :key="t.key"
            class="tab"
            :class="{ active: t.key === activeTab }"
            @click="onTabChange(t.key)"
          >
            <span class="tab-icon" :class="t.key" />
            <span class="tab-text">{{ t.label }}</span>
            <span v-if="t.count != null" class="tab-count">{{ t.count }}</span>
          </button>
        </div>
        <div class="user-stats">
          <div class="stat">
            <div class="num">{{ stats.following }}</div>
            <div class="label">关注数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.followers }}</div>
            <div class="label">粉丝数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.likes }}</div>
            <div class="label">获赞数</div>
          </div>
          <div class="stat">
            <div class="num">{{ stats.views }}</div>
            <div class="label">播放数</div>
          </div>
        </div>
      </nav>
      <div class="content-inner user-profile-container">
        <aside class="left-panel" v-if="activeTab === 'collections'">
          <div class="panel-section">
            <div class="panel-title">我创建的收藏夹</div>
            <button class="new-folder" @click="onCreateFolder">
              <span class="plus">＋</span>
              新建收藏夹
            </button>
            <ul class="folder-list">
              <li
                v-for="f in folders"
                :key="f.id"
                class="folder"
                :class="{ active: f.id === activeFolderId }"
              >
                <div class="folder-main" @click="onFolderSelect(f)">
                  <svg class="folder-icon" width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M2 4C2 3.44772 2.44772 3 3 3H7.5L9 5H15C15.5523 5 16 5.44772 16 6V14C16 14.5523 15.5523 15 15 15H3C2.44772 15 2 14.5523 2 14V4Z" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                  <span class="folder-name">{{ f.name }}</span>
                  <span class="folder-count">{{ f.count }}</span>
                </div>
                <div
                  class="folder-more"
                  @click.stop
                >
                  <button
                    class="folder-more-btn"
                    @click.stop="toggleFolderMenu(f.id)"
                    aria-label="more"
                  >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <circle cx="8" cy="4" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="8" r="1.5" fill="currentColor"/>
                      <circle cx="8" cy="12" r="1.5" fill="currentColor"/>
                    </svg>
                  </button>
                  <div
                    v-if="folderMenuForId === f.id"
                    class="folder-more-menu"
                    @mouseleave="onFolderMenuLeave(f.id)"
                  >
                    <button class="menu-item" @click.stop="onRenameFolder(f)">编辑信息</button>
                    <button class="menu-item danger" @click.stop="onDeleteFolder(f)">删除</button>
                  </div>
                </div>
              </li>
            </ul>
          </div>
          <div class="panel-section">
            <div class="panel-title">我追的合集/收藏夹</div>
            <ul class="folder-list">
              <li
                v-for="f in followedFolders"
                :key="f.id"
                class="folder"
              >
                <span class="folder-icon" />
                <span class="folder-name">{{ f.name }}</span>
                <span class="folder-count">{{ f.count }}</span>
              </li>
            </ul>
          </div>
        </aside>

        <section class="right-panel">
          <div v-if="activeTab === 'collections'" class="fav-header">
            <div class="fav-left">
              <div class="fav-title">{{ activeFolder?.name || '默认收藏夹' }}</div>
              <div class="fav-sub">公开 · 视频数：{{ activeFolder?.count ?? 0 }}</div>
            </div>
            <button class="play-all">播放全部</button>
            <div class="fav-tools">
              <button class="tool-btn">批量操作</button>
            </div>
          </div>

          <div v-if="activeTab === 'collections'" class="toolbar">
            <div class="chips">
              <button class="chip active">最近收藏</button>
              <button class="chip">最多播放</button>
              <button class="chip">最近投稿</button>
            </div>
            <div class="searchbar">
              <input class="input" placeholder="请输入关键词" />
              <button class="search-btn" aria-label="search">🔍</button>
            </div>
          </div>

          <div v-if="activeTab === 'collections'" class="video-grid">
            <div v-if="loading && videos.length === 0" class="loading">加载中...</div>
            <div v-else-if="videos.length === 0" class="empty">暂无收藏</div>
            <article
              v-else
              v-for="v in videos"
              :key="v.id"
              class="video-card"
            >
              <div class="card-inner">
                <div class="thumb" @click="$router.push(`/video/${v.id}`)">
                  <img v-if="v.cover" :src="v.cover" alt="" @error="onImageError" />
                  <div v-else class="thumb-ph" />
                  <span class="duration">{{ v.duration }}</span>
                </div>
                <div class="v-title-row">
                  <div class="v-title" :title="v.title" @click="$router.push(`/video/${v.id}`)">{{ v.title }}</div>
                  <div
                    class="video-more"
                    @click.stop
                  >
                    <button class="more-btn" @click.stop="toggleVideoMenu(v.id)" aria-label="更多操作">
                      <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="8" cy="4" r="1.5" fill="currentColor"/>
                        <circle cx="8" cy="8" r="1.5" fill="currentColor"/>
                        <circle cx="8" cy="12" r="1.5" fill="currentColor"/>
                      </svg>
                    </button>
                    <div
                      v-if="videoMenuForId === v.id"
                      class="video-more-menu"
                      @mouseleave="onVideoMenuLeave(v.id)"
                    >
                      <button class="menu-item" @click.stop="onUnfavorite(v)">取消收藏</button>
                      <button class="menu-item" @click.stop="openMoveDialog(v)">移动至</button>
                    </div>
                  </div>
                </div>
                <div class="v-meta">
                  <span>▶ {{ v.play }}</span>
                  <span>💬 {{ v.danmaku }}</span>
                  <span class="time">{{ v.time }}</span>
                </div>
              </div>
            </article>
            <div v-if="loading && videos.length > 0" class="loading-more">加载中...</div>
          </div>

          <!-- 移动到收藏夹弹层 -->
          <div
            v-if="moveDialog.visible"
            class="move-dialog-mask"
            @click="closeMoveDialog"
          >
            <div class="move-dialog" @click.stop>
              <div class="move-dialog-title">移动到</div>
              <ul class="move-folder-list">
                <li
                  v-for="f in folders"
                  :key="f.id"
                  class="move-folder-item"
                  @click="confirmMove(f.id)"
                >
                  <span class="name">{{ f.name }}</span>
                  <span class="count">{{ f.count }}</span>
                </li>
              </ul>
            </div>
          </div>

          <div v-else-if="activeTab === 'home'" class="empty">主页内容</div>
          <div v-else-if="activeTab === 'dynamics'" class="empty">动态内容</div>
          <div v-else-if="activeTab === 'submit'" class="empty">投稿内容</div>
          <div v-else class="empty">暂无内容</div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import TopHeader from '@/components/TopHeader.vue'
import { getFavoriteListByFolder } from '@/api/favorite'
import { getFavoriteFolderList, createFavoriteFolder } from '@/api/favoriteFolder'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'UserProfile',
  components: { TopHeader },
  data () {
    return {
      coverImage: '/assets/topheader/favorite.png',
      activeTab: 'collections',
      tabs: [
        { key: 'home', label: '主页' },
        { key: 'dynamics', label: '动态' },
        { key: 'submit', label: '投稿' },
        { key: 'collections', label: '收藏', count: 0 }
      ],
      nickname: '皇升级',
      avatar: '/public/favicon.ico',
      stats: { following: 24, followers: 3, likes: 0, views: 0 },
      activeFolderId: null,
      folders: [],
      followedFolders: [],
      videos: [],
      loading: false,
      page: 1,
      pageSize: 20,
      total: 0,
      finished: false,
      // 右侧视频更多菜单
      videoMenuForId: null,
      // 左侧收藏夹更多菜单
      folderMenuForId: null,
      // 移动视频对话框
      moveDialog: {
        visible: false,
        video: null
      }
    }
  },
  computed: {
    activeFolder () {
      return this.folders.find(f => f.id === this.activeFolderId)
    },
    currentUserId () {
      // 从路由参数获取用户ID，如果没有则使用当前登录用户
      const routeUserId = this.$route?.params?.id
      if (routeUserId) {
        return routeUserId
      }
      const userStore = useUserStore()
      return userStore.user?.userId || userStore.user?.id
    }
  },
  mounted () {
    // 如果初始就是收藏tab，则加载数据
    if (this.activeTab === 'collections' && this.currentUserId) {
      this.initCollections()
    }
    
    // 监听tab切换（只在从其他tab切换到collections时加载）
    let previousTab = this.activeTab
    this.$watch('activeTab', (newTab) => {
      if (newTab === 'collections' && previousTab !== 'collections' && this.currentUserId) {
        this.initCollections()
      }
      previousTab = newTab
    })
  },
  methods: {
    onTabChange (key) {
      // 如果切换到收藏tab，重置并加载数据
      if (key === 'collections' && this.currentUserId) {
        this.activeTab = key
        this.initCollections()
      } else {
        this.activeTab = key
      }
    },
    async initCollections () {
      await this.loadFolders()
      // 默认选中第一个（通常是“默认收藏夹”）
      if (!this.activeFolderId && this.folders.length > 0) {
        this.activeFolderId = this.folders[0].id
      }
      if (this.activeFolderId) {
        await this.loadFavorites(this.currentUserId, this.activeFolderId, true)
      }
    },

    async loadFolders () {
      try {
        const { data } = await getFavoriteFolderList(this.currentUserId)
        if (data.success) {
          this.folders = (data.list || []).map(it => ({
            id: it.id,
            name: it.name,
            count: it.count ?? 0
          }))
          // 如果当前选中的收藏夹已不存在，重置
          if (this.activeFolderId && !this.folders.some(f => f.id === this.activeFolderId)) {
            this.activeFolderId = null
          }
        }
      } catch (e) {
        console.error('加载收藏夹失败:', e)
        this.folders = []
      }
    },

    onFolderSelect (folder) {
      if (!folder || !folder.id) return
      if (folder.id === this.activeFolderId) return
      this.activeFolderId = folder.id
      this.loadFavorites(this.currentUserId, folder.id, true)
    },

    async onCreateFolder () {
      if (!this.currentUserId) return
      try {
        const name = await ElMessageBox.prompt('请输入收藏夹名称', '新建收藏夹', {
          confirmButtonText: '创建',
          cancelButtonText: '取消',
          inputPlaceholder: '例如：学习/游戏/前端',
          inputValidator: (v) => {
            if (!v || !String(v).trim()) return '名称不能为空'
            if (String(v).trim().length > 30) return '名称最多30个字符'
            return true
          }
        }).then(res => res.value)

        const { data } = await createFavoriteFolder(this.currentUserId, name, true)
        if (data.success) {
          ElMessage.success('创建成功')
          await this.loadFolders()
        } else {
          ElMessage.warning(data.message || '创建失败')
        }
      } catch (e) {
        // cancel
      }
    },

    async loadFavorites (userId, folderId, reset = false) {
      if (this.loading) return
      
      // 重置时清空数据并重置分页
      if (reset) {
        this.page = 1
        this.videos = []
        this.finished = false
      }
      
      this.loading = true
      try {
        const { data } = await getFavoriteListByFolder(userId, folderId, this.page, this.pageSize)
        if (data.success) {
          const list = data.list || []
          const total = data.total || 0
          
          // 格式化视频数据，使用 Set 去重（基于 videoId）
          const existingIds = new Set(this.videos.map(v => v.id))
          const formattedVideos = list
            .map(item => ({
              id: item.videoId || item.id,
              favoriteId: item.id,
              cover: item.coverUrl || '',
              title: item.title || '未知标题',
              duration: item.duration || '00:00',
              play: '--',
              danmaku: '--',
              time: this.formatTime(item.createTime)
            }))
            .filter(item => !existingIds.has(item.id)) // 过滤掉已存在的视频
          
          // 追加新数据
          this.videos = [...this.videos, ...formattedVideos]
          
          this.total = total
          this.tabs.find(t => t.key === 'collections').count = total
          // 更新当前收藏夹数量（后端列表也有 count，但这里做一次即时刷新）
          const currentFolder = this.folders.find(f => f.id === folderId)
          if (currentFolder) currentFolder.count = total
          
          // 判断是否加载完成
          if (this.videos.length >= total || list.length < this.pageSize) {
            this.finished = true
          } else {
            this.page += 1
          }
        }
      } catch (error) {
        console.error('加载收藏列表失败:', error)
        if (reset) {
          this.videos = []
        }
      } finally {
        this.loading = false
      }
    },

    toggleVideoMenu (videoId) {
      this.videoMenuForId = this.videoMenuForId === videoId ? null : videoId
    },

    onVideoMenuLeave (videoId) {
      if (this.videoMenuForId === videoId) {
        this.videoMenuForId = null
      }
    },

    async onUnfavorite (video) {
      const userStore = useUserStore()
      const userId = userStore.user?.userId || userStore.user?.id
      if (!userId || !video.favoriteId) return
      try {
        const { deleteFavorite } = await import('@/api/favorite')
        const { data } = await deleteFavorite(video.favoriteId, userId)
        if (data.success) {
          ElMessage.success('已取消收藏')
          this.videoMenuForId = null
          await this.initCollections()
        } else {
          ElMessage.warning(data.message || '取消收藏失败')
        }
      } catch (e) {
        console.error('取消收藏失败:', e)
        ElMessage.error('取消收藏失败')
      }
    },

    openMoveDialog (video) {
      this.moveDialog.video = video
      this.moveDialog.visible = true
      this.videoMenuForId = null
    },

    closeMoveDialog () {
      this.moveDialog.visible = false
      this.moveDialog.video = null
    },

    async confirmMove (targetFolderId) {
      const video = this.moveDialog.video
      if (!video || !targetFolderId || targetFolderId === this.activeFolderId) {
        this.closeMoveDialog()
        return
      }
      const userStore = useUserStore()
      const userId = userStore.user?.userId || userStore.user?.id
      try {
        const { addFavoriteToFolder } = await import('@/api/favorite')
        const { data } = await addFavoriteToFolder(userId, video.id, targetFolderId)
        if (data.success) {
          ElMessage.success(data.message || '已移动')
          this.closeMoveDialog()
          await this.initCollections()
        } else {
          ElMessage.warning(data.message || '移动失败')
        }
      } catch (e) {
        console.error('移动收藏失败:', e)
        ElMessage.error('移动失败')
        this.closeMoveDialog()
      }
    },

    toggleFolderMenu (folderId) {
      this.folderMenuForId = this.folderMenuForId === folderId ? null : folderId
    },

    onFolderMenuLeave (folderId) {
      if (this.folderMenuForId === folderId) {
        this.folderMenuForId = null
      }
    },

    async onRenameFolder (folder) {
      if (!folder || !folder.id) return
      if (folder.name === '默认收藏夹') {
        ElMessage.warning('默认收藏夹不允许重命名')
        return
      }
      try {
        const name = await ElMessageBox.prompt('请输入新的收藏夹名称', '编辑信息', {
          confirmButtonText: '保存',
          cancelButtonText: '取消',
          inputValue: folder.name,
          inputValidator: (v) => {
            if (!v || !String(v).trim()) return '名称不能为空'
            if (String(v).trim().length > 30) return '名称最多30个字符'
            return true
          }
        }).then(res => res.value)
        const userId = this.currentUserId
        const { renameFavoriteFolder } = await import('@/api/favoriteFolder')
        const { data } = await renameFavoriteFolder(userId, folder.id, name)
        if (data.success) {
          ElMessage.success('已更新')
          this.folderMenuForId = null
          await this.loadFolders()
        } else {
          ElMessage.warning(data.message || '更新失败')
        }
      } catch (e) {
        // cancel
      }
    },

    async onDeleteFolder (folder) {
      if (!folder || !folder.id) return
      if (folder.name === '默认收藏夹') {
        ElMessage.warning('默认收藏夹不允许删除')
        return
      }
      try {
        await ElMessageBox.confirm(
          `确定要删除收藏夹「${folder.name}」吗？其中的视频将回到默认收藏夹。`,
          '删除收藏夹',
          { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
        )
        const userId = this.currentUserId
        const { deleteFavoriteFolder } = await import('@/api/favoriteFolder')
        const { data } = await deleteFavoriteFolder(userId, folder.id)
        if (data.success) {
          ElMessage.success('已删除')
          this.folderMenuForId = null
          // 如果当前选中的是被删除的收藏夹，重置
          if (this.activeFolderId === folder.id) {
            this.activeFolderId = null
          }
          await this.initCollections()
        } else {
          ElMessage.warning(data.message || '删除失败')
        }
      } catch (e) {
        // cancel
      }
    },
    formatTime (timeStr) {
      if (!timeStr) return '未知时间'
      try {
        const date = new Date(timeStr)
        const now = new Date()
        const diff = now - date
        const days = Math.floor(diff / (1000 * 60 * 60 * 24))
        
        if (days === 0) {
          return '今天'
        } else if (days === 1) {
          return '昨天'
        } else if (days < 7) {
          return `${days}天前`
        } else if (days < 30) {
          const weeks = Math.floor(days / 7)
          return `${weeks}周前`
        } else if (days < 365) {
          const months = Math.floor(days / 30)
          return `${months}个月前`
        } else {
          const years = Math.floor(days / 365)
          return `${years}年前`
        }
      } catch (e) {
        return '未知时间'
      }
    },
    onImageError (e) {
      console.error('图片加载失败:', e.target.src)
      console.error('尝试的路径:', this.coverImage)
    },
    onImageLoad () {
      console.log('图片加载成功:', this.coverImage)
    }
  }
}
</script>

<style lang="scss" scoped>
.user-profile-page {
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  margin: 0 auto;
  background: #FFFFFF;
  min-height: 100vh;
  // 本页 TopHeader 需要覆盖在封面图上，因此不预留顶部空间
  padding-top: 0;
}

// 统一内容区域宽度，避免 header / main 各自写死不同的 width 导致不一致
.user-profile-container {
  width: 100%;
  max-width: 75%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 8px;
}

.profile-header {
  position: relative;
  height: 230px;
  overflow: hidden;

  .profile-header-inner {
    position: relative;
    height: 90%;
  }

  // 顶部大图
  .cover {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    filter: brightness(0.8);
  }

  // 用户信息这一横栏（含头像）和下方 Tab，整体锚定在图片容器底部
  .header-inner {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
  }

  .user-row {
    position: relative;
    margin-top: 0;
    display: grid;
    grid-template-columns: 75px 1fr;
    gap: 16px;
    align-items: end;
    padding-bottom: 14px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    border: 3px solid #fff;
    object-fit: cover;
    background: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
  }

  .user-meta {
    padding-bottom: 6px;

    .name-row {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .name {
      font-size: 22px;
      font-weight: 700;
      color: #fff;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.35);
    }

    .sub-row {
      margin-top: 6px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.85);
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.25);
    }

    .badge {
      font-size: 12px;
      line-height: 18px;
      padding: 0 8px;
      border-radius: 10px;
      background: rgba(0, 0, 0, 0.25);
      color: #fff;
      border: 1px solid rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(2px);
    }

    .badge.vip {
      background: rgba(255, 255, 255, 0.18);
    }
  }
}

.profile-tabs {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  padding: 10px 12px;
  border-bottom: 1px solid #eee;
  background: #fff;
  border-radius: 8px 8px 0 0;

  .tabs-left {
    display: flex;
    gap: 18px;
  }

  .user-stats {
    display: grid;
    grid-auto-flow: column;
    gap: 22px;
    color: #222;
    font-size: 14px;

    .stat {
      text-align: center;
      min-width: 60px;
    }

    .num {
      font-weight: 700;
      font-size: 16px;
      color: #222;
    }

    .label {
      margin-top: 2px;
      font-size: 12px;
      color: #999;
    }
  }

  .tab {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    border: 0;
    background: transparent;
    cursor: pointer;
    color: #61666d;
    font-size: 14px;
    position: relative;

    .tab-icon {
      width: 16px;
      height: 16px;
      border-radius: 4px;
      background: #e8f7ff;
    }

    .tab-count {
      font-size: 12px;
      color: #00a1d6;
      font-weight: 600;
    }

    &.active {
      color: #00a1d6;
      font-weight: 600;
    }

    &.active::after {
      content: '';
      position: absolute;
      left: 8px;
      right: 8px;
      bottom: -10px;
      height: 2px;
      background: #00a1d6;
      border-radius: 2px;
    }
  }
}

.content-wrap {
  padding: 16px 0 36px;
}

.content-inner {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  padding: 0; // padding 由 .user-profile-container 统一控制
}

.left-panel {
  background: #fff;
  border-radius: 8px;
  padding: 14px 12px;
  height: fit-content;
}

.panel-section {
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.panel-title {
  font-size: 14px;
  color: #222;
  font-weight: 600;
  margin-bottom: 10px;
}

.new-folder {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #222;
  margin-bottom: 10px;

  .plus {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    background: #e8f7ff;
    color: #00a1d6;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
  }
}

.folder-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.folder {
  display: flex;
  align-items: center;
  justify-content: space-between;
  align-items: center;
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  color: #222;

  &:hover {
    background: #f6f7f8;
  }

  &.active {
    background: #e8f7ff;
    color: #00a1d6;
  }

  .folder-main {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    min-width: 0;
  }

  .folder-icon {
    width: 18px;
    height: 18px;
    flex-shrink: 0;
    color: #999;
    transition: color 0.2s;
  }

  &.active .folder-icon {
    color: #00a1d6;
  }

  .folder-name {
    flex: 1;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .folder-count {
    font-size: 12px;
    color: #999;
  }

  &.active .folder-count {
    color: #00a1d6;
  }

  .folder-more {
    position: relative;
    margin-left: 6px;
  }

  .folder-more-btn {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    border: none;
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: #666;
    }

    svg {
      width: 16px;
      height: 16px;
    }
  }

  .folder-more-menu {
    position: absolute;
    top: 28px;
    right: 0;
    min-width: 120px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
    padding: 6px 0;
    z-index: 20;
  }

  .folder-more-menu .menu-item {
    width: 100%;
    padding: 6px 14px;
    text-align: left;
    border: none;
    background: transparent;
    font-size: 13px;
    color: #222;
    cursor: pointer;

    &:hover {
      background: #f5f7fa;
    }

    &.danger {
      color: #e23c3c;
    }
  }
}

.right-panel {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px 18px;
  min-height: 520px;
}

.fav-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.fav-left {
  flex: 1;
}

.fav-title {
  font-size: 18px;
  font-weight: 700;
  color: #222;
}

.fav-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.play-all {
  border: 0;
  background: #00a1d6;
  color: #fff;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
}

.tool-btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 9px 12px;
  cursor: pointer;
}

.toolbar {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.chips {
  display: flex;
  gap: 10px;
}

.chip {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  color: #61666d;
  font-size: 13px;

  &.active {
    background: #00a1d6;
    border-color: #00a1d6;
    color: #fff;
  }
}

.searchbar {
  display: grid;
  grid-template-columns: 1fr 38px;
  gap: 10px;
  align-items: center;
}

.input {
  height: 34px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0 10px;
  outline: none;
  width: 100%;
}

.search-btn {
  height: 34px;
  width: 38px;
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
}

.video-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

.video-card {
  cursor: pointer;
  position: relative;

  .card-inner {
    position: relative;
  }

  .thumb {
    position: relative;
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    background: #f1f2f3;
    padding-bottom: 56%;

    img,
    .thumb-ph {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .duration {
      position: absolute;
      right: 6px;
      bottom: 6px;
      font-size: 12px;
      color: #fff;
      background: rgba(0, 0, 0, 0.55);
      padding: 2px 6px;
      border-radius: 4px;
    }
  }

  .v-title-row {
    margin-top: 8px;
    display: flex;
    align-items: flex-start;
    gap: 8px;
    min-height: 36px;
  }

  .v-title {
    flex: 1;
    font-size: 13px;
    color: #222;
    line-height: 1.4;
    height: 36px;
    overflow: hidden;
    display: -webkit-box;
    line-clamp: 2;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    cursor: pointer;
    transition: color 0.2s;

    &:hover {
      color: #00a1d6;
    }
  }

  .v-meta {
    margin-top: 6px;
    font-size: 12px;
    color: #999;
    display: flex;
    gap: 8px;

    .time {
      margin-left: auto;
    }
  }

  .video-more {
    position: relative;
    flex-shrink: 0;
    margin-top: 2px;
  }

  .more-btn {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    border: none;
    background: transparent;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: rgba(0, 0, 0, 0.06);
      color: #666;
    }

    svg {
      width: 16px;
      height: 16px;
    }
  }

  .video-more-menu {
    position: absolute;
    top: 28px;
    right: 0;
    min-width: 140px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
    padding: 6px 0;
    z-index: 20;
  }

  .video-more-menu .menu-item {
    width: 100%;
    padding: 6px 14px;
    text-align: left;
    border: none;
    background: transparent;
    font-size: 13px;
    color: #222;
    cursor: pointer;

    &:hover {
      background: #f5f7fa;
    }
  }
}

.move-dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 30;
}

.move-dialog {
  width: 320px;
  max-height: 420px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
  padding: 14px 0 10px;
  display: flex;
  flex-direction: column;
}

.move-dialog-title {
  padding: 0 16px 10px;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #f1f2f3;
}

.move-folder-list {
  list-style: none;
  padding: 6px 0 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}

.move-folder-item {
  padding: 8px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  cursor: pointer;

  &:hover {
    background: #f5f7fa;
  }

  .name {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .count {
    margin-left: 8px;
    font-size: 12px;
    color: #999;
  }
}

.empty {
  text-align: center;
  color: #999;
  padding: 60px 0;
}

.loading,
.loading-more {
  text-align: center;
  color: #999;
  padding: 20px 0;
  grid-column: 1 / -1;
}

@media (max-width: 960px) {
  .profile-header .header-inner,
  .content-inner {
    width: auto;
    padding: 0 12px;
  }

  .content-inner {
    grid-template-columns: 1fr;
  }

  .video-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

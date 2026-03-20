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
            <div
              class="avatar-wrap"
              :class="{ 'avatar-wrap-disabled': !canEditProfile }"
              @click="onChangeAvatarClick"
            >
              <img class="avatar" :src="avatar" alt="avatar" />
              <div v-if="avatarUploading" class="avatar-mask">上传中...</div>
            </div>
            <input
              ref="avatarInput"
              type="file"
              accept="image/*"
              class="avatar-input"
              @change="onAvatarSelected"
            />
            <div class="user-meta">
              <div class="name-row">
                <div class="name">{{ nickname }}</div>
                <span class="badge level">LV5</span>
                <span class="badge vip">大会员</span>
              </div>
              <div
                class="sub-row"
                :class="{ 'sub-row-disabled': !canEditProfile }"
                @click="editBio"
              >
                <span v-if="bio">{{ bio }}</span>
                <span v-else class="bio-placeholder">编辑个性签名</span>
              </div>
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
            <span class="tab-icon" :class="t.key">
              <!-- 主页图标 -->
              <svg v-if="t.key === 'home'" t="1769841391403" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="22" height="22">
                <path d="M691.1168 896 577.7792 896c-18.1824 0-32.9344-14.8416-32.9344-33.1456l0-225.472c0-1.5424 0.0128-3.0464 0.0384-4.4928L479.0464 632.8896c0.0128 1.4656 0.032 2.9696 0.032 4.4928l0 225.472c0 18.2976-14.7584 33.1456-32.9408 33.1456L332.8064 896c-64.5632 0-117.088-56.992-117.088-127.0656L215.7184 589.0304l-6.0608 0c-34.3552 0-62.6688-17.6128-75.7312-47.136-10.7008-24.3456-9.536-63.3792 25.792-100.1408l266.2144-277.0048C448.7616 141.0432 479.3152 128 511.9744 128s63.2064 13.0432 86.0224 36.7296l266.2528 277.0048c35.4624 36.8832 36.544 75.9616 25.7344 100.2304-12.9152 29.408-41.2352 47.0528-75.6928 47.0528l-6.0672 0 0 179.9168C808.2048 839.008 755.68 896 691.1168 896L691.1168 896zM610.7328 829.7152l80.3904 0c31.6352 0 51.2064-31.5456 51.2064-60.7808L742.3296 555.8784c0-18.2912 14.7584-33.1456 32.9408-33.1456l39.0016 0c10.6752 0 13.8368-3.8976 15.5008-7.7248 1.248-3.4496 0.16-13.5936-12.9024-27.168L550.656 210.8544c-20.5248-21.3504-56.8192-21.3504-77.3888 0.0128L207.0656 487.8464c-13.0752 13.6064-14.144 23.7952-12.8768 27.2832 1.6128 3.6672 4.8064 7.6032 15.4496 7.6032l39.0208 0c18.1824 0 32.9408 14.8416 32.9408 33.1456l0 213.0624c0 29.2352 19.5648 60.7808 51.2064 60.7808l80.3904 0-0.096-199.9936c-0.2944-14.9952-0.6592-33.664 13.7216-48.4096 14.3616-14.7328 33.9456-14.7328 40.3648-14.7328l89.568 0c6.4384 0 26.0032 0 40.3648 14.7328 14.3616 14.752 14.0032 33.4208 13.7088 48.4096l-0.096 7.6608L610.7328 829.7152 610.7328 829.7152zM610.7328 829.7152" fill="currentColor"/>
              </svg>
              <!-- 动态图标 -->
              <svg v-else-if="t.key === 'dynamics'" t="1769841252767" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="22" height="22">
                <path d="M541.87 55.47l-0.06 296.95c44.59-51.23 110.27-83.62 183.53-83.62 134.32 0 243.2 108.88 243.2 243.2v29.87l-296.95-0.06c51.23 44.59 83.62 110.27 83.62 183.53 0 134.32-108.88 243.2-243.2 243.2h-29.87l0.02-296.91c-44.59 51.21-110.26 83.58-183.49 83.58-134.32 0-243.2-108.88-243.2-243.2v-29.87l296.95 0.06c-51.23-44.59-83.62-110.27-83.62-183.53 0-134.32 108.88-243.2 243.2-243.2h29.87z m0 488.83v362.03l5-0.83c81.83-15.74 144.32-85.85 148.39-171.27l0.21-8.89c0-89.41-63.95-163.87-148.6-180.16l-5-0.88z m-62.17-2.43H117.67l0.83 5c15.74 81.83 85.85 144.32 171.27 148.39l8.89 0.21c89.41 0 163.87-63.95 180.16-148.6l0.88-5z m245.63-213.34c-89.41 0-163.87 63.95-180.16 148.6l-0.88 5h362.03l-0.83-5c-15.74-81.83-85.85-144.32-171.27-148.39l-8.89-0.21z m-243.2-210.9l-5 0.88c-84.65 16.28-148.6 90.75-148.6 180.16l0.21 8.89c4.07 85.42 66.56 155.53 148.39 171.27l5 0.83V117.63z" fill="currentColor"/>
              </svg>
              <!-- 投稿图标 -->
              <svg v-else-if="t.key === 'submit'" t="1769843304015" class="icon" viewBox="0 0 1030 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="22" height="22">
                <path d="M973.69003 866.154339l-341.707922-1.436626 362.526207-355.681248c21.19403-21.193831 21.19403-55.687044 0-76.900661L651.582819 89.230294c-21.19403-21.213417-55.687044-21.193831-76.881075 0L38.095563 625.798301c-21.174244 21.213617-21.193831 55.705632 0 76.899462l165.927901 165.875536-157.660106-2.419959c-22.238722 0-40.255107 18.017584-40.255107 40.256306l0-3.030745c0 22.239921 18.016385 40.256306 40.255107 40.256306l927.326473 0c22.220335 0 40.255107-18.016385 40.255107-40.256306l0 3.030745C1013.945137 884.171923 995.910365 866.154339 973.69003 866.154339zM306.230967 903.780629l-60.759005-60.759005 0 0L71.622432 669.190481c-2.703567-2.722154-2.74294-7.143157 0-9.884898L608.190439 122.737376c2.741741-2.74294 7.161744-2.703567 9.884898 0l342.90591 342.925497c2.723154 2.74274 2.723154 7.12357 0.019587 9.846724L593.488007 843.021423l-60.757806 60.759005L306.230967 903.780429zM615.196289 180.595348c-14.149007-1.677063-27.311883 8.34754-29.106866 22.614467l-49.235019 390.995505L150.555116 637.77538c-14.3073 1.618103-24.58753 14.504966-22.970426 28.811066 1.500383 13.31897 12.78733 23.147306 25.87046 23.147306 0.967544 0 1.953476-0.039373 2.940606-0.158293l406.682869-45.861106c11.97708-1.340691 21.451656-10.714735 22.94964-22.634054l51.780293-411.359498C639.605739 195.435287 629.481404 182.391531 615.196289 180.595348z" fill="currentColor"/>
              </svg>
              <!-- 收藏图标 -->
              <svg v-else-if="t.key === 'collections'" t="1769841362388" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="22" height="22">
                <path d="M327.728136 804.301276l150.60509-60.966537 33.721009-13.627369 33.721009 13.627369 150.609183 60.914348-4.484128-166.562527-0.907673-32.985252 20.663622-25.711592 96.563266-120.353089L652.842749 411.574776l-30.030967-9.081843-17.825994-25.716708-92.931552-134.033669-92.871177 134.033669-17.825994 25.716708-30.033014 9.081843-155.490354 47.120181 96.566336 120.29476 20.663622 25.711592-0.907673 32.985252L327.728136 804.301276M789.940197 939.129031 512.054235 826.727031 234.17339 939.129031l8.115841-303.886125L62.389956 411.175687l282.879696-85.667102L512.054235 84.869946l166.843936 240.638639 282.711873 85.667102L781.766027 635.243929 789.940197 939.129031 789.940197 939.129031 789.940197 939.129031z" fill="currentColor"/>
              </svg>
            </span>
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
      <div class="content-inner user-profile-container" :class="{ 'no-left-panel': activeTab !== 'collections' }">
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
                @click="onFolderSelect(f)"
              >
                <div class="folder-main">
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
            <div class="fav-cover">
              <img 
                v-if="activeFolder?.coverUrl" 
                :src="normalizeImageUrl(activeFolder.coverUrl)" 
                alt="收藏夹封面" 
                @error="onImageError"
              />
              <div v-else class="fav-cover-placeholder">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M4 16L8.586 11.414C9.367 10.633 10.633 10.633 11.414 11.414L16 16M14 14L15.586 12.414C16.367 11.633 17.633 11.633 18.414 12.414L20 14M14 8H14.01M6 20H18C19.105 20 20 19.105 20 18V6C20 4.895 19.105 4 18 4H6C4.895 4 4 4.895 4 6V18C4 19.105 4.895 20 6 20Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="fav-left">
              <div class="fav-title">{{ activeFolder?.name || '默认收藏夹' }}</div>
              <div class="fav-sub">公开 · 视频数：{{ activeFolder?.count ?? 0 }}</div>
            </div>
            <button v-if="!isBatchMode" class="play-all">播放全部</button>
            <div class="fav-tools">
              <button v-if="!isBatchMode" class="tool-btn" @click="enterBatchMode">批量操作</button>
              <button v-else class="tool-btn" @click="exitBatchMode">退出管理</button>
            </div>
          </div>

          <!-- 批量操作头部 -->
          <div v-if="activeTab === 'collections' && isBatchMode" class="batch-header">
            <div class="batch-left">
              <label class="select-all-label">
                <input 
                  type="checkbox" 
                  :checked="isAllSelected" 
                  @change="toggleSelectAll"
                />
                <span>全选</span>
              </label>
              <span class="selected-count">已经选择 {{ selectedFavoriteIds.length }} 个视频</span>
            </div>
            <div class="batch-right">
              <button 
                class="batch-btn delete-btn" 
                @click="batchUnfavorite" 
                :disabled="selectedFavoriteIds.length === 0"
              >
                批量取消收藏
              </button>
              <button 
                class="batch-btn move-btn" 
                @click="openBatchMoveDialog" 
                :disabled="selectedFavoriteIds.length === 0"
              >
                批量移动
              </button>
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
              :class="{ 
                'batch-mode': isBatchMode,
                'selected': isBatchMode && selectedFavoriteIds.includes(v.favoriteId)
              }"
            >
              <div 
                class="card-inner"
                :class="{ 'selected': isBatchMode && selectedFavoriteIds.includes(v.favoriteId) }"
              >
                <!-- 批量选择复选框 -->
                <div v-if="isBatchMode" class="batch-checkbox">
                  <input 
                    type="checkbox" 
                    :checked="selectedFavoriteIds.includes(v.favoriteId)"
                    @change="toggleSelect(v.favoriteId)"
                    @click.stop
                  />
                </div>
                <div class="thumb" @click="isBatchMode ? null : openVideoInNewTab(v.id)">
                  <img v-if="v.cover" :src="v.cover" alt="" @error="onImageError" />
                <div v-else class="thumb-ph" />
                <span class="duration">{{ v.duration }}</span>
              </div>
                <div class="v-title-row">
                  <div class="v-title" :title="v.title" @click="openVideoInNewTab(v.id)">{{ v.title }}</div>
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

          <!-- 分页组件 -->
          <div v-if="activeTab === 'collections' && total > 0" class="pagination-wrapper">
            <el-config-provider :locale="zhCn">
              <el-pagination
                v-model:current-page="page"
                :page-size="pageSize"
                :total="total"
                layout="prev, pager, next, jumper, total"
                @current-change="handlePageChange"
              />
            </el-config-provider>
          </div>

          <!-- 移动到收藏夹弹层（单个） -->
          <div
            v-if="moveDialog.visible && !moveDialog.isBatch"
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

          <!-- 批量移动到收藏夹弹层 -->
          <div
            v-if="moveDialog.visible && moveDialog.isBatch"
            class="move-dialog-mask"
            @click="closeMoveDialog"
          >
            <div class="move-dialog" @click.stop>
              <div class="move-dialog-title">批量移动到（已选择 {{ selectedFavoriteIds.length }} 个视频）</div>
              <ul class="move-folder-list">
                <li
                  v-for="f in folders"
                  :key="f.id"
                  class="move-folder-item"
                  @click="confirmBatchMove(f.id)"
                >
                  <span class="name">{{ f.name }}</span>
                  <span class="count">{{ f.count }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- 编辑收藏夹弹层 -->
          <div
            v-if="editFolderDialog.visible"
            class="edit-folder-dialog-mask"
            @click="closeEditFolderDialog"
          >
            <div class="edit-folder-dialog" @click.stop>
              <div class="edit-folder-header">
                <div class="edit-folder-title">{{ editFolderDialog.mode === 'create' ? '收藏夹信息' : '编辑收藏夹' }}</div>
                <button class="edit-folder-close" @click="closeEditFolderDialog">×</button>
              </div>
              <div class="edit-folder-body">
                <!-- 封面图片 -->
                <div class="edit-folder-cover">
                  <img
                    v-if="editFolderDialog.coverUrl"
                    :src="editFolderDialog.coverUrl"
                    alt="封面"
                    @error="onImageError"
                  />
                  <div v-else class="edit-folder-cover-placeholder">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M4 16L8.586 11.414C9.367 10.633 10.633 10.633 11.414 11.414L16 16M14 14L15.586 12.414C16.367 11.633 17.633 11.633 18.414 12.414L20 14M14 8H14.01M6 20H18C19.105 20 20 19.105 20 18V6C20 4.895 19.105 4 18 4H6C4.895 4 4 4.895 4 6V18C4 19.105 4.895 20 6 20Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </div>
                </div>
                <!-- 名称输入 -->
                <div class="edit-folder-field">
                  <label class="edit-folder-label">名称 <span class="required-star">*</span></label>
                  <div class="edit-folder-input-wrapper">
                    <input
                      v-model="editFolderDialog.name"
                      type="text"
                      class="edit-folder-input"
                      placeholder="请输入收藏夹名称"
                      maxlength="20"
                    />
                    <span class="edit-folder-char-count">{{ editFolderDialog.name.length }} / 20</span>
                  </div>
                </div>
                <!-- 公开/私密 -->
                <div class="edit-folder-field">
                  <label class="edit-folder-label">公开</label>
                  <div class="edit-folder-switch-wrapper">
                    <label class="edit-folder-switch">
                      <input
                        v-model="editFolderDialog.isPublic"
                        type="checkbox"
                        class="edit-folder-switch-input"
                      />
                      <span class="edit-folder-switch-slider"></span>
                    </label>
                    <span class="edit-folder-switch-text">{{ editFolderDialog.isPublic ? '公开' : '私密' }}</span>
                  </div>
                </div>
                <!-- 描述 -->
                <div class="edit-folder-field">
                  <label class="edit-folder-label">简介</label>
                  <textarea
                    v-model="editFolderDialog.description"
                    class="edit-folder-textarea"
                    placeholder="可以简单描述下你的收藏夹"
                    maxlength="200"
                    rows="4"
                  ></textarea>
                  <div class="edit-folder-char-count-right">{{ editFolderDialog.description.length }} / 200</div>
                </div>
              </div>
              <div class="edit-folder-footer">
                <button class="edit-folder-btn cancel" @click="closeEditFolderDialog">取消</button>
                <button class="edit-folder-btn confirm" @click="confirmEditFolder">{{ editFolderDialog.mode === 'create' ? '创建' : '确定' }}</button>
              </div>
            </div>
          </div>

          <ProfileHome
            v-else-if="activeTab === 'home'"
            :user-id="currentUserId"
            @open-folder="handleOpenFolder"
          />
          <div v-else-if="activeTab === 'dynamics'" class="empty">动态内容</div>
          <div v-else-if="activeTab === 'submit'" class="empty">投稿内容</div>
        </section>
      </div>
    </main>
  </div>
</template>

<script>
import TopHeader from '@/components/TopHeader.vue'
import ProfileHome from './ProfileHome.vue'
import { getFavoriteListByFolder } from '@/api/favorite'
import { getFavoriteFolderList, createFavoriteFolder } from '@/api/favoriteFolder'
import { fetchMyProfile, updateAvatar as apiUpdateAvatar, updateBio as apiUpdateBio } from '@/api/userProfile'
import { getUserStats } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const DEFAULT_GREY_AVATAR = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(
  `<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120" viewBox="0 0 120 120">
    <circle cx="60" cy="60" r="58" fill="#d1d5db"/>
    <circle cx="60" cy="48" r="18" fill="#b6c0ca"/>
    <path d="M22 120c6-30 25-46 38-46s32 16 38 46" fill="#b6c0ca"/>
  </svg>`
)}`;

export default {
  name: 'UserProfile',
  components: { TopHeader, ProfileHome },
  data () {
    return {
      zhCn,
      coverImage: '/assets/topheader/favorite.png',
      activeTab: 'collections',
      tabs: [
        { key: 'home', label: '主页' },
        { key: 'dynamics', label: '动态' },
        { key: 'submit', label: '投稿' },
        { key: 'collections', label: '收藏', count: 0 }
      ],
      nickname: '-',
      avatar: DEFAULT_GREY_AVATAR,
      bio: '',
      avatarUploading: false,
      // store 引用（用于刷新后从 localStorage 同步头像/签名）
      userStore: null,
      stats: { following: 0, followers: 0, likes: 0, views: 0 },
      activeFolderId: null,
      folders: [],
      followedFolders: [],
      videos: [],
      loading: false,
      page: 1,
      pageSize: 20,
      total: 0,
      // 右侧视频更多菜单
      videoMenuForId: null,
      // 左侧收藏夹更多菜单
      folderMenuForId: null,
      // 批量操作相关
      isBatchMode: false,
      selectedFavoriteIds: [], // 选中的收藏ID列表（favoriteId）
      // 视频数据缓存：{ folderId: { videos: [], total: number, page: number } }
      videosCache: {},
      // 移动视频对话框
      moveDialog: {
        visible: false,
        video: null,
        isBatch: false // 是否为批量移动
      },
      // 编辑收藏夹对话框
      editFolderDialog: {
        visible: false,
        mode: 'create', // 'create' 或 'edit'
        folder: null,
        name: '',
        description: '',
        isPublic: true,
        coverUrl: ''
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
    },
    myUserId () {
      const userStore = this.userStore || useUserStore()
      return userStore.user?.userId || userStore.user?.id
    },
    // 仅当查看的是自己的主页时才允许编辑（头像/姓名/签名）
    canEditProfile () {
      const viewingId = this.$route?.params?.id
      const myId = this.myUserId
      if (!myId) return false
      // /profile（不带 id）默认就是自己的主页
      if (!viewingId) return true
      return String(viewingId) === String(myId)
    },
    // 是否全选
    isAllSelected () {
      if (this.videos.length === 0) return false
      return this.videos.every(v => this.selectedFavoriteIds.includes(v.favoriteId))
    }
  },
  created () {
    // 刷新页面时，优先从 userStore(localStorage) 同步头像/签名，避免接口未返回/网络慢导致空白
    this.userStore = useUserStore()
    this.syncProfileFromStore()
    // 监听 store.user 变化（比如上传头像成功后 store 更新），同步到本页展示
    this.$watch(
      () => this.userStore.user,
      () => this.syncProfileFromStore(),
      { deep: true }
    )
  },
  mounted () {
    // 检查URL参数，如果有tab参数，切换到对应tab
    const urlParams = new URLSearchParams(window.location.search)
    const tab = urlParams.get('tab')
    
    if (tab === 'collections') {
      this.activeTab = 'collections'
    }
    
    // 先加载当前用户资料（头像 + 签名）
    this.loadProfile()
      .finally(() => {
        // 如果初始就是收藏tab，则加载数据（initCollections 会处理 folder 参数）
        if (this.activeTab === 'collections' && this.currentUserId) {
          this.initCollections()
        }

        // 加载用户统计信息（关注/粉丝/获赞/播放）
        if (this.currentUserId) {
          this.loadUserStats(this.currentUserId)
        }
      })
    
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
    openVideoInNewTab (videoId) {
      if (!videoId) return
      const base = window.__MICRO_APP_BASE_ROUTE__ || ''
      const normalizedBase = base.replace(/\/$/, '')
      const path = `/video/${encodeURIComponent(videoId)}`
      const url = `${normalizedBase}${path}`
      window.open(url, '_blank')
    },
    syncProfileFromStore () {
      try {
        const store = this.userStore || useUserStore()
        const routeUserId = this.$route?.params?.id
        const myUserId = store.user?.userId || store.user?.id
        // 当路由指向“他人主页”时，不覆盖显示当前用户的头像/昵称/简介
        if (routeUserId && myUserId && String(routeUserId) !== String(myUserId)) {
          const q = this.$route?.query || {}
          if (typeof q.nickname === 'string' && q.nickname.trim()) this.nickname = q.nickname.trim()
          if (typeof q.avatar === 'string' && q.avatar.trim()) this.avatar = this.normalizeAvatarUrl(q.avatar.trim())
          if (typeof q.bio === 'string' && q.bio.trim()) this.bio = q.bio.trim()
          return
        }
        const u = store.user || {}
        if (u.username) this.nickname = u.username
        // 头像优先取 store 的 avatar（刷新后 localStorage 会带着）
        if (u.avatar) {
          this.avatar = this.normalizeAvatarUrl(u.avatar)
        } else if (!this.avatar) {
          // 兜底占位
          this.avatar = '/public/favicon.ico'
        }
        if (typeof u.bio === 'string') {
          this.bio = u.bio
        }
      } catch (e) {
        // ignore
      }
    },
    normalizeAvatarUrl (url) {
      if (!url) return '/public/favicon.ico'
      // 如果已经是完整 URL（http/https），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      // 如果是相对路径（以 / 开头），直接返回
      if (url.startsWith('/')) {
        return url
      }
      // 其他情况，当作相对路径处理
      return '/' + url
    },
    normalizeImageUrl (url) {
      if (!url) return ''
      // 如果已经是完整 URL（http/https），直接返回
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url
      }
      // 如果是相对路径（以 / 开头），直接返回
      if (url.startsWith('/')) {
        return url
      }
      // 其他情况，当作相对路径处理
      return '/' + url
    },
    async loadProfile () {
      try {
        const userStore = this.userStore || useUserStore()
        if (!userStore.isAuthenticated) return
        const routeUserId = this.$route?.params?.id
        const myUserId = userStore.user?.userId || userStore.user?.id
        // 他人主页：避免调用“我的资料”接口覆盖展示内容
        if (routeUserId && myUserId && String(routeUserId) !== String(myUserId)) {
          const q = this.$route?.query || {}
          if (typeof q.nickname === 'string' && q.nickname.trim()) this.nickname = q.nickname.trim()
          if (typeof q.avatar === 'string' && q.avatar.trim()) this.avatar = this.normalizeAvatarUrl(q.avatar.trim())
          if (typeof q.bio === 'string' && q.bio.trim()) this.bio = q.bio.trim()
          return
        }
        const { data } = await fetchMyProfile()
        if (data && data.id) {
          this.nickname = data.username || this.nickname
          if (data.avatar) {
            this.avatar = this.normalizeAvatarUrl(data.avatar)
          }
          this.bio = data.bio || ''
          // 同步到全局 userStore，保证顶部头像一致
          const currentUser = userStore.user || {}
          const normalizedAvatar = data.avatar ? this.normalizeAvatarUrl(data.avatar) : (currentUser.avatar || '')
          userStore.setUser({
            ...currentUser,
            id: data.id,
            userId: data.id,
            username: data.username || currentUser.username,
            loginAccount: currentUser.loginAccount || data.account,
            avatar: normalizedAvatar,
            bio: data.bio || currentUser.bio
          })
        }
      } catch (e) {
        console.warn('加载用户资料失败:', e)
      }
    },
    async loadUserStats (targetUserId) {
      if (!targetUserId) return
      try {
        const { data } = await getUserStats(targetUserId)
        if (data?.success) {
          const s = data.stats || {}
          const num = (v) => (v === undefined || v === null || v === '' ? 0 : Number(v))
          this.stats = {
            following: num(s.followingCount ?? s.following ?? 0),
            followers: num(s.followerCount ?? s.followers ?? 0),
            likes: num(s.likeCount ?? s.likes ?? 0),
            views: num(s.viewCount ?? s.views ?? 0),
          }
        }
      } catch (e) {
        // 不阻断页面展示
      }
    },
    onTabChange (key) {
      // 如果切换到收藏tab，重置并加载数据
      if (key === 'collections' && this.currentUserId) {
      this.activeTab = key
        this.initCollections()
      } else {
        this.activeTab = key
      }
    },

    // 点击头像更换
    onChangeAvatarClick () {
      if (!this.canEditProfile) return
      if (this.avatarUploading) return
      const input = this.$refs.avatarInput
      if (input) {
        input.click()
      }
    },

    // 选择头像文件
    async onAvatarSelected (event) {
      if (!this.canEditProfile) return
      const file = event?.target?.files?.[0]
      if (!file) return
      this.avatarUploading = true
      try {
        const { data } = await apiUpdateAvatar(file)
        if (data && data.success && data.avatar) {
          const normalizedAvatar = this.normalizeAvatarUrl(data.avatar)
          this.avatar = normalizedAvatar
          ElMessage.success('头像已更新')
          // 更新全局 store
          const userStore = useUserStore()
          const currentUser = userStore.user || {}
          userStore.setUser({
            ...currentUser,
            avatar: normalizedAvatar
          })
        } else {
          ElMessage.error(data?.message || '头像更新失败')
        }
      } catch (e) {
        console.error('头像上传失败:', e)
        ElMessage.error('头像上传失败，请稍后重试')
      } finally {
        this.avatarUploading = false
        if (event && event.target) {
          // 允许再次选择同一个文件
          event.target.value = ''
        }
      }
    },

    // 编辑个性签名
    async editBio () {
      if (!this.canEditProfile) return
      const current = this.bio || ''
      try {
        const { value } = await ElMessageBox.prompt('请输入个性签名', '编辑个性签名', {
          inputValue: current,
          inputPlaceholder: '这个人很神秘，还没有写签名~',
          inputType: 'textarea',
          confirmButtonText: '保存',
          cancelButtonText: '取消',
          inputMaxlength: 80,
          inputValidator: (val) => {
            if (val && val.length > 80) return '签名不能超过 80 个字符'
            return true
          }
        })
        const bio = (value || '').trim()
        const { data } = await apiUpdateBio(bio)
        if (data && data.success) {
          this.bio = data.bio || bio
          ElMessage.success('签名已更新')
          const userStore = useUserStore()
          const currentUser = userStore.user || {}
          userStore.setUser({
            ...currentUser,
            bio: this.bio
          })
        } else {
          ElMessage.error(data?.message || '签名更新失败')
        }
      } catch (e) {
        if (e === 'cancel' || e === 'close') return
        console.error('更新签名失败:', e)
      }
    },
    async initCollections () {
      await this.loadFolders()
      // 检查URL参数，如果有folder参数，优先使用
      const urlParams = new URLSearchParams(window.location.search)
      const folderId = urlParams.get('folder')
      let targetFolderId = null
      
      if (folderId) {
        const folderIdNum = parseInt(folderId)
        if (!isNaN(folderIdNum)) {
          const folder = this.folders.find(f => f.id === folderIdNum)
          if (folder) {
            targetFolderId = folderIdNum
          }
        }
      }
      
      // 如果没有指定folder，默认选中第一个（通常是"默认收藏夹"）
      if (!targetFolderId && !this.activeFolderId && this.folders.length > 0) {
        targetFolderId = this.folders[0].id
      } else if (!targetFolderId) {
        targetFolderId = this.activeFolderId
      }
      
      if (targetFolderId) {
        this.activeFolderId = targetFolderId
        // 检查缓存
        const cached = this.videosCache[targetFolderId]
        if (cached) {
          // 使用缓存数据
          this.videos = cached.videos
          this.total = cached.total
          this.page = cached.page || 1
          this.tabs.find(t => t.key === 'collections').count = cached.total
          // 如果收藏夹没有封面，从缓存的视频列表中获取
          const currentFolder = this.folders.find(f => f.id === targetFolderId)
          if (currentFolder && !currentFolder.coverUrl && cached.videos.length > 0 && cached.videos[0].cover) {
            currentFolder.coverUrl = cached.videos[0].cover
          }
        } else {
          // 没有缓存，加载数据
          await this.loadFavorites(this.currentUserId, targetFolderId, true)
        }
      }
    },

    async loadFolders () {
      try {
        const { data } = await getFavoriteFolderList(this.currentUserId)
        if (data.success) {
          this.folders = (data.list || []).map(it => ({
            id: it.id,
            name: it.name,
            count: it.count ?? 0,
            description: it.description || '',
            // 初始不信任后端封面，统一前端按“最新收藏视频”原则计算
            coverUrl: '',
            isPublic: it.isPublic !== false
          }))
          // 异步为每个有视频的收藏夹计算“最新收藏视频封面”
          this.folders.forEach(folder => {
            if (folder.count > 0) {
              this.updateFolderCover(folder.id)
            }
          })
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

    // 统一计算某个收藏夹的“封面”：该收藏夹最新收藏的视频封面
    async updateFolderCover (folderId) {
      if (!folderId || !this.currentUserId) return
      try {
        const { data } = await getFavoriteListByFolder(this.currentUserId, folderId, 1, 1)
        if (data && data.success && data.list && data.list.length > 0) {
          const firstVideo = data.list[0]
          const newCoverUrl = firstVideo.coverUrl || ''
          if (newCoverUrl && newCoverUrl.trim() !== '') {
            // 更新当前页左侧列表中的封面
            const folder = this.folders.find(f => f.id === folderId)
            if (folder) {
              folder.coverUrl = this.normalizeImageUrl(newCoverUrl)
            }
            // 如果当前激活的就是这个收藏夹，同步更新顶部展示封面
            if (this.activeFolderId === folderId && this.activeFolder) {
              this.activeFolder.coverUrl = this.normalizeImageUrl(newCoverUrl)
            }
            // 如果编辑弹窗正在编辑这个收藏夹，同步更新预览封面
            if (this.editFolderDialog.visible && this.editFolderDialog.folder && this.editFolderDialog.folder.id === folderId) {
              this.editFolderDialog.coverUrl = this.normalizeImageUrl(newCoverUrl)
            }
            // 通知其他页面（如主页 ProfileHome）同步更新该收藏夹封面
            window.dispatchEvent(new CustomEvent('favorite-folder-cover-updated', {
              detail: { folderId, coverUrl: this.normalizeImageUrl(newCoverUrl) }
            }))
          }
        }
      } catch (e) {
        console.error('更新收藏夹封面失败:', e)
      }
    },

    onFolderSelect (folder) {
      if (!folder || !folder.id) return
      if (folder.id === this.activeFolderId) return
      
      // 检查缓存中是否有该收藏夹的数据
      const cached = this.videosCache[folder.id]
      if (cached) {
        // 使用缓存数据
        this.activeFolderId = folder.id
        this.videos = cached.videos
        this.total = cached.total
        this.page = cached.page || 1
        // 更新tab计数
        this.tabs.find(t => t.key === 'collections').count = cached.total
        // 如果收藏夹没有封面，从缓存的视频列表中获取
        if (!folder.coverUrl && cached.videos.length > 0 && cached.videos[0].cover) {
          folder.coverUrl = cached.videos[0].cover
        }
      } else {
        // 没有缓存，加载数据
        this.activeFolderId = folder.id
        this.loadFavorites(this.currentUserId, folder.id, true)
      }
    },

    // 处理从主页点击收藏夹的事件（不改变URL地址）
    handleOpenFolder (folderId) {
      if (!folderId) return
      
      // 切换到收藏tab
      if (this.activeTab !== 'collections') {
        this.onTabChange('collections')
      }
      
      // 等待tab切换和收藏夹列表加载完成后再选择收藏夹
      this.$nextTick(() => {
        // 查找对应的收藏夹
        const folder = this.folders.find(f => f.id === folderId)
        if (folder) {
          this.onFolderSelect(folder)
        } else {
          // 如果收藏夹列表还没加载完，等待加载完成
          const checkFolder = () => {
            const foundFolder = this.folders.find(f => f.id === folderId)
            if (foundFolder) {
              this.onFolderSelect(foundFolder)
            } else if (this.folders.length > 0) {
              // 如果列表已加载但找不到，可能是延迟加载，再等一次
              setTimeout(checkFolder, 100)
            }
          }
          checkFolder()
        }
      })
    },

    async onCreateFolder () {
      if (!this.currentUserId) return
      this.editFolderDialog = {
        visible: true,
        mode: 'create',
        folder: null,
        name: '',
        description: '',
        isPublic: true,
        coverUrl: ''
      }
    },

    async loadFavorites (userId, folderId, reset = false) {
      if (this.loading) return
      
      // 重置时清空数据并重置分页
      if (reset) {
        this.page = 1
        this.videos = []
      }
      
      this.loading = true
      try {
        const { data } = await getFavoriteListByFolder(userId, folderId, this.page, this.pageSize)
        if (data.success) {
          let list = data.list || []
          const total = data.total || 0
          
          // 按收藏时间倒序排序（最新收藏的排在最前）
          list = list.slice().sort((a, b) => {
            const ta = a.createTime ? new Date(a.createTime).getTime() : 0
            const tb = b.createTime ? new Date(b.createTime).getTime() : 0
            return tb - ta
          })
          
          // 格式化视频数据
          const formattedVideos = list.map(item => ({
            id: item.videoId || item.id,
            favoriteId: item.id,
            cover: item.coverUrl || '',
            title: item.title || '未知标题',
            duration: item.duration || '00:00',
            play: '--',
            danmaku: '--',
            time: this.formatTime(item.createTime)
          }))
          
          // 替换数据（分页模式，不再追加）
          this.videos = formattedVideos
          
          this.total = total
          this.tabs.find(t => t.key === 'collections').count = total
          // 更新当前收藏夹数量（后端列表也有 count，但这里做一次即时刷新）
          const currentFolder = this.folders.find(f => f.id === folderId)
          if (currentFolder) {
            currentFolder.count = total
          }
          
          // 缓存数据
          this.videosCache[folderId] = {
            videos: formattedVideos,
            total: total,
            page: this.page
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

    // 分页切换处理
    handlePageChange (newPage) {
      this.page = newPage
      if (this.activeFolderId) {
        // 检查缓存中是否有该页的数据
        const cached = this.videosCache[this.activeFolderId]
        if (cached && cached.page === newPage) {
          // 使用缓存数据
          this.videos = cached.videos
          this.total = cached.total
        } else {
          // 没有缓存，加载数据
          this.loadFavorites(this.currentUserId, this.activeFolderId, false)
        }
        // 滚动到顶部
        this.$nextTick(() => {
          const rightPanel = document.querySelector('.right-panel')
          if (rightPanel) {
            rightPanel.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }
        })
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
          // 清除当前收藏夹的缓存，重新加载
          this.clearFolderCache(this.activeFolderId)
          await this.initCollections()
        } else {
          ElMessage.warning(data.message || '取消收藏失败')
        }
      } catch (e) {
        console.error('取消收藏失败:', e)
        ElMessage.error('取消收藏失败')
      }
    },

    // 批量操作相关方法
    enterBatchMode () {
      this.isBatchMode = true
      this.selectedFavoriteIds = []
    },

    exitBatchMode () {
      this.isBatchMode = false
      this.selectedFavoriteIds = []
    },

    toggleSelectAll (e) {
      if (e.target.checked) {
        // 全选：添加所有视频的 favoriteId
        this.selectedFavoriteIds = this.videos.map(v => v.favoriteId).filter(id => id)
      } else {
        // 取消全选
        this.selectedFavoriteIds = []
      }
    },

    toggleSelect (favoriteId) {
      const index = this.selectedFavoriteIds.indexOf(favoriteId)
      if (index > -1) {
        this.selectedFavoriteIds.splice(index, 1)
      } else {
        this.selectedFavoriteIds.push(favoriteId)
      }
    },

    // 批量取消收藏
    async batchUnfavorite () {
      if (this.selectedFavoriteIds.length === 0) {
        ElMessage.warning('请先选择要取消收藏的视频')
        return
      }

      try {
        await ElMessageBox.confirm(
          `确定要取消收藏选中的 ${this.selectedFavoriteIds.length} 个视频吗？`,
          '批量取消收藏',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )

        const userStore = useUserStore()
        const userId = userStore.user?.userId || userStore.user?.id
        if (!userId) return

        const { batchDeleteFavorites } = await import('@/api/favorite')
        const { data } = await batchDeleteFavorites(userId, this.selectedFavoriteIds)
        
        if (data.success) {
          ElMessage.success(`已取消收藏 ${this.selectedFavoriteIds.length} 个视频`)
          this.selectedFavoriteIds = []
          // 清除当前收藏夹的缓存，重新加载
          this.clearFolderCache(this.activeFolderId)
          await this.initCollections()
        } else {
          ElMessage.warning(data.message || '批量取消收藏失败')
        }
      } catch (e) {
        if (e !== 'cancel') {
          console.error('批量取消收藏失败:', e)
          ElMessage.error('批量取消收藏失败')
        }
      }
    },

    // 打开批量移动对话框
    openBatchMoveDialog () {
      if (this.selectedFavoriteIds.length === 0) {
        ElMessage.warning('请先选择要移动的视频')
        return
      }
      this.moveDialog.visible = true
      this.moveDialog.isBatch = true
      this.moveDialog.video = null
    },

    openMoveDialog (video) {
      this.moveDialog.video = video
      this.moveDialog.visible = true
      this.moveDialog.isBatch = false
      this.videoMenuForId = null
    },

    closeMoveDialog () {
      this.moveDialog.visible = false
      this.moveDialog.video = null
      this.moveDialog.isBatch = false
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
          // 清除相关收藏夹的缓存
          this.clearFolderCache(this.activeFolderId)
          this.clearFolderCache(targetFolderId)
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

    // 批量移动
    async confirmBatchMove (targetFolderId) {
      if (!targetFolderId || targetFolderId === this.activeFolderId) {
        ElMessage.warning('不能移动到当前收藏夹')
        return
      }

      if (this.selectedFavoriteIds.length === 0) {
        this.closeMoveDialog()
        return
      }

      const userStore = useUserStore()
      const userId = userStore.user?.userId || userStore.user?.id
      if (!userId) return

      try {
        // 获取选中的视频信息
        const selectedVideos = this.videos.filter(v => this.selectedFavoriteIds.includes(v.favoriteId))
        
        // 批量移动：循环调用单个移动API
        const { addFavoriteToFolder } = await import('@/api/favorite')
        let successCount = 0
        let failCount = 0

        for (const video of selectedVideos) {
          try {
            const { data } = await addFavoriteToFolder(userId, video.id, targetFolderId)
            if (data.success) {
              successCount++
            } else {
              failCount++
            }
          } catch (e) {
            failCount++
          }
        }

        if (successCount > 0) {
          ElMessage.success(`已成功移动 ${successCount} 个视频${failCount > 0 ? `，${failCount} 个失败` : ''}`)
          this.selectedFavoriteIds = []
          this.closeMoveDialog()
          // 清除相关收藏夹的缓存
          this.clearFolderCache(this.activeFolderId)
          this.clearFolderCache(targetFolderId)
          await this.initCollections()
        } else {
          ElMessage.warning('移动失败，请稍后重试')
        }
      } catch (e) {
        console.error('批量移动失败:', e)
        ElMessage.error('批量移动失败')
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
        ElMessage.warning('默认收藏夹不允许编辑')
        return
      }
      
      // 先打开对话框，显示基本信息（使用已有的封面或空）
      let coverUrl = folder.coverUrl || ''
      this.editFolderDialog = {
        visible: true,
        mode: 'edit',
        folder: folder,
        name: folder.name || '',
        description: folder.description || '',
        isPublic: folder.isPublic !== false,
        coverUrl: coverUrl
      }
      this.folderMenuForId = null
      
      // 无论封面是否已有，都通过统一方法按“最新收藏视频”规则刷新封面
      this.updateFolderCover(folder.id)
    },
    
    closeEditFolderDialog () {
      this.editFolderDialog.visible = false
    },
    
    async confirmEditFolder () {
      const { name, description, isPublic } = this.editFolderDialog
      if (!name || !name.trim()) {
        ElMessage.warning('名称不能为空')
        return
      }
      if (name.trim().length > 20) {
        ElMessage.warning('名称最多20个字符')
        return
      }
      if (description && description.trim().length > 200) {
        ElMessage.warning('描述最多200个字符')
        return
      }
      
      try {
        if (this.editFolderDialog.mode === 'create') {
          const { data } = await createFavoriteFolder(
            this.currentUserId,
            name.trim(),
            isPublic,
            description.trim() || null
          )
          if (data.success) {
            ElMessage.success('创建成功')
            this.closeEditFolderDialog()
            await this.loadFolders()
          } else {
            ElMessage.warning(data.message || '创建失败')
          }
        } else {
          // edit mode
          const { updateFavoriteFolder } = await import('@/api/favoriteFolder')
          const { data } = await updateFavoriteFolder(
            this.currentUserId,
            this.editFolderDialog.folder.id,
            name.trim(),
            isPublic,
            description.trim() || null
          )
          if (data.success) {
            ElMessage.success('已更新')
            this.closeEditFolderDialog()
            await this.loadFolders()
          } else {
            ElMessage.warning(data.message || '更新失败')
          }
        }
      } catch (e) {
        console.error('操作失败:', e)
        ElMessage.error('操作失败')
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
          // 通知其他页面（如主页的收藏夹列表）同步更新
          window.dispatchEvent(new CustomEvent('favorite-folders-updated'))
          // 先本地更新左侧收藏夹列表，保证当前页面立即刷新
          this.folders = this.folders.filter(f => f.id !== folder.id)
          this.folderMenuForId = null
          // 清除被删除收藏夹的缓存
          this.clearFolderCache(folder.id)
          // 如果当前选中的是被删除的收藏夹，重置
          if (this.activeFolderId === folder.id) {
            this.activeFolderId = null
            this.videos = []
            this.total = 0
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
  min-height: 105vh;
  // 本页 TopHeader 需要覆盖在封面图上，因此不预留顶部空间
  padding-top: 0;
}

// 统一内容区域宽度，避免 header / main 各自写死不同的 width 导致不一致
.user-profile-container {
  width: 100%;
  max-width: 85%;
  min-width: 1200px;
  margin: 0 auto;
  padding: 0 8px;
}

.profile-header {
  position: relative;
  height: 210px;
  overflow: hidden;

  .profile-header-inner {
    position: relative;
    height: 100%;
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

  .avatar-wrap {
    position: relative;
    width: 64px;
    height: 64px;
    cursor: pointer;
    flex-shrink: 0;
  }

  .avatar-wrap-disabled {
    cursor: default !important;
    pointer-events: none;
  }

  .avatar {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    border: 3px solid #fff;
    object-fit: cover;
    background: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12);
    display: block;
    position: relative;
    z-index: 99999;
  }

  .avatar-input {
    display: none;
  }

  .avatar-mask {
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #fff;
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
      cursor: pointer;
    }

  .sub-row-disabled {
    cursor: default !important;
    pointer-events: none;
  }

    .bio-placeholder {
      opacity: 0.9;
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
      width: 22px;
      height: 22px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      transition: color 0.3s;
      svg {
        width: 22px;
        height: 22px;
      }

      // 主页 - 绿色
      &.home {
        color: #00d26a;
      }

      // 动态 - 粉色
      &.dynamics {
        color: #fb7299;
      }

      // 投稿 - 蓝色
      &.submit {
        color: #00a1d6;
      }

      // 收藏 - 黄色
      &.collections {
        color: #ffb02e;
      }
    }

    &.active .tab-icon {
      // 激活状态保持各自颜色，但可以稍微加深
      &.home {
        color: #00c05a;
      }

      &.dynamics {
        color: #fb5a8a;
      }

      &.submit {
        color: #0091c6;
      }

      &.collections {
        color: #ffa01e;
      }
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
      bottom: -15px;
      height: 2px;
      background: #00a1d6;
      border-radius: 2px;
    }
  }
}

.content-wrap {
  padding: 0px 0 36px;
}

.content-inner {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  padding: 0; // padding 由 .user-profile-container 统一控制

  // 当没有左侧面板时（如主页），右侧面板占满整个容器
  &.no-left-panel {
    grid-template-columns: 1fr;
  }
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
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.fav-cover {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f2f3;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .fav-cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ccc;
    background: #f5f7fa;
  }
}

.batch-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;

  .batch-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .select-all-label {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      font-size: 14px;
      color: #222;
      user-select: none;

      input[type="checkbox"] {
        width: 16px;
        height: 16px;
        cursor: pointer;
      }
    }

    .selected-count {
      font-size: 14px;
      color: #666;
    }
  }

  .batch-right {
    display: flex;
    align-items: center;
    gap: 12px;

    .batch-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 16px;
      border: 1px solid #e3e3e3;
      background: #fff;
      border-radius: 4px;
      font-size: 14px;
      color: #666;
      cursor: pointer;
      transition: all 0.2s;

      &:hover:not(:disabled) {
        border-color: #00a1d6;
        color: #00a1d6;
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &.delete-btn {
        color: #ff4d4f;
        
        &:hover:not(:disabled) {
          border-color: #ff4d4f;
          color: #ff4d4f;
        }
      }

      &.move-btn {
        &:hover:not(:disabled) {
          border-color: #00a1d6;
          color: #00a1d6;
        }
      }
    }
  }
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
  font-size: 14px;
  transition: all 0.2s;

  &:hover {
    border-color: #00a1d6;
    color: #00a1d6;
  }
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

  &.batch-mode {
    cursor: default;
  }

  .card-inner {
    position: relative;

    .batch-checkbox {
      position: absolute;
      top: 8px;
      left: 8px;
      z-index: 10;
      border-radius: 4px;
      padding: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;

      input[type="checkbox"] {
        cursor: pointer;
        width: 16px;
        height: 16px;
      }
    }
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
  margin-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 编辑收藏夹对话框 */
.edit-folder-dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.edit-folder-dialog {
  width: 420px;
  max-height: 85vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.24);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.edit-folder-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 0;
  border-bottom: none;
  flex-shrink: 0;
}

.edit-folder-title {
  font-size: 14px;
  font-weight: 600;
  color: #222;
}

.edit-folder-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #f5f7fa;
    color: #666;
  }
}

.edit-folder-body {
  padding: 8px 16px 16px;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  width: 100%;
  
  &::-webkit-scrollbar {
    display: none;
  }
  
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.edit-folder-cover {
  width: 100%;
  height: 160px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.edit-folder-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.edit-folder-field {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.edit-folder-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #222;
  margin-bottom: 8px;
  
  .required-star {
    color: #ff4757;
  }
}

.edit-folder-input-wrapper {
  position: relative;
}

.edit-folder-input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  padding-right: 60px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #222;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;

  &:focus {
    border-color: #00a1d6;
  }

  &::placeholder {
    color: #999;
  }
}

.edit-folder-char-count {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #999;
}

.edit-folder-switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.edit-folder-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  cursor: pointer;
}

.edit-folder-switch-input {
  opacity: 0;
  width: 0;
  height: 0;

  &:checked + .edit-folder-switch-slider {
    background-color: #00AEEC;

    &:before {
      transform: translateX(20px);
    }
  }
}

.edit-folder-switch-slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: 0.3s;
  border-radius: 24px;

  &:before {
    position: absolute;
    content: "";
    height: 18px;
    width: 18px;
    left: 3px;
    bottom: 3px;
    background-color: white;
    transition: 0.3s;
    border-radius: 50%;
  }
}

.edit-folder-switch-text {
  font-size: 14px;
  color: #666;
}

.edit-folder-textarea {
  width: 100%;
  min-height: 80px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  color: #222;
  outline: none;
  resize: vertical;
  font-family: inherit;
  transition: border-color 0.2s;
  box-sizing: border-box;

  &:focus {
    border-color: #00a1d6;
  }

  &::placeholder {
    color: #999;
  }
}

.edit-folder-char-count-right {
  text-align: right;
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}

.edit-folder-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0px 16px 12px;
  border-top: none;
  flex-shrink: 0;
}

.edit-folder-btn {
  flex: 1;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  &.cancel {
    background: #f5f7fa;
    color: #666;

    &:hover {
      background: #e5e7eb;
    }
  }

  &.confirm {
    background: #00AEEC;
    color: #fff;

    &:hover {
      background: #009dd9;
    }
  }
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

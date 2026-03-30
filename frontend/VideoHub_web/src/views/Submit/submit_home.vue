<template>
  <div class="submit-layout">
    <!-- 顶部标题栏（横跨整页） -->
    <div class="sidebar-header">
      <div class="header-left">
        <span class="site-logo" aria-hidden="true" v-html="webLogoSvg"></span>
        <span class="site-title">创作中心</span>

        <!-- 主站入口：紧贴“创作中心”右侧 -->
        <button class="main-site-btn" type="button" @click="goHome" title="主站">
          <svg
            t="1774885495121"
            class="main-site-icon"
            viewBox="0 0 1024 1024"
            version="1.1"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
          >
            <path
              d="M278.886 148.173c14.336-8.192 32.615-9.37 47.821-2.663 11.367 4.66 19.968 13.824 29.184 21.607 38.144 32.921 75.981 66.304 114.279 99.02h80.435c38.297-32.767 76.083-66.047 114.227-98.969 9.267-7.782 17.869-16.896 29.286-21.658 14.746-6.502 32.461-5.632 46.592 2.048 16.59 8.55 28.11 26.266 28.826 44.954 1.024 13.568-3.84 27.29-12.34 37.786-7.526 8.345-16.537 15.206-24.882 22.681-5.325 4.403-10.138 9.523-16.026 13.21 23.654 0 47.258-0.256 70.912 0.153 31.13 0.82 61.44 14.592 82.893 37.172 22.63 22.22 35.533 53.555 35.481 85.196 0.103 108.442 0 216.935 0.052 325.376-0.103 16.384 0.819 33.024-2.816 49.152-6.656 32.973-28.826 61.594-56.986 79.36a121.344 121.344 0 0 1-64.717 17.767H263.27c-16.998-0.103-34.252 0.87-50.995-2.867-32.102-6.452-60.006-27.648-77.824-54.63a121.088 121.088 0 0 1-19.251-66.97V414.362c0.102-16.538-0.922-33.178 2.458-49.408 10.24-52.941 58.93-96.103 112.998-98.458 24.627-0.768 49.306-0.205 73.933-0.307-11.623-8.397-21.811-18.535-32.768-27.75a55.04 55.04 0 0 1-20.531-45.927c0.716-18.227 11.673-35.584 27.648-44.34m-13.056 221.8c-20.992 3.737-38.912 20.326-44.75 40.754a76.493 76.493 0 0 0-2.354 21.71c0.102 89.036-0.052 178.073 0.05 267.16-0.409 24.218 16.334 47.156 39.118 54.887 8.14 2.97 16.896 3.021 25.395 3.072 153.19-0.102 306.432 0.051 459.622-0.051 22.477 0.87 44.083-13.107 53.555-33.28 5.786-11.571 5.684-24.73 5.479-37.376V438.016c0-9.114 0.307-18.483-2.304-27.29a58.778 58.778 0 0 0-36.864-38.656c-9.78-3.584-20.429-3.02-30.669-3.072H292.506c-8.858 0-17.818-0.307-26.624 0.973z"
              fill="currentColor"
              p-id="6191"
            ></path>
            <path
              d="M358.707 455.526c14.643-1.484 29.85 3.277 41.011 12.8 12.442 10.24 19.559 26.112 19.764 42.138 0.358 19.405 0.102 38.86 0.102 58.266 0 12.8-3.38 25.804-11.315 35.942a54.989 54.989 0 0 1-48.487 21.76 54.938 54.938 0 0 1-44.032-28.262c-6.81-11.674-7.372-25.498-7.168-38.605 0.41-18.842-1.024-37.786 0.87-56.576a55.296 55.296 0 0 1 49.255-47.463z m292.455 0a55.245 55.245 0 0 1 60.723 53.044c0.819 18.227 0.102 36.454 0.41 54.681 0.102 12.8-1.434 26.112-8.5 37.12-10.24 17.05-30.31 27.546-50.176 26.112a55.04 55.04 0 0 1-43.366-24.985c-7.936-11.776-9.472-26.266-9.114-40.09 0.359-18.74-0.665-37.478 0.615-56.166 1.843-25.6 23.91-47.514 49.408-49.664z"
              fill="currentColor"
              p-id="6192"
            ></path>
          </svg>
          <span class="main-site-text">主站</span>
        </button>
      </div>

      <div class="header-right">
        <div
          class="avatar-menu-wrap"
          @mouseenter="showAvatarMenu = true"
          @mouseleave="showAvatarMenu = false"
        >
          <div class="header-avatar" :title="displayName">
            <img v-if="userAvatar" :src="userAvatar" alt="avatar" />
            <span v-else class="avatar-fallback">{{ avatarText }}</span>
          </div>

          <div v-show="showAvatarMenu" class="avatar-menu">
            <button class="avatar-menu-item" type="button" @click="goProfile">
              <el-icon class="menu-left-icon"><User /></el-icon>
              <span>个人中心</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
            <button class="avatar-menu-item" type="button" @click="goSubmitManagement">
              <el-icon class="menu-left-icon"><VideoCamera /></el-icon>
              <span>投稿管理</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
            <div class="avatar-menu-divider"></div>
            <button class="avatar-menu-item logout" type="button" @click="doLogout">
              <el-icon class="menu-left-icon"><RefreshLeft /></el-icon>
              <span>退出登录</span>
              <el-icon class="arrow"><ArrowRight /></el-icon>
            </button>
          </div>
        </div>

        <div class="days-chip">在VideoHub星球的第 {{ creatorDays }} 天</div>

        <button class="msg-btn" type="button" @click="goMessages" aria-label="消息">
          <svg t="1774887669952" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="7472" width="18" height="18" aria-hidden="true">
            <path
              d="M938.666667 898.517333H85.333333c-47.061333 0-85.333333-38.272-85.333333-85.333333V218.666667c0-47.061333 38.272-85.333333 85.333333-85.333334h853.333334c47.061333 0 85.333333 38.272 85.333333 85.333334v594.517333c0 47.018667-38.272 85.333333-85.333333 85.333333z m0-85.333333v42.666667-42.666667zM85.333333 218.666667v594.517333h853.205334L938.666667 218.666667H85.333333z"
              fill="#757575"
              p-id="7473"
            />
            <path
              d="M916.821333 158.464a42.794667 42.794667 0 0 1 0 60.330667l-373.333333 373.376a42.794667 42.794667 0 0 1-60.330667 0 42.837333 42.837333 0 0 1 0-60.373334l373.333334-373.333333a42.794667 42.794667 0 0 1 60.330666 0z"
              fill="#757575"
              p-id="7474"
            />
            <path
              d="M107.178667 158.464a42.794667 42.794667 0 0 0 0 60.330667l373.333333 373.376a42.794667 42.794667 0 0 0 60.330667 0 42.922667 42.922667 0 0 0 0-60.373334l-373.333334-373.333333a42.794667 42.794667 0 0 0-60.330666 0z"
              fill="#757575"
              p-id="7475"
            />
          </svg>
        </button>
      </div>
    </div>

    <div class="submit-main">
      <!-- 左侧侧边栏 -->
      <el-aside width="220px" class="sidebar">
        <el-menu
        :default-active="currentView"
        class="sidebar-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item-group title="主要功能">
          <!-- 投稿：使用纯样式图标按钮，不再依赖图片资源 -->
          <el-menu-item index="submit" class="submit-menu-item" @click.stop.prevent>
            <div class="submit-entry" @click.stop="enterSubmitView">
              <span class="submit-icon" aria-hidden="true">
                <svg
                  t="1774079965106"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="10407"
                  width="20"
                  height="20"
                >
                  <path
                    d="M522.666667 586.666667a21.333333 21.333333 0 0 0 21.333333-21.333334l-0.042667-253.397333 112.938667 112.896 2.986667 2.474667a21.333333 21.333333 0 0 0 27.221333-2.474667l15.061333-15.061333a21.333333 21.333333 0 0 0 0-30.208l-152.448-152.448a53.333333 53.333333 0 0 0-75.434666 0L321.834667 379.562667a21.333333 21.333333 0 0 0 0 30.208l15.061333 15.061333 2.986667 2.474667a21.333333 21.333333 0 0 0 27.221333-2.474667l112.853333-112.896 0.042667 253.397333a21.333333 21.333333 0 0 0 17.493333 20.992l3.84 0.341334z m213.333333 181.333333c29.44 0 53.333333-23.893333 53.333333-53.333333V640a21.333333 21.333333 0 0 0-21.333333-21.333333h-21.333333a21.333333 21.333333 0 0 0-21.333334 21.333333v64H298.666667V640a21.333333 21.333333 0 0 0-21.333334-21.333333H256a21.333333 21.333333 0 0 0-21.333333 21.333333v74.666667c0 29.44 23.893333 53.333333 53.333333 53.333333h448z"
                    fill="currentColor"
                    p-id="10408"
                  ></path>
                </svg>
              </span>
              <span class="submit-text">投稿</span>
            </div>
          </el-menu-item>
          <el-menu-item index="home">
            <div class="menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M985.6 486.4l-448-448c-12.8-6.4-38.4-6.4-51.2 0l-448 448c-6.4 12.8-12.8 44.8 0 51.2 12.8 6.4 38.4 6.4 51.2 0L512 108.8l422.4 428.8c6.4 6.4 19.2 6.4 25.6 6.4 6.4 0 19.2 0 25.6-12.8 12.8-12.8 12.8-32 0-44.8zM832 620.8c-19.2 0-32 12.8-32 32V896h-576v-236.8c0-19.2-12.8-32-32-32s-32 12.8-32 25.6v275.2c0 19.2 12.8 32 32 32h640c19.2 0 32-12.8 32-25.6v-275.2c0-19.2-12.8-38.4-32-38.4z" fill="currentColor"></path>
                  <path d="M704 550.4c0-19.2-6.4-32-25.6-38.4-19.2-6.4-38.4 6.4-38.4 25.6-12.8 57.6-64 102.4-128 102.4s-115.2-38.4-128-102.4c-6.4-19.2-19.2-25.6-38.4-25.6-12.8 0-25.6 19.2-25.6 32C339.2 640 422.4 704 512 704s172.8-64 192-153.6z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">首页</span>
            </div>
          </el-menu-item>
        </el-menu-item-group>
        
        <el-menu-item-group title="内容管理">
          <el-menu-item index="contentManagement" class="todo-item">
            <div class="menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M800 672.96a96 96 0 1 1 0 192 96 96 0 0 1 0-192z m0 64a32 32 0 1 0 0 64 32 32 0 0 0 0-64z" fill="currentColor"></path>
                  <path d="M751.616 536.128a96 96 0 0 1 96.768 0l128.128 74.624a96 96 0 0 1 47.616 82.944v150.464a96 96 0 0 1-47.616 82.944l-128.128 74.688a96 96 0 0 1-96.768-0.064l-127.68-74.624a96 96 0 0 1-47.552-82.88v-150.592a96 96 0 0 1 47.552-82.88z m64.512 55.232a32 32 0 0 0-32.256 0l-127.68 74.688a32 32 0 0 0-15.808 27.584v150.592a32 32 0 0 0 15.808 27.584l127.68 74.624a32 32 0 0 0 32.256 0l128.128-74.624a32 32 0 0 0 15.872-27.648v-150.4a32 32 0 0 0-15.872-27.712z" fill="currentColor"></path>
                  <path d="M800 63.488a96 96 0 0 1 95.552 86.72l0.384 9.728v287.616h-64V158.72l-0.448-4.992A32 32 0 0 0 805.76 128l-5.76-0.512-575.232 0.064a32 32 0 0 0-31.488 26.24l-0.512 5.76v702.848a32 32 0 0 0 26.112 31.488l5.76 0.512L512.064 896l-0.256 64-287.552-1.6a96 96 0 0 1-95.168-86.784l-0.384-9.216V159.552A96 96 0 0 1 215.488 64l9.28-0.448L800 63.488z" fill="currentColor"></path>
                  <path d="M448 640v64H256.128v-64zM640 448v64H257.28V448zM766.912 256.96v64H256.128v-64z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">内容管理</span>
            </div>
          </el-menu-item>
          <el-menu-item index="dataCenter" class="todo-item">
            <div class="menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M818.7 223.9c12.9 0 23.8 10.9 23.8 23.8v400.9c0 12.9-10.9 23.8-23.8 23.8H209.3c-12.9 0-23.8-10.9-23.8-23.8V247.7c0-12.9 10.9-23.8 23.8-23.8h609.4m0-65.5H209.3c-49.1 0-89.3 40.2-89.3 89.3v400.9c0 49.1 40.2 89.3 89.3 89.3h609.3c49.1 0 89.3-40.2 89.3-89.3V247.7c0-49.1-40.1-89.3-89.2-89.3z" fill="currentColor"></path>
                  <path d="M309.8 544.2c-9.3 0-18.6-4-25.1-11.7-11.6-13.9-9.8-34.5 4-46.1L435 363.6c13.1-10.9 32.3-10.1 44.3 2.1l99.4 100.6 117.5-105.8c13.4-12.1 34.1-11 46.2 2.4 12.1 13.4 11 34.1-2.4 46.2L599.4 535.8c-13 11.7-32.9 11.1-45.2-1.3L454 433.2 330.8 536.6c-6.1 5.1-13.6 7.6-21 7.6zM807.7 887.4H220.3c-18.1 0-32.7-14.7-32.7-32.7s14.7-32.7 32.7-32.7h587.4c18.1 0 32.7 14.7 32.7 32.7s-14.6 32.7-32.7 32.7z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">数据中心</span>
            </div>
          </el-menu-item>
        </el-menu-item-group>
        <el-menu-item index="fans" class="todo-item">
            <div class="menu-item-inner">
              <span class="menu-icon" aria-hidden="true">
                <svg viewBox="0 0 1024 1024" width="20" height="20">
                  <path d="M290.4064 902.7584c-54.528-13.7728-97.6384-40.3456-132.4544-94.8224-7.0144-10.9568-8.96-19.7632-8.704-26.7776a45.4144 45.4144 0 0 1 8.8576-24.2688c13.1072-19.2512 39.936-39.4752 78.7456-57.9584 76.8-36.608 184.4224-58.9824 264.3456-58.9824 79.872 0 187.5456 22.3744 264.3456 58.9824 38.8096 18.432 65.6384 38.7072 78.7456 57.9584 6.144 9.1136 8.704 17.1008 8.9088 24.2176 0.2048 7.0656-1.7408 15.872-8.7552 26.8288-34.816 54.4768-77.9264 81.0496-132.4544 94.8224-56.8832 14.336-125.2352 14.5408-210.7904 14.5408-85.6064 0-153.9072-0.2048-210.7904-14.5408z m207.8208 78.5408h5.888c82.0224 0 158.1056 0 223.488-16.4864 68.608-17.3056 126.208-52.736 170.752-122.368 12.8-20.0704 19.456-41.3696 18.7904-63.1808a109.056 109.056 0 0 0-19.968-58.368c-22.528-33.1776-61.5424-59.4432-104.0896-79.7696-86.016-40.96-203.1104-65.1776-291.8912-65.1776-88.832 0-205.9264 24.2176-291.84 65.1776-42.5984 20.3264-81.6128 46.592-104.1408 79.7696a109.056 109.056 0 0 0-19.968 58.368c-0.6144 21.8112 5.9904 43.1104 18.7904 63.1296 44.544 69.6832 102.144 105.1136 170.7008 122.4192 65.4336 16.4864 141.5168 16.4864 223.488 16.4864z" fill="currentColor"></path>
                  <path d="M506.0096 554.7008c-137.7792 0-250.0096-114.7904-250.0096-256 0-131.4304 96-240.64 223.5904-254.72 2.0992-0.4096 8.0896-1.28 14.4896-1.28h190.72c46.08 0 83.2 37.5296 83.2 83.6096 0 26.0608-11.52 49.92-32 66.1504l-1.6896 1.28c14.4896 32.8704 21.76 68.7104 21.76 104.96 0 141.2096-112.2304 256-250.0608 256z m-20.48-446.72C391.2704 118.6304 320 200.5504 320 298.7008c0 105.8304 83.6096 192 186.0096 192s186.0608-86.1696 186.0608-192c0-33.6896-8.5504-66.56-24.7808-95.5904 0-0.4096-0.4096-0.8192-0.4096-1.28a31.9488 31.9488 0 0 1 7.2704-42.24l22.1696-17.4592a20.224 20.224 0 0 0 7.68-15.8208 19.456 19.456 0 0 0-19.2-19.6096l-199.2704 1.28z" fill="currentColor"></path>
                </svg>
              </span>
              <span class="menu-label">粉丝管理</span>
            </div>
          </el-menu-item>

        <!-- <el-menu-item-group title="创作成长">
          <el-menu-item index="growth" class="todo-item">
            <span>创作成长</span>
          </el-menu-item>
          <el-menu-item index="achievement" class="todo-item">
            <span>任务成就</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="promotion" class="todo-item">
            <span>必火推广</span>
            <el-tag size="small" type="danger" class="new-tag">NEW</el-tag>
          </el-menu-item>
          <el-menu-item index="academy" class="todo-item">
            <span>创作学院</span>
          </el-menu-item>
        </el-menu-item-group> -->
        
        <!-- <el-menu-item-group title="设置与规范">
          <el-menu-item index="rights" class="todo-item">
            <span>创作权益</span>
          </el-menu-item>
          <el-menu-item index="convention" class="todo-item">
            <span>社区公约</span>
          </el-menu-item>
          <el-menu-item index="settings" class="todo-item">
            <span>创作设置</span>
          </el-menu-item>
        </el-menu-item-group> -->
        </el-menu>
      </el-aside>

      <!-- 右侧内容区域 -->
      <el-main class="content-main">
      <!-- 投稿页面内容 -->
      <div v-if="currentView === 'submit'" class="content-page">
        <!-- 顶部标签导航 -->
        <el-tabs v-model="activeTab" class="submit-tabs">
          <el-tab-pane
            v-for="tab in tabs"
            :key="tab.key"
            :label="tab.label"
            :name="tab.key"
          />
        </el-tabs>

        <!-- 投稿流程：图1上传 -> 图2编辑 -->
        <el-card class="upload-card" shadow="never">
          <SubmitUploadStep
            v-if="submitStep === 'upload'"
            @uploaded="onUploaded"
          />
          <SubmitEditStep
            v-else
            :submission-id="submissionId"
            :video-file="uploadedVideoFile"
            :video-name="uploadedVideoName"
            :duration-seconds="uploadedDurationSeconds"
            :initial-draft="initialDraft"
            @back="goBackToUpload"
            @done="onEditDone"
            @created="onSubmissionCreated"
          />
        </el-card>

        <!-- 推广模块（仅上传页展示） -->
        <!-- <div v-if="submitStep === 'upload'" class="promo-list">
          <el-card class="promo-card" shadow="hover">
            <div class="promo-content">
              <div class="promo-text">
                <h3>哔哩哔哩投稿快捷方式</h3>
                <p>保存bilibili投稿入口到桌面，界面更简单，投稿更快捷</p>
              </div>
              <el-button type="primary" plain>立即安装</el-button>
            </div>
          </el-card>

          <el-card class="promo-card" shadow="hover">
            <div class="promo-content">
              <div class="promo-text">
                <h3>必剪桌面端</h3>
                <p>一键字幕，海量素材，全能剪辑，支持一键投稿</p>
              </div>
              <el-button type="primary" plain>立即下载</el-button>
            </div>
          </el-card>
        </div> -->

        <!-- 底部说明（仅上传页展示） -->
        <!-- <div v-if="submitStep === 'upload'" class="footer-note">
          <div class="links">
            <el-link type="primary" href="javascript:void(0)">选择本地视频</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">哔哩哔哩内容规范</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">哔哩哔哩账号公约</el-link>
          </div>
          <div class="tools">
            <span>创作工具：</span>
            <el-link type="primary" href="javascript:void(0)">小程序版</el-link>
            <el-divider direction="vertical" />
            <el-link type="primary" href="javascript:void(0)">PC版</el-link>
          </div>
        </div> -->
      </div>

      <!-- 内容管理页面内容 -->
      <div v-else-if="currentView === 'contentManagement'" class="content-page">
        <ContentManagement @continue-edit="openDraftForEdit" />
      </div>

      <!-- 数据中心页面内容 -->
      <div v-else-if="currentView === 'dataCenter'" class="content-page">
        <DataCenter />
      </div>

      <!-- 首页内容 -->
      <div v-else-if="currentView === 'home'" class="content-page">
        <CreatorHome @switch-view="onCreatorHomeSwitchView" />
      </div>

      <!-- 创作成长内容 -->
      <div v-else-if="currentView === 'growth'" class="content-page">
        <CreatorGrowth />
      </div>

      <!-- 粉丝管理内容 -->
      <div v-else-if="currentView === 'fans'" class="content-page">
        <FansManage />
      </div>

      <!-- 任务成就内容 -->
      <div v-else-if="currentView === 'achievement'" class="content-page">
        <Achievements />
      </div>

      <!-- 必火推广内容 -->
      <div v-else-if="currentView === 'promotion'" class="content-page">
        <Promotion />
      </div>

      <!-- 创作学院内容 -->
      <div v-else-if="currentView === 'academy'" class="content-page">
        <Academy />
      </div>

      <!-- 创作权益内容 -->
      <div v-else-if="currentView === 'rights'" class="content-page">
        <Rights />
      </div>

      <!-- 社区公约内容 -->
      <div v-else-if="currentView === 'convention'" class="content-page">
        <Convention />
      </div>

      <!-- 创作设置内容 -->
      <div v-else-if="currentView === 'settings'" class="content-page">
        <Settings />
      </div>

      <!-- 其他页面内容 -->
      <div v-else class="content-page">
        <h2>{{ getPageTitle(currentView) }}</h2>
        <p>这里将显示 {{ getPageTitle(currentView) }} 相关功能</p>
      </div>
      </el-main>
    </div>
  </div>
  
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { User, VideoCamera, RefreshLeft, ArrowRight } from '@element-plus/icons-vue'
import ContentManagement from './content_management.vue'
import DataCenter from './data_center.vue'
import CreatorHome from './creator_home.vue'
import CreatorGrowth from './creator_growth.vue'
import FansManage from './fans_manage.vue'
import Achievements from './achievements.vue'
import Promotion from './promotion.vue'
import Academy from './academy.vue'
import Rights from './rights.vue'
import Convention from './convention.vue'
import Settings from './settings.vue'
import SubmitUploadStep from './components/SubmitUploadStep.vue'
import SubmitEditStep from './components/SubmitEditStep.vue'
import { getVideoDraftDetail } from '@/api/video'
import { fetchMyProfile } from '@/api/userProfile'
import webLogoRaw from '@/../public/assets/webLogo.svg?raw'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const showAvatarMenu = ref(false)

const webLogoSvg = computed(() =>
  webLogoRaw
    .replace(/fill="#000"\b/gi, 'fill="currentColor"')
    .replace(/fill="#000000"\b/gi, 'fill="currentColor"')
    .replace(/<svg\b/i, '<svg style="width:100%;height:100%;display:block;"')
)

const user = computed(() => userStore.user || {})
const displayName = computed(() => user.value.username || user.value.loginAccount || '未登录')

const normalizeAvatarUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('data:')) return url
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

const userAvatar = computed(() => {
  const avatar = user.value.avatar || user.value.avatarUrl || ''
  return avatar ? normalizeAvatarUrl(String(avatar)) : ''
})

const avatarText = computed(() => String(displayName.value).trim().slice(0, 1).toUpperCase() || 'U')

const creatorDays = computed(() => {
  const ct = user.value?.createTime || user.value?.create_time
  if (!ct) return 1
  const t = typeof ct === 'number' ? ct : Date.parse(String(ct))
  if (!Number.isFinite(t)) return 1
  const delta = Date.now() - t
  const days = Math.floor(delta / 86400000) + 1
  return days > 0 ? days : 1
})

const syncProfileForCreatorHeader = async () => {
  try {
    const { data } = await fetchMyProfile()
    if (!data) return
    const profile = data.success ? data : data
    const current = user.value || {}
    userStore.setUser({
      ...current,
      id: profile.id || current.id,
      userId: profile.id || current.userId || current.id,
      username: profile.username || current.username,
      loginAccount: current.loginAccount || profile.account,
      avatar: profile.avatar || current.avatar || '',
      bio: profile.bio || current.bio || '',
      createTime: profile.createTime || current.createTime || current.create_time || ''
    })
  } catch {
    // ignore: do not block submit page
  }
}

const goHome = () => {
  // “主站”：跳转到首页（新开标签页，避免打断创作流程）
  window.open('/', '_blank', 'noopener,noreferrer')
}

const goMessages = () => {
  // 跳转到消息页面（客户端路由已存在 /messages）
  router.push('/messages')
}

const goProfile = () => {
  const uid = user.value?.id || user.value?.userId
  router.push(uid ? `/profile/${uid}` : '/profile')
  showAvatarMenu.value = false
}

const goSubmitManagement = () => {
  currentView.value = 'contentManagement'
  showAvatarMenu.value = false
}

const doLogout = async () => {
  await userStore.logout()
  showAvatarMenu.value = false
  router.push('/')
}

// 当前视图状态
const currentView = ref('submit')

// 根据 URL 参数设置初始视图
onMounted(() => {
  const viewParam = route.query.view
  if (viewParam && typeof viewParam === 'string') {
    currentView.value = viewParam
  }
  void syncProfileForCreatorHeader()
})

const tabs = [
  { key: 'video', label: '视频投稿' },
  // { key: 'short', label: '短剧投稿' },
  // { key: 'column', label: '专栏投稿' },
  // { key: 'interactive', label: '互动视频投稿' },
  // { key: 'music', label: '音频投稿' },
  // { key: 'sticker', label: '贴纸投稿' },
  // { key: 'material', label: '视频素材投稿' }
]

const activeTab = ref('video')

const handleMenuSelect = (index) => {
  // 投稿入口只允许点击蓝色 submit-entry 区域触发
  if (index === 'submit') return
  currentView.value = index
}

const enterSubmitView = () => {
  currentView.value = 'submit'
  submitStep.value = 'upload'
  submissionId.value = ''
  uploadedVideoFile.value = null
  uploadedVideoName.value = ''
  initialDraft.value = null
}

/** 创作中心首页快捷入口 */
const onCreatorHomeSwitchView = (view) => {
  if (!view || typeof view !== 'string') return
  if (view === 'submit') {
    enterSubmitView()
    return
  }
  currentView.value = view
}

// 投稿流程状态
const submitStep = ref('upload') // upload | edit
const submissionId = ref('')
const uploadedVideoFile = ref(null)
const uploadedVideoName = ref('')
const uploadedDurationSeconds = ref(0)
const initialDraft = ref(null)

const onUploaded = (payload) => {
  submissionId.value = payload?.submissionId || ''
  uploadedVideoFile.value = payload?.videoFile || null
  uploadedVideoName.value = payload?.videoName || ''
  uploadedDurationSeconds.value = payload?.durationSeconds || 0
  // 选择文件后就进入填写信息页；不自动上传
  submitStep.value = (uploadedVideoFile.value || submissionId.value) ? 'edit' : 'upload'
}

const goBackToUpload = () => {
  submitStep.value = 'upload'
  submissionId.value = ''
}

const onEditDone = () => {
  // 保存/投稿完成后回到上传页
  submitStep.value = 'upload'
  submissionId.value = ''
  uploadedVideoFile.value = null
  uploadedVideoName.value = ''
  uploadedDurationSeconds.value = 0
  initialDraft.value = null
}

const onSubmissionCreated = (sid) => {
  if (!sid || typeof sid !== 'string') return
  submissionId.value = sid
}

const openDraftForEdit = async (sid) => {
  if (!sid || typeof sid !== 'string') return
  currentView.value = 'submit'
  submitStep.value = 'edit'
  submissionId.value = sid
  uploadedVideoFile.value = null
  uploadedVideoName.value = ''
  initialDraft.value = null
  try {
    const { data } = await getVideoDraftDetail(sid)
    if (data?.success) {
      initialDraft.value = data.data || null
      uploadedVideoName.value = String(data.data?.title || '')
      const d = data.data?.duration
      uploadedDurationSeconds.value =
        d != null && Number(d) > 0 ? Math.floor(Number(d)) : 0
    }
  } catch (e) {
    // 忽略：草稿可能已不存在
    initialDraft.value = null
  }
}

// 支持从 URL 参数直接打开草稿编辑：/submitHome?view=submit&submissionId=xxx
onMounted(async () => {
  const sid = route.query.submissionId
  if (sid && typeof sid === 'string') {
    await openDraftForEdit(sid)
  }
})

// 获取页面标题
const getPageTitle = (view) => {
  const titleMap = {
    'submit': '投稿',
    'home': '首页',
    'contentManagement': '内容管理',
    'dataCenter': '数据中心',
    'growth': '创作成长',
    'fans': '粉丝管理',
    'achievement': '任务成就',
    'promotion': '必火推广',
    'academy': '创作学院',
    'rights': '创作权益',
    'convention': '社区公约',
    'settings': '创作设置'
  }
  return titleMap[view] || view
}
</script>

<style lang="scss" scoped>
.submit-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
  width: 200px;
}

.submit-main {
  display: flex;
  flex: 1;
  min-height: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
  color: #00a1d6;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 0 0 auto;
    margin-right: 0;
  }
  
  .site-logo {
    width: 90px;
    height: 45px;
    display: inline-flex;
    flex: 0 0 auto;
    color: currentColor;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    :deep(svg) {
      width: 100%;
      height: 100%;
      display: block;
    }

    /* 兜底：确保注入的 SVG 也跟随 currentColor */
    :deep(path) {
      fill: currentColor !important;
    }
  }
  
  .site-title {
    color: inherit;
    font-weight: 700;
    font-size: 20px;
    line-height: 32px;
  }
}

.main-site-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  color: #757575;
  cursor: pointer;
  padding: 0;
  border-radius: 8px;
  font-weight: 700;
  margin-left: 20px;
  height: 32px;
  line-height: 32px;
}

.main-site-icon {
  width: 20px;
  height: 20px;
  color: inherit;
  flex: 0 0 20px;
}

.main-site-text {
  font-size: 13px;
  line-height: 32px;
  color: #757575;
}

.days-chip {
  font-size: 12px;
  color: #fa8e57;
  background: rgba(250, 142, 87, 0.10); /* #FA8E571A */
  padding: 5px 12px 5px 16px;
  border-radius: 999px;
  margin: 0 32px 0 12px;
  border: 1px solid #fa8e57;
  box-sizing: border-box;
}

.msg-btn {
  width: 35px;
  height: 35px;
  border-radius: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #757575;
}

.msg-btn:hover {
  background: transparent;
  color: #757575;
}

.header-avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  overflow: hidden;
  background: #d1d5db;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #374151;
  font-weight: 700;
  font-size: 13px;
  user-select: none;
}

.header-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-fallback {
  line-height: 1;
}

.avatar-menu-wrap {
  position: relative;
  display: inline-flex;
}

.avatar-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 170px;
  background: #fff;
  border: 1px solid #eceef1;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  padding: 8px 0;
  z-index: 1200;
}

.avatar-menu-item {
  width: 100%;
  height: 46px;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  color: #2f3238;
  font-size: 16px;
  text-align: left;
  cursor: pointer;
}

.avatar-menu-item:hover {
  background: #f6f7f9;
}

.avatar-menu-item.logout {
  color: #2f3238;
}

.menu-left-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
  color: #61666d;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-menu-item .arrow {
  margin-left: auto;
  color: #c0c4cc;
  width: 14px;
  height: 14px;
  flex: 0 0 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.avatar-menu-divider {
  height: 1px;
  background: #eceef1;
  margin: 6px 0;
}

.sidebar-menu {
  border-right: none;
  
  :deep(.el-menu-item-group__title) {
    // 不显示分组标题（主要功能 / 内容管理 / 创作成长 / 设置与规范）
    display: none;
  }
  
  :deep(.el-menu-item) {
    height: 46px;
    line-height: 46px;
    color: #4b5563;
    
    &.is-active {
      background-color: transparent !important;
      color: #00a1d6;
    }
  }

  /* hover：仅背景变浅灰；字体颜色不变化（字体变蓝只在选中态） */
  :deep(.el-menu-item:hover) {
    background-color: #f6f6f8 !important;
  }

  /* 投稿按钮：hover 不显示灰底 */
  :deep(.el-menu-item.submit-menu-item:hover) {
    background-color: transparent !important;
  }
}

/* 左侧“投稿”按钮的图标样式，参考你提供的蓝色按钮效果 */
:deep(.submit-menu-item) {
  /* 单独的 li 间距：与下面普通按钮拉开一些距离 */
  margin: 24px 0;
  /* 整行禁用点击，避免白色区域触发 */
  pointer-events: none;
  cursor: default;
  /* hover 时父容器不要变色 */
  &:hover {
    background-color: transparent !important;
  }
  /* 清理默认高亮背景，交给内部按钮控制 */
  &.is-active {
    background-color: transparent;
  }

  .submit-entry {
    width: 80%;
    /* 只允许蓝色按钮区域可点击 */
    pointer-events: auto;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    height: 40px;
    margin: 0 auto;
    border-radius: 2px;
    background-color: #00a1d6;
    color: #fff;
    font-size: 15px;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }

  /* 投稿按钮：hover 时内部按钮也不变色 */
  &:hover .submit-entry {
    background-color: #00a1d6;
  }

  .submit-icon {
    width: 25px;
    height: 25px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;

    svg {
      width: 25px;
      height: 25px;
      display: block;
    }
  }

  .submit-text {
    letter-spacing: 2px;
  }

  /* 投稿按钮 hover 不变色：保留默认底色即可 */
}

.new-tag {
  margin-left: auto;
}

.menu-item-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 80%;
  margin: 0 auto;
}

.menu-icon {
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 20px;
    height: 20px;
    display: block;
  }
}

.menu-label {
  font-size: 14px;
}

/* 滚动只使用 App 根级 .scroll-container，避免与 el-main 叠加成双滚动条 */
.content-main.el-main {
  flex: 1;
  min-width: 0;
  padding: 16px 16px 24px;
  overflow: visible !important;
  background: #fafafa;
}

.content-page {
  max-width: 1100px;
  margin: 0 auto;
  background: #ffffff;
  min-height: 0;
}

.submit-tabs {
  :deep(.el-tabs__header) {
    height: 64px;
    margin: 0;
    padding: 0 35px;
    display: flex;
    align-items: center;
    border-bottom: 1.5px solid #eef2f7;
  }

  :deep(.el-tabs__nav-wrap) {
    height: 64px;
  }

  :deep(.el-tabs__nav-scroll) {
    height: 64px;
    display: flex;
    align-items: center;
  }

  /* 去掉 Tabs 底下的灰色分隔线（避免选中蓝色条出现“粗灰底”） */
  :deep(.el-tabs__nav-wrap)::after {
    background-color: transparent !important;
    border-bottom: none !important;
    height: 0 !important;
  }

  :deep(.el-tabs__nav-scroll)::after {
    background-color: transparent !important;
    border-bottom: none !important;
    height: 0 !important;
  }

  :deep(.el-tabs__nav) {
    height: 64px;
  }

  :deep(.el-tabs__item) {
    height: 64px;
    line-height: 64px;
    padding: 0 18px;
    font-size: 16px;
    color: #00a1d6;
    font-weight: 700;
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
  }
}

.upload-card {
  margin: 14px 0 16px;
}

:deep(.upload-card.el-card) {
  border: none !important;
  box-shadow: none !important;
}

::deep(.upload-card.el-card > .el-card__body) {
  padding: 10px 70px !important;
}

.upload-area {
  text-align: center;
  padding: 24px 40px 32px;
  
  .upload-title {
    margin: 8px 0 16px;
    color: #606266;
    font-size: 14px;
  }

  .file-hint {
    margin-top: 8px;
    color: #909399;
    font-size: 13px;
  }

  .cover-row {
    margin-top: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
  }

  .cover-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #606266;
  }

  .cover-label {
    font-weight: 500;
  }

  .cover-text {
    color: #909399;
  }

  .cover-preview {
    width: 120px;
    height: 68px;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .frame-picker {
    display: grid;
    grid-template-columns: 520px 1fr;
    gap: 16px;
    align-items: start;
  }

  .frame-cropper {
    width: 520px;
    height: 292px;
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    background-image:
      linear-gradient(45deg, rgba(0,0,0,0.04) 25%, transparent 25%),
      linear-gradient(-45deg, rgba(0,0,0,0.04) 25%, transparent 25%),
      linear-gradient(45deg, transparent 75%, rgba(0,0,0,0.04) 75%),
      linear-gradient(-45deg, transparent 75%, rgba(0,0,0,0.04) 75%);
    background-size: 16px 16px;
    background-position: 0 0, 0 8px, 8px -8px, -8px 0px;
    position: relative;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background-image: var(--frame-cropper-backdrop);
      background-repeat: no-repeat;
      background-position: center;
      background-size: contain;
      opacity: var(--frame-cropper-backdrop-opacity, 0);
      z-index: 0;
    }
  }

  .frame-cropper-core {
    width: 100%;
    height: 100%;
    background: transparent !important;
    position: relative;
    z-index: 1;
  }

  .frame-controls {
    min-width: 0;
  }

  .frame-preview {
    margin-bottom: 12px;
  }

  .frame-video {
    width: 100%;
    aspect-ratio: 16 / 9;
    border-radius: 10px;
    background: #000;
    display: block;
  }

  .frame-preview-hint {
    margin-top: 6px;
    color: #909399;
    font-size: 12px;
    text-align: left;
  }

  .frame-time {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #606266;
    font-size: 12px;
    margin-bottom: 10px;

    .sep {
      opacity: 0.6;
    }
  }

  .frame-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }

  .upload-form {
    margin-top: 24px;
    text-align: left;
  }

.tag-editor {
  width: 100%;
}

.tag-suggestions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag-suggestions__label {
  color: #909399;
  font-size: 12px;
  margin-right: 4px;
}

.schedule-row,
.collection-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.schedule-hint,
.collection-hint {
  color: #909399;
  font-size: 12px;
}

.schedule-picker,
.collection-editor {
  margin-top: 10px;
}
}

.promo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.promo-card {
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.promo-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.promo-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  
  &.quick {
    background: #e6f7ff;
    color: #409eff;
  }
  
  &.bijian {
    background: #ffeaea;
    color: #f56c6c;
  }
}

.promo-text {
  flex: 1;
  
  h3 {
    margin: 0 0 6px 0;
    font-size: 16px;
    color: #303133;
    font-weight: 500;
  }
  
  p {
    margin: 0;
    color: #606266;
    font-size: 13px;
    line-height: 1.4;
  }
}

.footer-note {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #909399;
  font-size: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  
  .links,
  .tools {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  :deep(.el-divider--vertical) {
    margin: 0 8px;
  }
}

@media (max-width: 768px) {
  .submit-layout {
    flex-direction: column;
  }
  
  .sidebar {
    height: auto;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .footer-note {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}
</style>


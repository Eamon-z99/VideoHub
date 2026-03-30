<template>
  <div class="home">
    <TopHeader />
    <header class="site-header">
      <img class="header-bg" src="/assets/header.png" alt="banner" />
    </header>
    <section class="navigation-section">
      <div class="nav-left-section">
        <div class="nav-left-top">
          <div class="quick-icons">
            <div class="qi-item" @click="goTo('/feed')">
              <div class="qi-circle orange">
                <img src="/assets/feed.png" class="qi-icon" />
              </div>
              <div class="qi-text">动态</div>
            </div>
            <div class="qi-item" @click="goTo('/hot')">
              <div class="qi-circle pink">
                <img src="/assets/trending.png" class="qi-icon" />
              </div>
              <div class="qi-text">热门</div>
            </div>
          </div>

          <div class="grid-container">
            <div
              v-for="c in videoNavTags"
              :key="c"
              class="grid-item"
              role="link"
              tabindex="0"
              @click="openTagFeedInNewTab(c)"
              @keydown.enter.prevent="openTagFeedInNewTab(c)"
            >
              {{ c }}
            </div>
          </div>
        </div>
      </div>
      
      <div class="nav-right-section">
        <div class="utility-links">
          <div class="utility-row">
            <div class="utility-item" @click="openPartitionFeedInNewTab('专栏')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M0 0L24 0L24 24L0 24L0 0z" fill="#F7F7F703" />
                <path d="M18 4.8L6 4.8C5.33726 4.8 4.8 5.33726 4.8 6L4.8 18C4.8 18.66276 5.33726 19.2 6 19.2L18 19.2C18.66276 19.2 19.2 18.66276 19.2 18L19.2 6C19.2 5.33726 18.66276 4.8 18 4.8z" fill="#61666Dff" />
                <path d="M4 5.99998C4 4.89541 4.89544 3.99997 6 3.99997L18 3.99997C19.1046 3.99997 20.00004 4.89541 20.00004 5.99998L20.00004 18C20.00004 19.1046 19.1046 19.99992 18 19.99992L6 19.99992C4.89544 19.99992 4 19.1046 4 18L4 5.99998zM6 5.59998C5.77909 5.59998 5.6 5.77907 5.6 5.99998L5.6 18C5.6 18.22092 5.77908 18.39996 6 18.39996L18 18.39996C18.22092 18.39996 18.39996 18.22092 18.39996 18L18.39996 5.99998C18.39996 5.77906 18.22092 5.59998 18 5.59998L6 5.59998z" fill="#61666Dff" />
                <path d="M15.99996 11.19931L8 11.19931C7.55818 11.19931 7.2 11.55749 7.2 11.99932C7.2 12.44112 7.55818 12.79932 8 12.79932L15.99996 12.79932C16.4418 12.79932 16.8 12.44112 16.8 11.99932C16.8 11.55749 16.4418 11.19931 15.99996 11.19931z" fill="#ffffffff" />
                <path d="M15.99996 8.00062L8 8.00062C7.55818 8.00062 7.2 8.35878 7.2 8.80061C7.2 9.24244 7.55818 9.60061 8 9.60061L15.99996 9.60061C16.4418 9.60061 16.8 9.24244 16.8 8.80061C16.8 8.35878 16.4418 8.00062 15.99996 8.00062z" fill="#ffffffff" />
                <path d="M11.2 14.4L8 14.4C7.55818 14.4 7.2 14.7582 7.2 15.20004C7.2 15.64188 7.55818 15.99996 8 15.99996L11.2 15.99996C11.64182 15.99996 12 15.64188 12 15.20004C12 14.7582 11.64182 14.4 11.2 14.4z" fill="#ffffffff" />
              </svg>
              <span class="utility-text">专栏</span>
            </div>
            <div class="utility-item" @click="openPartitionFeedInNewTab('活动')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M0.89371 0.57143L23.75086 0.57143L23.75086 23.42857L0.89371 23.42857L0.89371 0.57143z" fill="#F7F7F703" />
                <path d="M4.70841 5.19295C5.2937 5.03613 5.8953 5.38346 6.05213 5.96874L9.42514 18.55703C9.58198 19.14229 9.23464 19.74389 8.64935 19.90069C8.06406 20.0576 7.46246 19.71029 7.30563 19.12491L3.93262 6.53667C3.77578 5.95138 4.12312 5.34978 4.70841 5.19295z" fill="#61666Dff" />
                <path d="M5.27351 7.30093L10.54989 5.88712L13.46914 6.22925L16.10731 5.52234C16.52366 5.4108 16.94503 5.63347 17.04846 6.01973L18.99543 12.34011C19.09886 12.7264 18.84526 13.12994 18.42903 13.24149L15.79086 13.94834L12.87154 13.60629L7.59518 15.02011L5.27351 7.30093z" fill="#61666Dff" />
                <path d="M4.63459 6.94483C4.7309 6.77202 4.89311 6.64559 5.08421 6.59439L10.36057 5.18058C10.44997 5.15663 10.5431 5.14986 10.63502 5.16063L13.4152 5.48645L15.91806 4.81581C16.66903 4.61457 17.5232 5.00021 17.75086 5.81514L19.6944 12.1248C19.69703 12.13349 19.69954 12.14206 19.70183 12.15086C19.92309 12.97623 19.37406 13.74549 18.61829 13.948L15.98011 14.65486C15.89074 14.67886 15.7976 14.6856 15.70571 14.67486L12.92549 14.34903L7.78448 15.72651C7.40264 15.8288 7.00859 15.60926 6.89474 15.23074L4.57307 7.51155C4.51609 7.3221 4.53829 7.11765 4.63459 6.94483zM6.19111 7.81225L8.09123 14.12983L12.68229 12.89977C12.77166 12.87577 12.8648 12.86903 12.95669 12.87977L15.7368 13.2056L18.23966 12.53497C18.26217 12.52891 18.27554 12.51874 18.28274 12.51109L16.34949 6.23501C16.3488 6.23285 16.34811 6.23067 16.34754 6.22849C16.33783 6.2252 16.32046 6.22243 16.29669 6.22882L13.65851 6.93571C13.56903 6.95967 13.47589 6.96645 13.384 6.95567L10.60387 6.62985L6.19111 7.81225z" fill="#61666Dff" />
              </svg>
              <span class="utility-text">活动</span>
            </div>
            <div class="utility-item" @click="openPartitionFeedInNewTab('社区中心')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M19.11943 4.38093L8.53291 4.38093C8.11213 4.38093 7.77102 4.72205 7.77102 5.14283L7.77102 8.67166L7.77928 8.78424C7.83369 9.15165 8.15038 9.43355 8.53291 9.43355L8.65256 9.43312L11.26813 9.32185L11.27639 9.43445C11.33081 9.80184 11.64754 10.08376 12.03006 10.08376L12.18743 12.96229L13.28743 14.06251L13.38183 14.14286C13.67954 14.35714 14.09714 14.33029 14.36491 14.06251L15.464 12.96229L19.11943 12.9624C19.54023 12.9624 19.88137 12.62126 19.88137 12.20046L19.88137 5.14283C19.88137 4.72205 19.54023 4.38093 19.11943 4.38093zM18.35657 5.90398L18.35657 11.43769L15.14949 11.43857L15.0296 11.44807C14.87189 11.47319 14.72503 11.54742 14.61074 11.66173L13.82629 12.44571L13.0416 11.66173L12.95006 11.58366C12.8208 11.48994 12.66446 11.43857 12.50286 11.43857L12.79086 8.55906L10.17703 8.67166L10.16877 8.55906C10.11434 8.19167 9.79766 7.90975 9.41512 7.90975L9.2937 7.90969L9.2937 5.90398L18.35657 5.90398z" fill="#61666Dff" />
                <path d="M16.07166 7.42781C16.45417 7.42781 16.77086 7.70971 16.82526 8.07712L16.8336 8.1897L16.8336 17.33257C16.8336 17.71509 16.55166 18.03177 16.18423 18.08617L16.07166 18.0944L9.0857 18.09406L6.24303 20.22777C5.80353 20.55737 5.19983 20.32423 5.05576 19.84011L5.03216 19.73269L5.02398 19.61829L5.02285 18.09406L3.88113 18.0944C3.53685 18.0944 3.24591 17.86606 3.15149 17.55257L3.12749 17.44514L3.11922 17.33257L3.11922 8.1897C3.11922 7.80718 3.40114 7.49048 3.76854 7.43607L3.88113 7.42781L16.07166 7.42781zM8.45256 13.52377L6.16685 13.52377L6.05425 13.532C5.68686 13.58651 5.40494 13.9032 5.40494 14.28571C5.40494 14.70651 5.74606 15.04754 6.16685 15.04754L8.45256 15.04754L8.56514 15.03931C8.93255 14.98491 9.21446 14.66823 9.21446 14.28571C9.21446 13.86491 8.87335 13.52377 8.45256 13.52377zM10.73827 10.4769L6.16685 10.4769L6.05425 10.48517C5.68686 10.53958 5.40494 10.85627 5.40494 11.2388C5.40494 11.65959 5.74606 12.00069 6.16685 12.00069L10.73827 12.00069L10.85086 11.99245C11.21826 11.93802 11.50023 11.62134 11.50023 11.2388C11.50023 10.81801 11.15906 10.4769 10.73827 10.4769z" fill="#61666Dff" />
              </svg>
              <span class="utility-text">社区中心</span>
            </div>
          </div>
          <div class="utility-row">
            <div class="utility-item" @click="openPartitionFeedInNewTab('直播')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M0 0L24 0L24 24L0 24L0 0z" fill="#F7F7F703" />
                <path d="M3.99984 6.40068C3.99984 5.95885 4.35802 5.60069 4.79984 5.60069L15.99984 5.60069C16.44168 5.60069 16.79988 5.95885 16.79988 6.40068L16.79988 10.00068L19.9998 7.60068L19.9998 16.80072L16.79988 14.40072L16.79988 17.60064C16.79988 18.04248 16.44168 18.40068 15.99984 18.40068L4.79984 18.40068C4.35802 18.40068 3.99984 18.04248 3.99984 17.60064L3.99984 6.40068z" fill="#61666Dff" />
                <path d="M3.19985 6.40066C3.19985 5.517 3.91619 4.80066 4.79984 4.80066L15.99984 4.80066C16.88352 4.80066 17.5998 5.51701 17.5998 6.40066L17.5998 8.40066L19.5198 6.96066C19.7622 6.77885 20.08656 6.7496 20.35764 6.88512C20.6286 7.02064 20.79984 7.29764 20.79984 7.60066L20.79984 16.8006C20.79984 17.10372 20.6286 17.38068 20.35764 17.51616C20.08656 17.65176 19.7622 17.62248 19.5198 17.44068L17.5998 16.00068L17.5998 17.60064C17.5998 18.48432 16.88352 19.2006 15.99984 19.2006L4.79984 19.2006C3.9162 19.2006 3.19985 18.48432 3.19985 17.60064L3.19985 6.40066zM15.99984 6.40066L4.79984 6.40066L4.79984 17.60064L15.99984 17.60064L15.99984 14.4006C15.99984 14.0976 16.17108 13.82064 16.44204 13.68516C16.71312 13.54956 17.03748 13.57884 17.27988 13.76064L19.19988 15.20064L19.19988 9.20066L17.27988 10.64066C17.03748 10.82246 16.71312 10.85172 16.44204 10.7162C16.17108 10.58069 15.99984 10.30368 15.99984 10.00066L15.99984 6.40066z" fill="#61666Dff" />
                <path d="M12.63456 12.93084L9.47683 15.03612C9.10921 15.28116 8.61252 15.1818 8.36743 14.81424C8.27982 14.68272 8.23307 14.5284 8.23307 14.37048L8.23307 10.16006C8.23307 9.71824 8.59124 9.36006 9.03307 9.36006C9.19102 9.36006 9.34542 9.40681 9.47683 9.49442L12.63456 11.59961C13.00224 11.84468 13.1016 12.3414 12.85644 12.70896C12.79788 12.79692 12.72252 12.87228 12.63456 12.93084z" fill="#ffffffff" />
              </svg>
              <span class="utility-text">直播</span>
            </div>
            <div class="utility-item" @click="openPartitionFeedInNewTab('课堂')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M19.33943 4.80001L16.25371 4.80001C14.42514 4.80001 13.39657 5.94287 12.82514 6.85715C12.71086 7.08573 12.25371 7.08573 12.13943 6.85715C11.68229 6.05715 10.65371 4.80001 8.71086 4.80001L5.62514 4.80001C4.82514 4.80001 4.13943 5.48573 4.13943 6.28573L4.13943 16C4.13943 16.8 4.82514 17.48571 5.62514 17.48571L8.71086 17.48571C10.53943 17.48571 11.568 18.74286 12.13943 19.54286C12.25371 19.77143 12.71086 19.77143 12.82514 19.54286C13.28229 18.74286 14.31086 17.48571 16.25371 17.48571L19.33943 17.48571C20.13943 17.48571 20.82514 16.8 20.82514 16L20.82514 6.17144C20.82514 5.37144 20.25371 4.80001 19.33943 4.80001zM11.568 12.57143L10.31086 14.85714L9.05371 12.57143L6.768 11.3143L9.05371 10.05715L10.31086 7.77144L11.568 10.05715L13.85371 11.3143L11.568 12.57143zM16.368 14.28571L15.68229 15.54286L14.88229 14.28571L13.62514 13.6L14.88229 12.91429L15.568 11.65715L16.25371 12.91429L17.51086 13.6L16.368 14.28571z" fill="#61666Dff" />
              </svg>
              <span class="utility-text">课堂</span>
            </div>
            <div class="utility-item" @click="openPartitionFeedInNewTab('新歌热榜')">
              <svg class="utility-icon-svg" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
                <path d="M18.16229 5.52378C19.36731 5.52378 20.3544 6.45621 20.44171 7.63891L20.44789 7.8095L20.44789 16.95234C20.44789 18.15737 19.51554 19.14446 18.33291 19.23177L18.16229 19.23806L5.97181 19.23806C4.7668 19.23806 3.77961 18.3056 3.69235 17.12297L3.68609 16.95234L3.68609 7.8095C3.68609 6.60451 4.61851 5.61731 5.80121 5.53006L5.97181 5.52378L18.16229 5.52378zM14.68914 7.34128C14.24057 7.07799 13.64914 7.33809 13.55577 7.86781L12.34663 12.40594C11.96057 12.05474 11.47371 11.80491 10.92165 11.70758C9.36766 11.43357 7.88577 12.4712 7.61177 14.02514C7.33776 15.5792 8.37538 17.06103 9.92936 17.33509L10.07949 17.35749C11.57771 17.5416 12.97406 16.52126 13.2392 15.01749L14.84069 9.35157L15.90011 10.14427L15.97429 10.19341C16.30411 10.38517 16.73257 10.30351 16.96663 9.99065C17.21863 9.65373 17.14994 9.17624 16.81303 8.92416L14.76251 7.39006L14.68914 7.34128z" fill="#61666Dff" />
              </svg>
              <span class="utility-text">新歌热榜</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="hero-grid">
      <div class="banner">
        <div class="slider" :style="{ transform: `translateX(-${slideIndex * 100}%)` }">
          <div
            v-for="(s, i) in slides"
            :key="i"
            class="slide"
            @click="playTopVideo(s)"
          >
            <div class="thumb-wrap">
              <img
                :src="s.cover"
                :alt="s.title"
                :loading="i === slideIndex ? 'eager' : 'lazy'"
                decoding="async"
                @error="onImgError"
              />
              <!-- 轮播不再展示视频时长 -->
              <!-- <span class="duration">{{ s.duration }}</span> -->
              <div class="play-overlay">
                <div class="play-button">▶</div>
              </div>
            </div>
            <div class="slide-caption">{{ s.title }}</div>
          </div>
        </div>
        <button class="arrow left" @click="prev">‹</button>
        <button class="arrow right" @click="next">›</button>
        <div class="indicators">
          <span v-for="(s,i) in slides" :key="`dot-${i}`" class="dot" :class="{ active: i===slideIndex }" @click="go(i)" />
        </div>
      </div>
      <aside class="recommend">
        <div
          class="top-video"
          v-for="(r, i) in recommends"
          :key="r.id ?? i"
          @click="playTopVideo(r)"
        >
          <VideoCard :video="r" :on-img-error="onImgError" lazy-cover />
        </div>
      </aside>
    </section>

    <section class="section">
      <div ref="videoWrapperRef" class="video-virtual-wrapper">
        <ElVirtualGrid
          class="video-virtual-grid"
          :data="videos"
          :total-column="columnCount"
          :total-row="rowCount"
          :column-count="columnCount"
          :column-width="columnWidth"
          :row-height="rowHeight"
          :height="gridHeight"
          :width="gridWidth"
        >
          <template #default="{ data, rowIndex, columnIndex, style }">
            <div :style="[style, getPaddingStyle(columnIndex)]" class="video">
              <div
                v-if="data && data[rowIndex * columnCount + columnIndex]"
                class="card"
                @click="playVideo(data[rowIndex * columnCount + columnIndex])"
              >
                <VideoCard
                  :video="data[rowIndex * columnCount + columnIndex]"
                  :on-img-error="onImgError"
                  lazy-cover
                />
              </div>
            </div>
          </template>
        </ElVirtualGrid>
      </div>
      <div 
        ref="loadMoreTrigger" 
        class="loading-bar"
      >
        <span v-if="loadingMore">加载中...</span>
        <span v-else-if="finished">已加载全部</span>
        <span v-else-if="loadingVideos">加载中...</span>
        <span v-else style="visibility: hidden;">加载更多</span>
      </div>
    </section>

    <!-- 悬浮：底「顶部」→「换一换」→「稍后再看」 -->
    <BackToTop :anchor="videoWrapperRef" />
    <Refresh
      floating-only
      :anchor="videoWrapperRef"
      :busy="loadingVideos"
      :floating-bottom-offset-px="FAB_ABOVE_BACK_TO_TOP_PX"
      @swap="onRefreshVideos"
    />
    <WatchLaterFab
      :anchor="videoWrapperRef"
      :bottom-offset-px="WATCH_LATER_BOTTOM_OFFSET_PX"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
// @ts-ignore Element Plus 未在主导出暴露虚拟列表，子路径命名导出
import { FixedSizeGrid as ElVirtualGrid } from 'element-plus/es/components/virtual-list/index.mjs'
import { useRouter } from 'vue-router'
import TopHeader from '@/components/TopHeader.vue'
import VideoCard from '@/components/VideoCard.vue'
import Refresh from '@/components/Refresh.vue'
import BackToTop from '@/components/BackToTop.vue'
import WatchLaterFab from '@/components/WatchLaterFab.vue'
import { fetchVideos, fetchHomeHeroVideos, searchVideos } from '@/api/video'
import { useUserStore } from '@/stores/user'
import { useWatchLaterStore } from '@/stores/watchLater'
import { VIDEO_NAV_TAGS } from '@/constants/videoNavTags'

const videoNavTags = VIDEO_NAV_TAGS

/** 「换一换」在「顶部」之上：≈ 顶部按钮高度 + 与顶部的间距 */
const FAB_ABOVE_BACK_TO_TOP_PX = 60
/** 「稍后再看」在「换一换」之上（换一换块高度 + 间距的估计值） */
const REFRESH_FAB_HEIGHT_WITH_GAP_PX = 100
const WATCH_LATER_BOTTOM_OFFSET_PX = FAB_ABOVE_BACK_TO_TOP_PX + REFRESH_FAB_HEIGHT_WITH_GAP_PX

const userStore = useUserStore()
const watchLaterStore = useWatchLaterStore()

const router = useRouter()

const slides = ref<any[]>([])
const slideIndex = ref(0)
let timer: any
const next = () => {
  if (!slides.value.length) return
  slideIndex.value = (slideIndex.value + 1) % slides.value.length
}
const prev = () => {
  if (!slides.value.length) return
  slideIndex.value = (slideIndex.value - 1 + slides.value.length) % slides.value.length
}
const go = (i: number) => {
  if (!slides.value.length) return
  const idx = Number(i)
  if (Number.isNaN(idx)) return
  slideIndex.value = Math.max(0, Math.min(idx, slides.value.length - 1))
}

const recommends = ref<any[]>([])

const videos = ref<any[]>([])
const loadingVideos = ref(false)
const loadingMore = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = 40
const totalCount = ref(0)
// 搜索关键字
const searchKeyword = ref<string>('')
// 响应式窗口宽度
const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1350)
// 视频容器ref，用于获取实际宽度
const videoWrapperRef = ref<HTMLElement | null>(null)
// 实际容器宽度
const actualContainerWidth = ref(1350)

// 响应式计算容器宽度：小屏幕1350px，大屏幕75%宽度
const containerWidth = computed(() => {
  const screenWidth = windowWidth.value
  if (screenWidth >= 1800) {
    // 大屏幕：75%宽度，但不超过2300px（与.home的max-width一致）
    return Math.min(Math.floor(screenWidth * 0.75), 2300)
  }
  return 1350
})

// 更新实际容器宽度
const updateActualWidth = () => {
  if (videoWrapperRef.value) {
    const rect = videoWrapperRef.value.getBoundingClientRect()
    actualContainerWidth.value = Math.floor(rect.width)
  }
}

// 网格可用宽度 = 实际容器宽度（已经减去了padding）
const gridWidth = computed(() => {
  // 使用实际测量的宽度，如果没有则使用计算值
  const width = actualContainerWidth.value > 0 ? actualContainerWidth.value : (containerWidth.value - 40)
  return width
})

// 响应式计算列数：小屏幕5列，大屏幕根据宽度动态计算
// 每列约270px（包含.video的左右padding各8px），但至少5列
const columnCount = computed(() => {
  const width = gridWidth.value
  // 每列约270px，但至少5列
  const cols = Math.max(5, Math.floor(width / 270))
  return cols
})

// 列宽 = 网格宽度 / 列数（每个.video有左右各8px padding，已包含在计算中）
const columnWidth = computed(() => {
  const width = gridWidth.value
  const cols = columnCount.value
  // 确保列宽是整数，并且能够整除
  return Math.floor(width / cols)
})
const rowHeight = 230
// 计算已加载视频的行数（只显示已加载的数据）
const rowCount = computed(() => Math.ceil(videos.value.length / columnCount.value))
// 虚拟滚动的高度：基于已加载的行数，最小高度为1行
const gridHeight = computed(() => {
  const rows = rowCount.value
  return rows > 0 ? rows * rowHeight : rowHeight
})
const fallbackCover = '/images/banner-1.jpg'
const getPaddingStyle = (columnIndex: number) => {
  return {
    paddingLeft: columnIndex === 0 ? '0px' : undefined,
    paddingRight: columnIndex === columnCount.value - 1 ? '0px' : undefined
  }
}

const onImgError = (evt: Event) => {
  const target = evt?.target as HTMLImageElement | null
  if (!target) return
  if ((target as any).__fallbackApplied) return
  ;(target as any).__fallbackApplied = true
  target.onerror = null
  target.src = fallbackCover
}

const formatDuration = (seconds?: number) => {
  if (!seconds || seconds <= 0) return '--:--'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/** 与虚拟列表卡片一致的字段，供首页推荐区 VideoCard 复用 */
const normalizeVideoItem = (item: any) => {
  const rawCover = (item?.coverUrl || '').trim()
  const safeCover = rawCover || fallbackCover
  const durationText = formatDuration(item?.duration)
  return {
    ...item,
    cover: safeCover,
    duration: durationText,
    playCount: typeof item?.viewCount === 'number' ? item.viewCount : '本地视频',
    up: item?.sourceFile || '本地文件',
    playUrl: item?.playUrl || '',
    id: item?.videoId || item?.id,
    isVideo: !!item?.playUrl
  }
}

const normalizeList = (data: any) => {
  const list = Array.isArray(data?.list) ? data.list : (Array.isArray(data) ? data : [])
  return list.map((item: any) => normalizeVideoItem(item))
}

// hero-grid：左侧轮播 slides 优先读取管理端配置；无配置时后端自动回退随机
const loadHeroConfiguredVideos = async () => {
  try {
    const { data } = await fetchHomeHeroVideos(6)
    const list = Array.isArray(data?.list) ? data.list : []
    const mapped = list.map((item: any) => {
      const v = normalizeVideoItem(item)
      return {
        ...v,
        title: item?.title || v.title || '本地视频'
      }
    })
    slides.value = mapped
    slideIndex.value = 0
  } catch (e) {
    // 失败时保持默认空状态，主列表仍可正常加载
  }
}

// hero-grid：右侧推荐 recommends 保持随机展示（与 slides 不需要一致）
const loadRandomRecommends = async () => {
  try {
    const { data } = await fetchVideos(1, 6)
    const list = Array.isArray(data?.list) ? data.list : []
    recommends.value = list.map((item: any) => normalizeVideoItem(item))
  } catch {
    // ignore
  }
}

const fetchVideosData = async (reset = false) => {
  if (loadingVideos.value || loadingMore.value) return
  if (reset) {
    page.value = 1
    finished.value = false
    videos.value = []
    totalCount.value = 0
  }
  const isFirstPage = page.value === 1
  if (isFirstPage) loadingVideos.value = true
  else loadingMore.value = true
  try {
    let data
    const keyword = searchKeyword.value?.trim()
    if (keyword) {
      // 使用 JSON 请求体进行搜索
      const resp = await searchVideos({
        page: page.value,
        pageSize,
        keyword
      })
      data = resp.data
    } else {
      const resp = await fetchVideos(page.value, pageSize)
      data = resp.data
    }
    const mapped = normalizeList(data)
    videos.value = [...videos.value, ...mapped]
    const total = typeof data?.total === 'number' ? data.total : undefined
    if (typeof total === 'number') {
      totalCount.value = total
    }
    if ((total && videos.value.length >= total) || mapped.length < pageSize) {
      finished.value = true
    } else {
      page.value += 1
    }
  } catch (e) {
    if (isFirstPage) videos.value = []
  } finally {
    loadingVideos.value = false
    loadingMore.value = false
  }
}

// 使用 Intersection Observer 检测底部元素
const loadMoreTrigger = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | null = null

const setupIntersectionObserver = () => {
  if (!loadMoreTrigger.value) return
  
  // 清理旧的 observer
  if (observer) {
    observer.disconnect()
  }
  
  // 创建新的 observer
  observer = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]
      // 当底部元素进入视口时，加载更多
      if (entry.isIntersecting && !loadingVideos.value && !loadingMore.value && !finished.value) {
        fetchVideosData()
      }
    },
    {
      root: null, // 使用视口作为根
      rootMargin: '100px', // 提前100px触发
      threshold: 0.1
    }
  )
  
  observer.observe(loadMoreTrigger.value)
}

// 监听 videos 变化，重新设置 observer
watch(
  () => videos.value.length,
  () => {
    if (!finished.value) {
      nextTick(() => {
        setupIntersectionObserver()
        updateActualWidth()
      })
    }
  }
)

// 监听容器宽度变化，更新实际宽度
watch(
  () => containerWidth.value,
  () => {
    nextTick(() => {
      updateActualWidth()
    })
  }
)

// 监听 finished 状态，如果已完成则停止观察
watch(
  () => finished.value,
  (isFinished) => {
    if (isFinished && observer) {
      observer.disconnect()
      observer = null
    } else if (!isFinished) {
      nextTick(() => {
        setupIntersectionObserver()
      })
    }
  }
)

// 窗口大小变化处理函数
let handleResize: (() => void) | null = null

onMounted(() => {
  timer = setInterval(next, 4000)
  fetchVideosData()
  loadHeroConfiguredVideos()
  loadRandomRecommends()
  const uid = userStore.user?.userId || userStore.user?.id
  if (uid) {
    watchLaterStore.load(uid as number)
  }
  // 等待 DOM 渲染后设置 observer
  nextTick(() => {
    setupIntersectionObserver()
    // 更新实际容器宽度
    updateActualWidth()
  })
  // 监听窗口大小变化
  handleResize = () => {
    windowWidth.value = window.innerWidth
    // 延迟更新，等待DOM更新完成
    nextTick(() => {
      updateActualWidth()
    })
  }
  window.addEventListener('resize', handleResize)
})

// 监听全局搜索事件（来自 TopHeader）
const handleGlobalSearch = (evt: Event) => {
  // 自定义事件携带 { keyword }
  const anyEvent = evt as CustomEvent<{ keyword?: string }>
  const kw = (anyEvent.detail?.keyword || '').trim()
  searchKeyword.value = kw
  fetchVideosData(true)
}

if (typeof window !== 'undefined') {
  window.addEventListener('global-search', handleGlobalSearch as EventListener)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (observer) {
    observer.disconnect()
    observer = null
  }
  if (handleResize) {
    window.removeEventListener('resize', handleResize)
    handleResize = null
  }
  if (typeof window !== 'undefined') {
    window.removeEventListener('global-search', handleGlobalSearch as EventListener)
  }
})

/** 与 WatchLaterFab 一致：微应用用 __MICRO_APP_BASE_ROUTE__，独立运行用 Vite BASE_URL（否则生产 /videohub/ 下会打开错误路径） */
function resolvePublicAppBase(): string {
  const micro = (window as any).__MICRO_APP_BASE_ROUTE__ || ''
  if (micro) return String(micro).replace(/\/$/, '')
  return String(import.meta.env.BASE_URL || '/').replace(/\/$/, '')
}

const openInNewTab = (path: string) => {
  const base = resolvePublicAppBase()
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  const url = `${base}${normalizedPath}`
  // 第三个参数含 noopener 时，window.open 在多数浏览器中固定返回 null，不能用「!w」判断拦截后改 location，否则当前页也会被跳转
  window.open(url, '_blank', 'noopener,noreferrer')
}

const openTagFeedInNewTab = (tag: string) => {
  const t = String(tag || '').trim()
  if (!t) return
  openInNewTab(`/tag/${encodeURIComponent(t)}`)
}

const openPartitionFeedInNewTab = (label: string) => {
  const t = String(label || '').trim()
  if (!t) return
  openInNewTab(`/partition/${encodeURIComponent(t)}`)
}

const playTopVideo = (video: any) => {
  if (!video || !video.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}

// 导航到其他页面
const goTo = (path: string) => { 
  openInNewTab(path) 
}

const navigateToCreatorCenter = () => {
  openInNewTab('/submitHome?view=contentManagement')
}

// 播放视频（列表区域）
const playVideo = (video: any) => {
  if (!video || !video.id) return
  openInNewTab(`/video/${encodeURIComponent(video.id)}`)
}

/** 换一换：重新拉取首页列表与顶部推荐 */
const onRefreshVideos = () => {
  fetchVideosData(true)
  loadHeroConfiguredVideos()
  loadRandomRecommends()
}
</script>

<style lang="scss" scoped>
.home {
  position: relative;
  background: #fff;
  min-width: 1600px;
  max-width: 2300px;
  width: 100%;
  margin: 0 auto;
}

.site-header {
  position: relative;
  height: 156px;

  .header-bg {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 156px;
    object-fit: cover;
  }
}

.navigation-section {
  max-width: 1350px;
  width: 100%;
  margin: -30px auto 0;
  padding: 0 20px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-top: 20px;
  overflow-y: hidden;

  .nav-left-section {
    display: flex;
    flex-direction: column;
    gap: 10px;
    flex: 1;
    overflow-y: hidden;

    .nav-left-top {
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .quick-icons {
      margin: 0;
      margin-left: 0px;
      margin-right: 20px;
      display: flex;
      gap: 20px;

      .qi-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 6px;

        .qi-circle {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;

          &.orange {
            background: #ff9212;
          }

          &.pink {
            background: #f07775;
          }

          .qi-icon {
            width: 23px;
            height: 23px;
            filter: brightness(0) invert(1);
          }
        }

        .qi-text {
          font-size: 14px;
          color: #222;
        }
      }
    }

    .grid-container {
      display: grid;
      grid-template-columns: repeat(11, 1fr);
      grid-template-rows: repeat(2, 1fr);
      gap: 8px;
      width: 100%;

      .grid-item {
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: #F6F7F8;
        border-radius: 8px;
        padding: 8px 12px;
        color: #61666D;
        font-size: 14px;
        letter-spacing: 2px;
        transition: color 0.3s;
        height: 15px;
        white-space: nowrap;
        box-sizing: content-box;
        cursor: pointer;
        &:hover {
            color: #000000;
        }
        &:focus-visible {
          outline: 2px solid #00a1d6;
          outline-offset: 2px;
        }
      }
    }
  }

  .nav-right-section {
    flex-shrink: 0;
    padding-left: 10px;
    border-left: 1.5px solid #eee;
    overflow-y: hidden;

    .utility-links {
      display: flex;
      flex-direction: column;
      gap: 8px;
      margin-top:3px;

      .utility-row {
        display: flex;
        gap: 12px;

        .utility-item {
          display: flex;
          flex-direction: row;
          align-items: center;
          gap: 6px;
          cursor: pointer;
          padding: 4px 8px;
          border-radius: 6px;
          transition: color 0.2s;

          &:hover {
            background-color: transparent;

            .utility-text {
              color: #00a1d6;
            }
          }

          .utility-icon-svg {
            width: 20px;
            height: 20px;
            flex-shrink: 0;
            display: block;
          }

          .utility-text {
            font-size: 14px;
            color: #61666d;
            white-space: nowrap;
            transition: color 0.2s;
          }
        }
      }
    }
  }
}

/* 顶部区域：5列网格，轮播占两列两行 */
.hero-grid {
  max-width: 1350px;
  width: 100%;
  margin: 20px auto 20px;
  // padding: 0 20px;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  /* 每个卡片的行高自适应内容 */
  grid-auto-rows: auto;
  gap: 16px;
  overflow-y: hidden;
  /* 防止子项内容撑破导致列宽不一致 */
  > * { min-width: 0; }

    .banner {
    position: relative;
    background: linear-gradient(135deg, #2b2b3a, #5b6bd5);
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .08);
    grid-column: 1 / span 2;  /* 占两列 */
    grid-row: 1 / span 3;
    width: 100%;
    height: 85%;

      .slider {
      position: absolute;
      inset: 0;
      display: flex;
      transition: transform .45s ease;
      z-index: 0; // 图片层

          .slide {
          min-width: 100%;
          position: relative;
          cursor: pointer;
          overflow: hidden;

          // 每一张幻灯片底部的渐变遮罩（在图片之上，标题之下）
          &::after {
            content: "";
            position: absolute;
            left: 0;
            right: 0;
            bottom: 0;
            height: 90px;
            background: linear-gradient(
              180deg,
              rgba(0, 0, 0, 0) 0%,
              rgba(0, 0, 0, 0.55) 60%,
              rgba(0, 0, 0, 0.8) 100%
            );
            pointer-events: none;
            z-index: 1;
          }

          .thumb-wrap {
            position: relative;
            width: 100%;
            height: 100%;
            border-radius: 8px;
            overflow: hidden;
            background: #f1f2f3;

            img {
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
              background: rgba(0, 0, 0, .55);
              padding: 2px 6px;
              border-radius: 4px;
              z-index: 2;
            }

            .play-overlay {
              position: absolute;
              inset: 0;
              display: flex;
              align-items: center;
              justify-content: center;
              background: rgba(0, 0, 0, 0.3);
              opacity: 0;
              pointer-events: none;
              transition: opacity 0.3s ease;
              z-index: 1;

              .play-button {
                width: 50px;
                height: 50px;
                background: rgba(255, 255, 255, 0.9);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 20px;
                color: #333;
                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
              }
            }

          }

          .slide-caption {
            position: absolute;
            left: 16px;
            bottom: 40px; // 提高一些，给圆点留出更大间距
            // 标题：18px、单行、不换行、超出省略号
            font-size: 18px;
            font-weight: 600;
            color: #fff;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: calc(100% - 160px); // 预留右下角箭头按钮区域
            z-index: 2; // 在渐变之上（slide::after 为 1）
          }
        }
    }

    .arrow {
      position: absolute;
      bottom: 40px; // 与标题同行
      transform: none;
      width: 28px;
      height: 28px;
      border-radius: 4px;
      border: 0;
      background: rgba(0, 0, 0, .55);
      color: #fff;
      cursor: pointer;
      z-index: 3; // 高于渐变（1）和图片（0）

      &.left {
        right: 56px;
      }

      &.right {
        right: 16px;
      }
    }

    .indicators {
      position: absolute;
      left: 16px;
      bottom: 24px; // 标题 bottom: 40px，与标题垂直间距约 16px，更宽松
      display: flex;
      justify-content: flex-start;
      gap: 6px;
      z-index: 3;

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: rgba(255, 255, 255, .5);
        cursor: pointer;

        &.active {
          background: #fff;
        }
      }
    }
  }

  .recommend {
    display: contents; /* 将推荐卡片直接放入网格，让其占据网格单元 */

    .top-video {
      min-width: 0;
      min-height: 0;
      cursor: pointer;

      :deep(.video-card) {
        width: 100%;
      }
    }
  }
}

.section {
  max-width: 1350px;
  width: 100%;
  margin: 55px auto 40px;
  // padding: 0 20px;
  box-sizing: border-box;

  .video-virtual-wrapper {
    width: 100%;
    margin: 0;
    padding: 0;
    display: flex;
    justify-content: flex-start;
    overflow: visible; /* 交给页面滚动 */
    box-sizing: border-box;
  }

  .video-virtual-grid {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    overflow: hidden !important; /* 禁止独立滚动，统一跟随页面 */
    overflow-y: hidden !important;
    overflow-x: hidden !important;
    /* 高度由组件动态计算，不强制设置 */
    scrollbar-width: none;
    -ms-overflow-style: none;

    &::-webkit-scrollbar {
      display: none;
      width: 0;
      height: 0;
    }

    /* 组件内部类名被作用域隔离，使用 :deep 隐藏所有虚拟滚动条 */
    :deep(.el-virtual-scrollbar),
    :deep(.el-virtual-scrollbar__thumb),
    :deep(.el-virtual-scrollbar__bar) {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
      opacity: 0 !important;
    }

    :deep(.el-vl__scrollbar) {
      display: none !important;
      width: 0 !important;
      height: 0 !important;
      opacity: 0 !important;
    }

    :deep(.el-vl__window),
    :deep(.el-vl__wrapper) {
      scrollbar-width: none;
      -ms-overflow-style: none;
      overflow: visible !important; /* 交给外层页面滚动 */
      height: auto !important;
      max-height: none !important;
      position: static !important; /* 避免内部定位影响整体高度 */
      overflow-y: visible !important;
      overflow-x: visible !important;
    }

    /* 部分版本还会在 wrap 上强制 overflow:auto，这里兜底 */
    :deep(.el-virtual-scrollbar__wrap),
    :deep(.el-virtual-scrollbar__wrap--horizontal) {
      overflow: visible !important;
      height: auto !important;
      max-height: none !important;
      overflow-y: visible !important;
      overflow-x: visible !important;
    }

    /* 兜底处理 el-virtual-list 自身可能的 overflow 设置 */
    :deep(.el-virtual-list),
    :deep(.el-virtual-list__window),
    :deep(.el-virtual-list__wrapper) {
      overflow: visible !important;
      overflow-y: visible !important;
      overflow-x: visible !important;
      height: auto !important;
      max-height: none !important;
      position: static !important;
    }

    /* 彻底禁用子容器滚动条 */
    :deep(.el-vl__window::-webkit-scrollbar),
    :deep(.el-vl__wrapper::-webkit-scrollbar) {
      display: none;
      width: 0;
      height: 0;
    }

    :deep(.el-vl__window::-webkit-scrollbar),
    :deep(.el-vl__wrapper::-webkit-scrollbar) {
      display: none;
      width: 0;
      height: 0;
    }
  }

  .video {
    padding: 8px;
    box-sizing: border-box;
    width: 100%;
    overflow: hidden;

    .card {
      display: grid;
      grid-template-rows: auto auto auto;
      gap: 6px;
      cursor: pointer;
    }

    .thumb-wrap {
      position: relative;
      width: 100%;
      padding-bottom: 56%;
      border-radius: 8px;
      overflow: hidden;
      background: #f1f2f3;

      img {
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
        background: rgba(0, 0, 0, .55);
        padding: 2px 6px;
        border-radius: 4px;
        z-index: 2;
      }

      .play-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(0, 0, 0, 0.3);
        opacity: 0;
        pointer-events: none;
        transition: opacity 0.3s ease;
        z-index: 1;

        .play-button {
          width: 50px;
          height: 50px;
          background: rgba(255, 255, 255, 0.9);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
          color: #333;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
        }
      }

      // 首页轮播/推荐：鼠标覆盖不再显示“待播放按钮”
      &:hover .play-overlay {
        opacity: 0;
      }
    }

    .v-title {
      font-size: 13px;
      color: #222;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .v-sub {
      font-size: 12px;
      color: #8a8a8a;
    }
  }

  .loading-bar {
    text-align: center;
    color: #8a8a8a;
    padding: 12px 0;
  }

  .load-more-trigger {
    height: 1px;
    width: 100%;
    visibility: hidden;
    pointer-events: none;
  }
}

/* 固定阈值以下不再自适应，保持 1200px 布局 */

/* 定义跳动动画，保持与 HomeView.vue 一致 */
@keyframes jump {
  0% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
  100% { transform: translateY(0); }
}

/* 大屏幕响应式：当屏幕宽度超过1800px时，让内容区域占据75%宽度 */
@media (min-width: 1800px) {
  .navigation-section,
  .hero-grid,
  .section {
    max-width: 75%;
  }

  .section .video-virtual-wrapper {
    width: 100%;
  }
  
  .navigation-section,
  .hero-grid,
  .section {
    padding: 0 20px;
    box-sizing: border-box;
  }
}
</style>
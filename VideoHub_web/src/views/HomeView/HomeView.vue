<template>
  <div class="cili_header">
    <div class="hearder_bar">
      <ul class="header_bar_left">
        <li class="events">
          <img src="/assets/home.png" class="events-img" />
          <span>首页</span>
        </li>
        <li class="events"><span>番剧</span></li>
        <li class="events"><span>直播</span></li>
        <li class="events"><span>游戏中心</span></li>
        <li class="events"><span>会员购</span></li>
        <li class="events"><span>漫画</span></li>
        <li class="events"><span>赛事</span></li>
        <li class="events">
          <img src="/assets/download-client.png" class="events-img" />
          <span>下载客户端</span>
        </li>
      </ul>
      <div class="header_bar_search">
        <input type="text" class="search-input" />
        <div class="search-btn">
          <img src="/assets/search.png" class="search-btn-img" />
        </div>
      </div>
      <ul class="header_bar_right">
        <li class="events">
          <a href="#" class="login-link" @click.prevent="showLogin = true">
            <span class="login-button">登录</span>
          </a>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/vip.png" class="events-imgb" />
            <span>大会员</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/messages.png" class="events-imgb" />
            <span>消息</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/feed.png" class="events-imgb" />
            <span>动态</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/favorites.png" class="events-imgb" />
            <span>收藏</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/history.png" class="events-imgb" />
            <span>历史</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="/assets/creator-center.png" class="events-imgb" />
            <span>创作中心</span>
          </div>
        </li>
        <li class="events">
          <div class="post-background">
            <img src="/assets/upload.png" class="post-icon" />
            <span>投稿</span>
          </div>
        </li>
      </ul>
    </div>
    <img src="/assets/header.png" alt="header background" class="background-image" />
    <br>

    <div class="header_channel">
      <div class="channel-icons">
        <li class="icons">
          <div class="dynamic">
            <div class="dynamic-bg"><img src="/assets/feed.png" class="dynamic-img" /></div>
            <span>动态</span>
          </div>
        </li>
        <li class="icons">
          <div class="popular">
            <div class="popular-bg"><img src="/assets/trending.png" class="popular-img" /></div>
            <span>热门</span>
          </div>
        </li>
      </div>

      <div class="channel-container">
        <div class="channel-left">
          <div class="grid-container">
            <a href="#" class="grid-item">番剧</a>
            <a href="#" class="grid-item">国创</a>
            <a href="#" class="grid-item">综艺</a>
            <a href="#" class="grid-item">动画</a>
            <a href="#" class="grid-item">鬼畜</a>
            <a href="#" class="grid-item">舞蹈</a>
            <a href="#" class="grid-item">娱乐</a>
            <a href="#" class="grid-item">科技</a>
            <a href="#" class="grid-item">美食</a>
            <a href="#" class="grid-item">汽车</a>
            <a href="#" class="grid-item">运动</a>
            <a href="#" class="grid-item">VLOG</a>
            <a href="#" class="grid-item">单机游戏</a>
            <a href="#" class="grid-item">公益</a>
            <a href="#" class="grid-item">电影</a>
            <a href="#" class="grid-item">电视剧</a>
            <a href="#" class="grid-item">纪录片</a>
            <a href="#" class="grid-item">游戏</a>
            <a href="#" class="grid-item">音乐</a>
            <a href="#" class="grid-item">影视</a>
            <a href="#" class="grid-item">知识</a>
            <a href="#" class="grid-item">资讯</a>
            <a href="#" class="grid-item">生活</a>
            <a href="#" class="grid-item">时尚</a>
            <a href="#" class="grid-item">动物圈</a>
            <a href="#" class="grid-item">搞笑</a>
            <a href="#" class="grid-item">虚拟UP主</a>
            <a href="#" class="grid-item">公开课</a>
          </div>
        </div>
        <div class="vertical-divider"></div> <!-- 分隔线 -->
        <div class="channel-right">
          <div class="grid-containerb">
            <a href="#" class="grid-itemb"><img src="/assets/channel/column.png" class="grid-itemb-img">专栏</a>
            <a href="#" class="grid-itemb"><img src="/assets/channel/activity.png" class="grid-itemb-img">活动</a>
            <a href="#" class="grid-itemb"><img src="/assets/channel/community.png" class="grid-itemb-img">社区中心</a>
            <a href="#" class="grid-itemb"><img src="/assets/channel/live.png" class="grid-itemb-img">直播</a>
            <a href="#" class="grid-itemb"><img src="/assets/channel/class.png" class="grid-itemb-img">课堂</a>
            <a href="#" class="grid-itemb"><img src="/assets/channel/new-songs-chart.png" class="grid-itemb-img">新歌热榜</a>
          </div>
        </div>
      </div>

    </div>

  </div>
  <br />
  <!-- 视频内容区域 -->
  <main class="video-grid">
    <VideoCard v-for="video in videoList" :key="video.id" :video="video" />
  </main>

  <!-- 登录组件 -->
  <Login v-model:show="showLogin" />

</template>

<script setup>
import { ref } from "vue";
import VideoCard from "@/components/VideoCard.vue";
import { useUserStore } from '@/stores/user'
import Login from "@/components/Login.vue";

const userStore = useUserStore()
const showLogin = ref(false)

// 临时测试数据
const videoList = ref([
  {
    id: 1,
    title: "【4K】超震撼的绝景之旅",
    cover: "/images/demo-cover1.jpg",
    playCount: "12.3万",
    duration: "15:30",
  },
  // 添加更多测试数据...
]);
</script>

<style lang="scss" scoped>
// 确保 router-link 不影响布局
.login-link {
  display: inline-block;
  text-decoration: none;
}

// 频道网格样式
.grid-itemb {
  display: flex;
  font-size: 8px;
  justify-content: center;
  align-items: center;
  background-color: transparent;
  padding: 0;
  text-decoration: none;
  color: #61666d;
  gap: 2.5px;

  &:hover {
    color: skyblue;
  }

  &-img {
    width: 10px;
    height: 8px;
  }
}

// 频道容器
.channel-container {
  display: flex;
  gap: 5px;
  position: absolute;
  height: 70px;
  width: 85%;
  left: 14%;

  &-left {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 80%;
    height: 100%;
  }

  &-right {
    display: flex;
    width: 20%;
    height: 100%;
  }
}

// 分隔线
.vertical-divider {
  width: 0.5px;
  background-color: #ccc;
  margin-top: 2%;
  height: 50%;
}

// 网格容器
.grid-container {
  display: grid;
  grid-template-columns: repeat(14, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 4px;
  width: 100%;
  height: 50%;

  &b {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: repeat(2, 1fr);
    gap: 0.5px;
    width: 75%;
    height: 50%;
    margin-top: 10%;
  }
}

// 网格项目
.grid-item {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0;
  border-radius: 5px;
  padding: 2px;
  text-decoration: none;
  color: #61666d;
  font-size: 8px;
  transition: background-color 0.3s, color 0.3s;

  &:hover {
    background-color: #e0e0e0;
    color: black;
  }
}

// 动态图标样式
.dynamic {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: auto;
  height: auto;

  &-bg {
    width: 24px;
    height: 24px;
    background-color: #ff9212;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &-img {
    width: 14px;
    height: 14px;
    filter: brightness(0) invert(1);
    font-weight: 300;
  }

  span {
    font-size: 8px;
    color: black;
    margin-top: 3px;
  }
}

// 热门图标样式
.popular {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: auto;
  height: auto;

  &-bg {
    width: 24px;
    height: 24px;
    background-color: #f07775;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &-img {
    width: 12px;
    height: 12px;
    filter: brightness(0) invert(1);
  }

  span {
    font-size: 8px;
    color: black;
    margin-top: 3px;
  }
}

// 频道图标
.channel-icons {
  display: flex;
  gap: 15px;
  position: absolute;
  height: 70px;
  width: auto;
  left: 6%;
  top: 14%;

  .icons {
    list-style-type: none;
  }
}

// 头部父元素样式
.cili_header {
  height: 300px;
  margin: 0 auto;
  padding: 0;
  width: 100%;
  max-width: 1659.33px;
  min-width: 0;
  position: relative;
  background-color: #ffffff;
  --design-width: 1659.33px;
  --header-scale: clamp(0.6, calc(100% / var(--design-width)), 1);

  // 背景图片样式
  .background-image {
    position: absolute;
    top: 0;
    left: 0;
    z-index: 0;
    width: 100%;
    max-height: 153px;
  }

  // 头部栏容器
    .hearder_bar {
    position: relative;
    z-index: 1;
    top: 0;
    width: 100%;
    height: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 6px;
    padding: 6px 2%;

    // 左侧栏样式
    .header_bar_left {
      display: flex;
      position: static;
      flex: 0 1 auto;
      max-width: 30%;
      list-style-type: none;
      padding-left: 0;
      margin: 0;
      gap: 10px;
      flex-wrap: wrap;

      .events {
        margin-right: 0;
        list-style-type: none;
        display: flex;
        align-items: center;

        &:last-child {
          margin-right: 0;
        }

        span {
          color: white;
          font-size: 14px;
          cursor: pointer;
          transition: color 0.2s, transform 0.2s;
          display: inline-block;
        }
        .events-img {
          width: 18px; // 增大图标宽度
          height: 18px; // 增大图标高度
          margin-right: 5px; // 调整图标和文字之间的间距
          margin-top: 2px;
          filter: brightness(0) invert(1);
          top: 15%;
        }
        &:hover span {
          color: #00a1d6;
          animation: jump 0.3s ease;
        }
      }
    }

    // 右侧栏样式
    .header_bar_right {
      display: flex;
      position: static;
      flex: 0 1 auto;
      max-width: 30%;
      list-style-type: none;
      padding-left: 0;
      margin: 0;
      gap: 10px;
      flex-wrap: wrap;
      margin-right: 100px;
      

      .events {
        margin-left: 1px;
        list-style-type: none;

        span {
          color: white;
          font-size: 14px;
          cursor: pointer;
          transition: color 0.2s, transform 0.2s;
          display: inline-block;
        }
        &-imgb:hover {
          animation: jump 0.3s ease;
        }
      }
    }

    // 搜索框容器样式
    .header_bar_search {
      position: relative;
      flex: 0 0 450px;
      width: 450px;
      min-width: 450px;
      max-width: 450px;
      display: flex;
      align-items: center;
      background: #ffffff;
      border-radius: 6px;
      margin: 0 4px;

      // 搜索输入框样式
      .search-input {
        width: 100%;
        height: 30px;
        padding: 0 48px 0 10px;
        position: relative;
        overflow: hidden;
        border: 1px solid var(--line_regular);
        border-radius: 6px;
        outline: none;
        font-size: 9.5px;
        transition: background-color 0.3s;
        z-index: 2;

        &:focus {
          padding-left: 5px;
          background: #ffffff;
        }
      }

      // 搜索按钮样式
      .search-btn {
        position: absolute;
        right: 3px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0;
        padding: 0;
        width: 20px;
        height: 20px;
        border: none;
        border-radius: 6px;
        color: var(--text1);
        line-height: 32px;
        cursor: pointer;
        transition: background-color 0.3s;
        z-index: 3;

        &:hover {
          background-color: rgba(0, 0, 0, 0.1);
        }

        &-img {
          width: 12px;
          height: 12px;
        }
      }
    }
  }


  // 导航栏布局
  .header_channel {
    height: 110px;
    width: 100%;
    max-width: 1133px;
    position: absolute;
    top: 103px;
  }
}

// 响应式：在较窄宽度时收紧间距，保证不重叠
@media (max-width: 1280px) {
  .hearder_bar {
    gap: 8px;
    padding: 8px 2%;
  }
  .header_bar_left,
  .header_bar_right {
    gap: 12px;
    max-width: 35%;
  }
  .header_bar_search {
    max-width: 600px;
    min-width: 220px;
  }
}

@media (max-width: 1024px) {
  .header_bar_left,
  .header_bar_right {
    gap: 10px;
    max-width: 40%;
  }
}

// 登录按钮样式
.login-button {
  background-color: #00aeec;
  color: white;
  padding: 6px 4px;
  border-radius: 48%;
  display: inline-block;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;

  &:hover {
    background-color: #0099cc;
    transform: translateY(-2px);
  }
}

// 投稿按钮样式
.post-background {
  background-color: #fb7299;
  border-radius: 5px;
  padding: 1px 10px;
  display: inline-flex;
  align-items: center;
  gap: 1px;
  width: 55px;
  height: 27px;
  margin-top: 3.5px;

  span {
    color: white;
    font-size: 9.5px;
  }

  .post-icon {
    width: 19px;
    height: 19px;
    filter: brightness(0) invert(1);
  }
}

// 图片样式
.events {
  &-img {
    width: 15px;
    height: 12px;
    margin-right: 3px;
    filter: brightness(0) invert(1);
  }

  &-imgb {
    width: 18px;
    height: 18px;
    display: block;
    margin: 0 auto 0px;
    filter: brightness(0) invert(1);

    &-container {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  }
}

// 视频网格布局
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

// 定义跳动动画
@keyframes jump {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
  100% {
    transform: translateY(0);
  }
}
</style>
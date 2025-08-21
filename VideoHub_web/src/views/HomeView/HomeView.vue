<template>
  <div class="cili_header">
    <div class="hearder_bar">
      <ul class="header_bar_left">
        <li class="events">
          <img src="../assets/home.png" class="events-img" />
          <span>首页</span>
        </li>
        <li class="events"><span>番剧</span></li>
        <li class="events"><span>直播</span></li>
        <li class="events"><span>游戏中心</span></li>
        <li class="events"><span>会员购</span></li>
        <li class="events"><span>漫画</span></li>
        <li class="events"><span>赛事</span></li>
        <li class="events">
          <img src="../assets/download-client.png" class="events-img" />
          <span>下载客户端</span>
        </li>
      </ul>
      <div class="header_bar_search">
        <input type="text" class="search-input" />
        <div class="search-btn">
          <img src="../assets/search.png" class="search-btn-img" />
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
            <img src="../assets/vip.png" class="events-imgb" />
            <span>大会员</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="../assets/messages.png" class="events-imgb" />
            <span>消息</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="../assets/feed.png" class="events-imgb" />
            <span>动态</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="../assets/favorites.png" class="events-imgb" />
            <span>收藏</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="../assets/history.png" class="events-imgb" />
            <span>历史</span>
          </div>
        </li>
        <li class="events">
          <div class="events-imgb-container">
            <img src="../assets/creator-center.png" class="events-imgb" />
            <span>创作中心</span>
          </div>
        </li>
        <li class="events">
          <div class="post-background">
            <img src="../assets/upload.png" class="post-icon" />
            <span>投稿</span>
          </div>
        </li>
      </ul>
    </div>
    <img src="../assets/header.png" alt="header background" class="background-image" />
    <br>

    <div class="header_channel">
      <div class="channel-icons">
        <li class="icons">
          <div class="dynamic">
            <div class="dynamic-bg"><img src="../assets/feed.png" class="dynamic-img" /></div>
            <span>动态</span>
          </div>
        </li>
        <li class="icons">
          <div class="popular">
            <div class="popular-bg"><img src="../assets/trending.png" class="popular-img" /></div>
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
            <a href="#" class="grid-itemb"><img src="../assets/channel/column.png" class="grid-itemb-img">专栏</a>
            <a href="#" class="grid-itemb"><img src="../assets/channel/activity.png" class="grid-itemb-img">活动</a>
            <a href="#" class="grid-itemb"><img src="../assets/channel/community.png" class="grid-itemb-img">社区中心</a>
            <a href="#" class="grid-itemb"><img src="../assets/channel/live.png" class="grid-itemb-img">直播</a>
            <a href="#" class="grid-itemb"><img src="../assets/channel/class.png" class="grid-itemb-img">课堂</a>
            <a href="#" class="grid-itemb"><img src="../assets/channel/new-songs-chart.png" class="grid-itemb-img">新歌热榜</a>
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

<style scoped>
/* 确保 router-link 不影响布局 */
.login-link {
  display: inline-block;
  text-decoration: none;
}

.grid-itemb:hover {
  color: skyblue; /* 鼠标悬停时字体颜色变为蓝色 */
}
.grid-itemb{
  display: flex;
  font-size: 8px;
  justify-content: center;
  align-items: center;
  background-color: transparent; /* 设置背景色为透明 */
  padding: 0; /* 内边距 */
  text-decoration: none; /* 去掉下划线 */
  color: #61666d; /* 字体颜色 */
  font-size: 8px; /* 字体大小 */
  gap:2.5px;
}

.grid-itemb-img {
  width: 10px;
  height: 8px;
}

.channel-container {
  display: flex; /* 横向排列 */
  gap: 5px; /* 图标之间的间距 */
  position: absolute;
  height: 70px;
  width: 85%;
  left: 14%;
}

.channel-left {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 80%; /* 调整宽度 */
  height: 100%;
}

.channel-right {
  display: flex;
  width: 20%; /* 调整宽度 */
  height: 100%;
}

.vertical-divider {
  width: 0.5px; /* 分隔线的宽度 */
  background-color: #ccc; /* 分隔线的颜色 */
  margin-top:2%;
  height: 50%; /* 分隔线的高度 */
}

.grid-containerb {
  display: grid;
  grid-template-columns: repeat(3, 1fr); 
  grid-template-rows: repeat(2, 1fr); /* 2行 */
  gap: 0.5px; /* 间距 */
  width: 75%;
  height: 50%;
  margin-top:10%;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(14, 1fr); /* 14列 */
  grid-template-rows: repeat(2, 1fr); /* 2行 */
  gap: 4px; /* 间距 */
  width: 100%;
  height: 50%;
}

.grid-item {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f0f0; /* 背景色 */
  border-radius: 5px; /* 圆角 */
  padding: 2px; /* 内边距 */
  text-decoration: none; /* 去掉下划线 */
  color: #61666d; /* 字体颜色 */
  font-size: 8px; /* 字体大小 */
  transition: background-color 0.3s,color 0.3s; /* 背景颜色过渡效果 */
}

.grid-item:hover {
  background-color: #e0e0e0; /* 鼠标悬停时的背景色 */
  color:black;
}

.channel-container{
  display: flex; /* 横向排列 */
  gap: 15px; /* 图标之间的间距 */
  position: absolute;
  height: 70px;
  width: 85%;
  left:14%; 
}

/* 动态图标样式 */
.dynamic {
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center; /* 水平居中 */
  justify-content: center; /* 垂直居中 */
  width: auto; /* 宽度自适应 */
  height: auto; /* 高度自适应 */
}

.dynamic-bg {
  width: 24px; /* 宽度与高度相等 */
  height: 24px; /* 高度与宽度相等 */
  background-color: #ff9212; /* 背景色为 #ff9212 */
  border-radius: 50%; /* 圆形 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.dynamic-img {
  width: 14px; /* 调整图标大小 */
  height: 14px; /* 调整图标大小 */
  filter: brightness(0) invert(1); /* 图标变白 */
  font-weight: 300;
}

.dynamic span {
  font-size: 8px; /* 文字大小 */
  color: black; /* 文字颜色 */
  margin-top: 3px; /* 文字与图标的间距 */
}

/* 热门图标样式 */
.popular {
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center; /* 水平居中 */
  justify-content: center; /* 垂直居中 */
  width: auto; /* 宽度自适应 */
  height: auto; /* 高度自适应 */
}

.popular-bg {
  width: 24px; /* 宽度与高度相等 */
  height: 24px; /* 高度与宽度相等 */
  background-color: #f07775; /* 背景色为 #f07775 */
  border-radius: 50%; /* 圆形 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.popular-img {
  width: 12px; /* 调整图标大小 */
  height: 12px; /* 调整图标大小 */
  filter: brightness(0) invert(1); /* 图标变白 */
}

.popular span {
  font-size: 8px; /* 文字大小 */
  color: black; /* 文字颜色 */
  margin-top: 3px; /* 文字与图标的间距 */
}

/* 横向排列 */
.channel-icons {
  display: flex; /* 横向排列 */
  gap: 15px; /* 图标之间的间距 */
  position: absolute;
  height: 70px;
  width: auto; /* 宽度自适应 */
  left:6%; /* 调整位置 */
  top: 14%; /* 调整位置 */
}

.channel-icons .icons {
  list-style-type: none; /* 去掉 ul 的圆点 */
}

/* 头部父元素样式 */
.cili_header {
  height: 300px;
  margin: 0 auto;
  padding: 0;
  width: 100%;
  max-width: 1659.33px;
  min-width: 800px;
  position: relative;
  background-color: #ffffff;
}

/* 背景图片样式 */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 0;
  width: 100%;
  max-height: 153px;
}

/* 头部栏容器 */
.hearder_bar {
  position: relative; /* 设置为相对定位，以便子元素可以绝对定位 */
  z-index: 1; /* 确保头部栏在背景图片之上 */
  top: 0;
  width: 100%;
  height: 15%;
}

/* 左侧栏样式 */
.header_bar_left {
  display: flex;
  position: absolute;
  left: 3%;
  top: 15%;
  list-style-type: none; /* 去掉 ul 的圆点 */
  padding-left: 0; /* 移除默认的ul内边距 */
  margin: 0; /* 移除默认的外边距 */
}

.header_bar_left .events {
  margin-right: 10px;
  list-style-type: none; /* 去掉 li 的圆点 */
}

.header_bar_left .events:last-child {
  margin-right: 0; /* 最后一个元素不需要右侧间距 */
}

.header_bar_left .events span {
  color: white; /* 字体颜色为白色 */
  font-size: 9.5px; /* 字体大小 */
  cursor: pointer; /* 鼠标悬停时显示手型 */
  transition: color 0.2s, transform 0.2s; /* 添加颜色和位移过渡效果 */
  display: inline-block; /* 确保 transform 生效 */
}

.header_bar_left .events:hover span {
  color: #00a1d6; /* 鼠标悬停时改变字体颜色 */
  animation: jump 0.3s ease; /* 触发跳动动画 */
}

/* 右侧栏样式 */
.header_bar_right {
  display: flex;
  position: absolute;
  right: 3%;
  top: 15%;
  list-style-type: none; /* 去掉 ul 的圆点 */
  padding-left: 0; /* 移除默认的ul内边距 */
  margin: 0; /* 移除默认的外边距 */
}

.header_bar_right .events {
  margin-left: 15px;
  list-style-type: none; /* 去掉 li 的圆点 */
}

.header_bar_right .events span {
  color: white; /* 字体颜色为白色 */
  font-size: 9.5px; /* 字体大小 */
  cursor: pointer; /* 鼠标悬停时显示手型 */
  transition: color 0.2s, transform 0.2s; /* 添加颜色和位移过渡效果 */
  display: inline-block; /* 确保 transform 生效 */
}

.header_bar_right .events-imgb:hover {
  animation: jump 0.3s ease; /* 触发跳动动画 */
}

/* 登录按钮样式 */
.login-button {
  background-color: #00aeec; /* 蓝色背景 */
  color: white; /* 文字颜色 */
  padding: 4px 3px; /* 内边距 */
  border-radius: 48%; /* 圆形 */
  display: inline-block; /* 确保背景包裹文字 */
  font-size: 9.5px; /* 文字大小 */
  cursor: pointer; /* 鼠标悬停时显示手型 */
  transition: background-color 0.2s, transform 0.2s; /* 添加背景色和位移过渡效果 */
}

.login-button:hover {
  background-color: #0099cc; /* 鼠标悬停时背景色变深 */
  transform: translateY(-2px); /* 鼠标悬停时文字向上跳动 */
}

/* 投稿按钮样式 */
.post-background {
  background-color: #fb7299; /* 背景颜色 */
  border-radius: 5px; /* 圆角弧度 */
  padding: 1px 10px; /* 内边距 */
  display: inline-flex; /* 使用 flex 布局 */
  align-items: center; /* 垂直居中 */
  gap: 1px; /* 图标和文字之间的间距 */
  width: 40px;
  height: 24px;
  margin-top: 3.5px;
}

.post-background span {
  color: white; /* 文字颜色 */
  font-size: 9.5px; /* 文字大小 */
}

.post-icon {
  width: 19px; /* 图标宽度 */
  height: 19px; /* 图标高度 */
  filter: brightness(0) invert(1); /* 图标变白 */
}

/* 定义跳动动画 */
@keyframes jump {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px); /* 向上跳动 */
  }
  100% {
    transform: translateY(0); /* 回到原位 */
  }
}

/* 搜索框容器样式 */
.header_bar_search {
  position: absolute;
  left: 50%;
  top: 60%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 6px;
}

/* 搜索输入框样式 */
.search-input {
  width: 330px;
  height: 25px;
  padding: 0 48px 0 10px; /* 调整左侧 padding */
  position: relative;
  overflow: hidden;
  border: 1px solid var(--line_regular);
  border-radius: 6px;
  outline: none;
  font-size: 9.5px;
  transition: background-color 0.3s;
  z-index: 2;
}

.search-input:focus {
  padding-left: 5px; /* 焦点状态下左侧 padding 为 5px */
  border: 1px solid #e3e5e7;
  background: #ffffff;
}

/* 搜索按钮样式 */
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
}

.search-btn:hover {
  background-color: rgba(0, 0, 0, 0.1); /* 鼠标悬停时的背景色 */
}

.search-btn-img {
  width: 12px;
  height: 12px;
}

/* events-img 样式：图片在 span 左边 */
.events-img {
  width: 15px; /* 调整图片大小 */
  height: 12px; /* 调整图片大小 */
  margin-right: 3px; /* 图片与文字之间的间距 */
}

/* events-imgb 样式：图片在 span 上边 */
.events-imgb {
  width: 14px; /* 调整图片大小 */
  height: 14px; /* 调整图片大小 */
  display: block; /* 将图片设置为块级元素 */
  margin: 0 auto 0px; /* 图片与文字之间的间距 */
}

/* 确保 events-imgb 的容器是垂直排列 */
.events-imgb-container {
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center; /* 水平居中 */
}

.events-img,
.events-imgb {
  filter: brightness(0) invert(1); /* 图标变白 */
}

/* 视频网格布局 */
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

/* 导航栏布局 */
.header_channel {
  height: 110px;
  width: 1133px;
  position: absolute;
  top: 103px;
}

/* 当屏幕尺寸小于500px时，背景图片长度样式为auto */
@media (max-width: 500px) {
  .background-image {
    height: auto;
    width: 100%;
  }
  .hearder_bar {
    position: absolute;
    z-index: 1;
    top: 0;
    left: 0;
    width: 100%;
    height: auto;
  }
}
</style>
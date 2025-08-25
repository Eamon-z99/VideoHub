<template>
  <div class="user-profile">
    <ProfileHeader />
    <div class="profile-body">
      <div class="profile-main">
        <ProfileTabs @change="onTabChange" :active="activeTab" />
        <keep-alive>
          <component :is="currentView" />
        </keep-alive>
      </div>
      <aside class="profile-aside">
        <ProfileSidebar />
      </aside>
    </div>
  </div>
  
</template>

<script>
import ProfileHeader from './components/ProfileHeader.vue'
import ProfileTabs from './components/ProfileTabs.vue'
import ProfileSidebar from './components/ProfileSidebar.vue'
import TabCollections from './tabs/TabCollections.vue'
import TabDynamics from './tabs/TabDynamics.vue'
import TabAlbums from './tabs/TabAlbums.vue'

export default {
  name: 'UserProfile',
  components: { ProfileHeader, ProfileTabs, ProfileSidebar, TabCollections, TabDynamics, TabAlbums },
  data () {
    return {
      activeTab: 'collections'
    }
  },
  computed: {
    currentView () {
      const map = {
        collections: 'TabCollections',
        dynamics: 'TabDynamics',
        albums: 'TabAlbums'
      }
      return map[this.activeTab]
    }
  },
  methods: {
    onTabChange (key) {
      this.activeTab = key
    }
  }
}
</script>

<style lang="scss" scoped>
.user-profile {
  display: flex;
  flex-direction: column;
  .profile-body {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 16px;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 16px 0 32px;

    .profile-main {
      background: #fff;
      border-radius: 8px;
      padding: 16px;
    }

    .profile-aside {
      position: sticky;
      top: 72px;
      height: fit-content;
    }
  }
}

@media (max-width: 960px) {
  .user-profile {
    .profile-body {
      grid-template-columns: 1fr;
    }
  }
}
</style>



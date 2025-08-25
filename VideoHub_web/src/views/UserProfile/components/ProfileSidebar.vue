<template>
  <div class="profile-sidebar">
    <section class="card">
      <h3>公告</h3>
      <p>编辑我的公告</p>
    </section>
    <section class="card">
      <h3>个人资料</h3>
      <ul>
        <li>UID 473967601</li>
        <li>生日 08-20</li>
      </ul>
    </section>
  </div>
</template>

<script>
export default { name: 'ProfileSidebar' }
</script>

<style lang="scss" scoped>
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 12px;
  .card {
    background: #fff;
    border-radius: 8px;
    padding: 12px;
    h3 { margin: 0 0 8px; font-size: 15px; }
    ul { margin: 0; padding-left: 16px; color: #666; }
  }
}
</style>



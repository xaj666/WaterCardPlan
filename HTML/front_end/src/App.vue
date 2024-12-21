<template>
  <div id="app">
    <el-container>
      <el-aside width="200px">
        <div class="logo">
          <img src="@/assets/logo.png" alt="logo" v-if="false">
          <span>WaterCard管理系统</span>
        </div>
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#fff"
          active-text-color="#409EFF"
          router>
          <el-menu-item index="/index">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <i class="el-icon-s-order"></i>
            <span slot="title">订单管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="breadcrumb-container">
            <div class="tags-view-container">
              <router-link
                v-for="tag in visitedViews"
                :key="tag.path"
                :class="isActive(tag) ? 'active-tag' : 'normal-tag'"
                :to="tag.path"
                class="tags-view-item"
              >
                <span>{{ tag.title }}</span>
                <span class="el-icon-close" @click.prevent.stop="closeSelectedTag(tag)"></span>
              </router-link>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  data() {
    return {
      visitedViews: []
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.addVisitedView(route)
      },
      immediate: true
    }
  },
  methods: {
    addVisitedView(route) {
      if (route.meta && route.meta.title) {
        const view = {
          path: route.path,
          title: route.meta.title
        }
        if (!this.visitedViews.some(v => v.path === view.path)) {
          this.visitedViews.push(view)
        }
      }
    },
    isActive(tag) {
      return tag.path === this.$route.path
    },
    closeSelectedTag(view) {
      const index = this.visitedViews.indexOf(view)
      this.visitedViews.splice(index, 1)
      if (this.isActive(view)) {
        const latestView = this.visitedViews.slice(-1)[0]
        if (latestView) {
          this.$router.push(latestView.path)
        } else {
          this.$router.push('/')
        }
      }
    }
  }
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: hidden;
}

#app {
  height: 100vh;
  overflow: hidden;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  height: 100vh;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 20px;
  background-color: #2b3649;
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  padding: 0 20px;
}

.breadcrumb-container {
  display: flex;
  align-items: center;
  height: 100%;
}

.el-breadcrumb {
  line-height: 60px;
  font-size: 14px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

/* 美化滚动条 */
.el-main::-webkit-scrollbar {
  width: 6px;
}

.el-main::-webkit-scrollbar-thumb {
  background-color: rgba(144, 147, 153, 0.3);
  border-radius: 3px;
}

.el-main::-webkit-scrollbar-track {
  background-color: transparent;
}

.tags-view-container {
  width: 100%;
  height: 34px;
  background: #fff;
  display: flex;
  align-items: center;
}

.tags-view-item {
  display: inline-flex;
  align-items: center;
  margin: 2px 5px;
  padding: 0 8px;
  height: 26px;
  line-height: 26px;
  border: 1px solid #d8dce5;
  border-radius: 3px;
  font-size: 12px;
  text-decoration: none;
  color: #495060;
  background: #fff;
  transition: all 0.3s;
}

.tags-view-item:first-of-type {
  margin-left: 15px;
}

.tags-view-item .el-icon-close {
  margin-left: 5px;
  border-radius: 50%;
  text-align: center;
  transition: all .3s;
  width: 16px;
  height: 16px;
  line-height: 16px;
  vertical-align: middle;
}

.tags-view-item .el-icon-close:hover {
  background-color: #b4bccc;
  color: #fff;
}

.active-tag {
  background-color: #42b983;
  color: #fff !important;
  border-color: #42b983;
}

.active-tag .el-icon-close {
  color: #fff;
}

.active-tag .el-icon-close:hover {
  background-color: rgba(255,255,255,0.3);
}

.normal-tag:hover {
  background-color: #f5f7fa;
}

.el-menu-item.is-active {
  position: relative;
  background-color: #263445 !important;
}

.el-menu-item.is-active:before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background-color: #409EFF;
}
</style>

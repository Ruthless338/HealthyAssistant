<!-- src/App.vue -->
<template>
  <div id="app">
    <NavBar
      @nav-click="handleRouteChange"
    />
    <div class="content-container">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>

import NavBar from './components/NavBar.vue';
import { mapState } from 'vuex';

export default {
  name: 'App',
  components: {
    NavBar
  },
  data (){
    return {
      currentRoute: 'Plan',
    }
  },
  computed:{
    ...mapState(['user']),
  },
  mounted() {
    this.checkUserProfile();
  },
  watch: {
    user: {
      handler() {
        this.checkUserProfile();
      },
      deep: true,
      immediate: true
    }
  },
  methods:{
    handleRouteChange(route){
      this.$router.push(route);
      console.log('路由切换到: ', route);
    },
    checkUserProfile() {
      const user = this.user;
      if (
        user &&
        user.isLoggedIn &&
        (
          !user.gender ||
          !user.age ||
          !user.height ||
          !user.weight ||
          !user.goal || user.goal.length === 0 ||
          !user.interest || user.interest.length === 0 ||
          !user.part || user.part.length === 0
        ) &&
        this.$route.name !== 'Selector'
      ) {
        this.$router.replace({ name: 'Selector' });
      }
    }
  },
};
</script>

<style>
/* 移除原有样式，使用全局样式 */
</style>
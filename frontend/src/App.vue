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
  methods:{
    handleRouteChange(route){
      this.$router.push(route);
      console.log('路由切换到: ', route);
    }
  },
};
</script>

<style>
/* 移除原有样式，使用全局样式 */
</style>
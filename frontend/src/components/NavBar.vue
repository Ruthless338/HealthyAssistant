<!-- src/components/NavBar.vue -->
 <template>
    <div class="navbar">
        <div class="github-link-container">
            <GitHubLink />
        </div>
        <div class="logo">
            <div class="logo-icon">
                <span class="icon">💪</span>
            </div>
            <span class="logo-text gradient-text">运动健康助手</span>
        </div>
        <div class="nav-links">
            <div 
                v-for="(item, index) in navItems"
                :key="index"
                class="nav-item"
                :class="{active :activeIndex === index}"
                @click="handleNavClick(index, item.route)"
            >
                <span class="nav-icon">{{ getNavIcon(item.name) }}</span>
                <span class="nav-text">{{ item.name }}</span>
                <div class="nav-indicator"></div>
            </div>
        </div>
        <div class="user-actions">
            <!-- 登录/注册按钮  -->
            <div v-if="!isLoggedIn" class="auth-buttons">
                <button class="login-btn glass" @click="handleLoginRegister">
                    <span class="btn-icon">🔐</span>
                    登录/注册
                </button>
            </div>
            <!-- 用户头像和用户名 -->
            <div v-else class="user-profile glass" @click="goToUserProfile">
                <div class="avatar-container">
                    <img :src="'http://localhost:8000/uploads/'+user.avatar" alt="User Avatar" class="avatar" />
                    <div class="avatar-status"></div>
                </div>
                <div class="user-info">
                    <div class="username">{{ user.username || '用户' }}</div>
                    <div class="user-status">在线</div>
                </div>
            </div>
            <button class="logout-btn" @click="logout">退出登录</button>
        </div>
    </div>
</template>
<script>
import GitHubLink from './GitHubLink.vue';
import { mapState } from 'vuex'; 
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
export default{
    name:'NavBar',
    components:{
        GitHubLink,
    },
    props:{
        isLoggedIn:{
            type:Boolean,
            default:false,
        },
    },
    data(){
        return {
            activeIndex: 0,
            navItems: [ 
              { name: '运动计划', route: 'Plan' },
              { name: '运动记录', route: 'Record' },
              { name: '运动社区', route: 'Community' },
            ],
            logo: '', 
        }
    },
    computed: {
        ...mapState(['user']),
        isLoggedIn() {
            return this.user.isLoggedIn;
        },
    },
    methods:{
        handleNavClick(index, route){
            this.activeIndex = index;
            this.$emit('nav-click', route);
            this.$router.push(route);
        },
        handleLoginRegister(){
            this.$router.push({name:'LoginRegister'});
        },
        goToUserProfile(){
            console.log('goToUserProfile');
            this.$router.push({name:'UserProfile'});
        },
        getNavIcon(name) {
            const icons = {
                '运动计划': '📋',
                '运动记录': '📊',
                '运动社区': '👥'
            };
            return icons[name] || '📄';
        },
        logout() {
            const store = useStore()
            const router = useRouter()
            store.commit('setUser', null)
            localStorage.removeItem('user')
            router.push({ name: 'Login' })
        }
    }
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80px;
  padding: 0 2rem;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar:hover {
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.github-link-container {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1001;
}

.logo {
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
}

.logo-icon .icon {
  font-size: 1.5rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.025em;
}

.nav-links {
  display: flex;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.5);
  padding: 0.5rem;
  border-radius: var(--radius-xl);
  backdrop-filter: blur(10px);
}

.nav-item {
  cursor: pointer;
  padding: 0.75rem 1.25rem;
  border-radius: var(--radius-lg);
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.3s ease;
  overflow: hidden;
}

.nav-item:hover {
  color: var(--primary-color);
  background: rgba(16, 185, 129, 0.1);
  transform: translateY(-1px);
}

.nav-item.active {
  color: var(--primary-color);
  background: rgba(16, 185, 129, 0.15);
  font-weight: 600;
}

.nav-item.active .nav-indicator {
  transform: scaleX(1);
}

.nav-icon {
  font-size: 1.125rem;
  transition: transform 0.2s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.nav-text {
  font-weight: inherit;
}

.nav-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  transform: scaleX(0);
  transition: transform 0.3s ease;
  border-radius: 2px 2px 0 0;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.auth-buttons {
  display: flex;
  gap: 0.75rem;
}

.login-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  border: none;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius-xl);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
}

.btn-icon {
  font-size: 1rem;
}

.user-profile {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-xl);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-profile:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
  background: rgba(255, 255, 255, 0.3);
}

.avatar-container {
  position: relative;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.user-profile:hover .avatar {
  border-color: var(--primary-color);
  transform: scale(1.05);
}

.avatar-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  background: var(--success-color);
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: var(--shadow-sm);
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.username {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.user-status {
  font-size: 0.75rem;
  color: var(--success-color);
  font-weight: 500;
}

.logout-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  border: none;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: var(--radius-xl);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 1rem;
    height: 70px;
  }
  
  .logo-text {
    display: none;
  }
  
  .nav-text {
    display: none;
  }
  
  .nav-item {
    padding: 0.75rem;
  }
  
  .user-info {
    display: none;
  }
  
  .user-profile {
    padding: 0.5rem;
  }
}

@media (max-width: 480px) {
  .nav-links {
    gap: 0.25rem;
    padding: 0.25rem;
  }
  
  .nav-item {
    padding: 0.5rem;
  }
}
</style>
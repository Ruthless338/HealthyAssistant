<!-- src/components/LoginRegister.vue -->
<template>
    <div class="login-register">
        <div class="background-decoration">
            <div class="floating-shape shape-1"></div>
            <div class="floating-shape shape-2"></div>
            <div class="floating-shape shape-3"></div>
        </div>
        
        <div class="auth-container glass">
            <div class="auth-box" :class="{active: isLogin}">
                <!-- 登录表单 -->
                <div class="auth-form login-form" v-if="isLogin">
                    <div class="form-header">
                        <div class="logo-section">
                            <div class="logo-icon">💪</div>
                            <h1 class="form-title gradient-text">欢迎回来</h1>
                        </div>
                        <p class="form-subtitle">继续你的健康之旅</p>
                    </div>
                    
                    <div class="form-content">
                        <div class="input-group">
                            <div class="input-wrapper">
                                <span class="input-icon">👤</span>
                                <input 
                                    type="text" 
                                    placeholder="用户名" 
                                    v-model="form.username"
                                    class="form-input"
                                >
                            </div>
                            <span class="error-tip" v-if="usernameError">用户名不存在</span>
                        </div>
                        
                        <div class="input-group">
                            <div class="input-wrapper">
                                <span class="input-icon">🔒</span>
                                <input 
                                    type="password" 
                                    placeholder="密码" 
                                    v-model="form.userpwd"
                                    class="form-input"
                                >
                            </div>
                            <span class="error-tip" v-if="passwordError">密码填写错误</span>
                        </div>
                        
                        <button class="submit-btn" @click="login">
                            <span class="btn-text">登录</span>
                            <span class="btn-icon">→</span>
                        </button>
                    </div>
                </div>
                
                <!-- 注册表单 -->
                <div class="auth-form register-form" v-else>
                    <div class="form-header">
                        <div class="logo-section">
                            <div class="logo-icon">🚀</div>
                            <h1 class="form-title gradient-text">开始旅程</h1>
                        </div>
                        <p class="form-subtitle">创建你的健康账户</p>
                    </div>
                    
                    <div class="form-content">
                        <div class="input-group">
                            <div class="input-wrapper">
                                <span class="input-icon">👤</span>
                                <input 
                                    type="text" 
                                    placeholder="用户名" 
                                    v-model="form.username"
                                    class="form-input"
                                >
                            </div>
                            <span class="error-tip" v-if="existed">用户名已经存在</span>
                        </div>
                        
                        <div class="input-group">
                            <div class="input-wrapper">
                                <span class="input-icon">🔒</span>
                                <input 
                                    type="password" 
                                    placeholder="密码" 
                                    v-model="form.userpwd"
                                    class="form-input"
                                >
                            </div>
                        </div>
                        
                        <button class="submit-btn" @click="register">
                            <span class="btn-text">注册</span>
                            <span class="btn-icon">→</span>
                        </button>
                    </div>
                </div>
            </div>
            
            <!-- 切换面板 -->
            <div class="switch-panel" :class="{active: isLogin}">
                <div class="switch-content" v-if="isLogin">
                    <div class="switch-header">
                        <h2 class="switch-title">你好，朋友！</h2>
                        <p class="switch-subtitle">还没有账户？立即注册开始你的健康之旅</p>
                    </div>
                    <button class="switch-btn" @click="changeType">
                        <span class="switch-icon">📝</span>
                        <span class="switch-text">注册账户</span>
                    </button>
                </div>
                
                <div class="switch-content" v-else>
                    <div class="switch-header">
                        <h2 class="switch-title">欢迎回来！</h2>
                        <p class="switch-subtitle">已有账户？登录继续你的健康计划</p>
                    </div>
                    <button class="switch-btn" @click="changeType">
                        <span class="switch-icon">🔑</span>
                        <span class="switch-text">登录账户</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import { useStore } from 'vuex';
export default {
    name: 'LoginRegister',
	setup() {
		const store = useStore();
		return {
			store
		};
	},
    data () {
        return {
            isLogin: false,
            emailError: false,
            passwordError: false,
            existed: false,
            form: {
                username: '',
                useremail: '',
                userpwd: ''
            }
        }
    },
    methods: {
        changeType () {
            this.isLogin = !this.isLogin;
            this.form.username = '';
            this.form.useremail = '';
            this.form.userpwd = '';
            // 清除错误状态
            this.usernameError = false;
            this.passwordError = false;
            this.existed = false;
        },
        login() {
			const self = this;
			if (self.form.username !== "" && self.form.userpwd !== "") {
				axios.post('http://localhost:8000/api/auth/login', {
					username: self.form.username,
					password: self.form.userpwd,
				})
				.then(res => {
					console.log("登录状态"+res.data.status);
					console.log(res.data.user);
					if (res.data.status === 200) {
						alert("登录成功！");
						//存储用户信息到Vuex store 或本地存储
						self.store.commit('setUser', {
								...res.data.user,
								isLoggedIn: true,
						});
						//登录后判断用户是否填写过身体基本信息以及兴趣、部位、目标，如果没有则跳转到填写页面
						if (res.data.user.interest.length > 0 && res.data.user.part.length > 0 &&res.data.user.goal.length > 0 ) {
							self.$router.push({name: 'Record'});
						} else {
							self.$router.push({name: 'Selector'});
						}
					} else {
						// 登录失败，用户名或密码错误
						alert("用户名或密码错误！");
					}
				})
				.catch(err => {
					console.log(err);
					// 捕获网络请求错误或后端抛出的异常
					if (err.response) {
						// 如果后端抛出了 HTTPException，通常会在 err.response.data 中返回错误信息
						alert("登录失败：" + err.response.data.message);
					} else {
						// 其他网络错误
						alert("登录失败：网络错误");
					}
					console.error(err);
				});
			} else {
				alert("填写不能为空！");
			}
		},
        register () {
            const self = this;
            if(self.form.username != ""  && self.form.userpwd != "") {
                axios.post('http://localhost:8000/api/auth/register', {
						username: self.form.username,
						password: self.form.userpwd
                })
                .then(
                    res => {
                        if(res.data.status === 200){
							alert("注册成功！");
							this.isLogin = true;
						} else {
							this.existed = true;
							alert("用户名已存在");
						}
                    }
                )
                .catch(
                    err => {
					// 捕获网络请求错误或后端抛出的异常
					if (err.response) {
						// 如果后端抛出了 HTTPException，通常会在 err.response.data 中返回错误信息
						alert("注册失败：" + err.response.data.message);
					} else {
						// 其他网络错误
						alert("登录失败：网络错误");
					}
					console.error(err);
				}
                )
            } else {
                alert("填写不能为空！");
            }
        }
    }
}
</script>

<style scoped>
.login-register {
    width: 100vw;
    height: 100vh;
    position: fixed;
    top: 0;
    left: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    overflow: hidden;
}

.background-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
}

.floating-shape {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 6s ease-in-out infinite;
}

.shape-1 {
    width: 100px;
    height: 100px;
    top: 20%;
    left: 10%;
    animation-delay: 0s;
}

.shape-2 {
    width: 150px;
    height: 150px;
    top: 60%;
    right: 15%;
    animation-delay: 2s;
}

.shape-3 {
    width: 80px;
    height: 80px;
    bottom: 20%;
    left: 20%;
    animation-delay: 4s;
}

@keyframes float {
    0%, 100% { transform: translateY(0px) rotate(0deg); }
    50% { transform: translateY(-20px) rotate(180deg); }
}

.auth-container {
    width: 900px;
    height: 600px;
    border-radius: var(--radius-2xl);
    display: flex;
    overflow: hidden;
    box-shadow: var(--shadow-xl);
    position: relative;
    z-index: 1;
}

.auth-box {
    flex: 1;
    padding: 3rem;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.auth-form {
    width: 100%;
    max-width: 400px;
}

.form-header {
    text-align: center;
    margin-bottom: 2.5rem;
}

.logo-section {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    margin-bottom: 1rem;
}

.logo-icon {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    box-shadow: var(--shadow-md);
}

.form-title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0;
    letter-spacing: -0.025em;
}

.form-subtitle {
    color: var(--text-secondary);
    font-size: 1rem;
    margin: 0;
}

.form-content {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.input-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
}

.input-icon {
    position: absolute;
    left: 1rem;
    font-size: 1.125rem;
    color: var(--text-secondary);
    z-index: 1;
}

.form-input {
    width: 100%;
    padding: 1rem 1rem 1rem 3rem;
    border: 2px solid var(--border-color);
    border-radius: var(--radius-lg);
    font-size: 1rem;
    background: var(--bg-primary);
    transition: all 0.3s ease;
    color: var(--text-primary);
}

.form-input:focus {
    outline: none;
    border-color: var(--primary-color);
    box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.error-tip {
    color: var(--error-color);
    font-size: 0.875rem;
    font-weight: 500;
    padding-left: 0.5rem;
}

.submit-btn {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: var(--radius-lg);
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    margin-top: 1rem;
}

.submit-btn:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
    background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
}

.btn-icon {
    font-size: 1.125rem;
    transition: transform 0.3s ease;
}

.submit-btn:hover .btn-icon {
    transform: translateX(4px);
}

.switch-panel {
    width: 400px;
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 3rem;
    position: relative;
    overflow: hidden;
}

.switch-panel::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
    opacity: 0.3;
}

.switch-content {
    text-align: center;
    color: white;
    position: relative;
    z-index: 1;
}

.switch-header {
    margin-bottom: 2rem;
}

.switch-title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 1rem;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.switch-subtitle {
    font-size: 1rem;
    margin: 0;
    opacity: 0.9;
    line-height: 1.6;
}

.switch-btn {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border: 2px solid rgba(255, 255, 255, 0.3);
    padding: 1rem 2rem;
    border-radius: var(--radius-xl);
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    backdrop-filter: blur(10px);
}

.switch-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    border-color: rgba(255, 255, 255, 0.5);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
}

.switch-icon {
    font-size: 1.125rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .auth-container {
        width: 90%;
        height: auto;
        min-height: 500px;
        flex-direction: column;
    }
    
    .auth-box {
        padding: 2rem 1.5rem;
    }
    
    .switch-panel {
        width: 100%;
        padding: 2rem 1.5rem;
    }
    
    .form-title {
        font-size: 1.75rem;
    }
    
    .switch-title {
        font-size: 1.75rem;
    }
}

@media (max-width: 480px) {
    .auth-container {
        width: 95%;
        margin: 1rem;
    }
    
    .auth-box {
        padding: 1.5rem 1rem;
    }
    
    .switch-panel {
        padding: 1.5rem 1rem;
    }
    
    .logo-icon {
        width: 50px;
        height: 50px;
        font-size: 1.25rem;
    }
    
    .form-title {
        font-size: 1.5rem;
    }
    
    .switch-title {
        font-size: 1.5rem;
    }
}
</style>
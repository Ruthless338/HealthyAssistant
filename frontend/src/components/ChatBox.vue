<template>
  <div class="chatbox-container glass">
    <div class="chat-header">
      <span class="chat-title">AI健康助理(回答依据您的运动记录和实际问题)</span>
    </div>
    <div class="chat-messages" ref="messagesRef">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        :class="['chat-bubble', msg.role === 'user' ? 'user' : 'ai']"
      >
        <span v-if="msg.role === 'user'" class="avatar user-avatar">🧑</span>
        <span v-else class="avatar ai-avatar">🤖</span>
        <div class="bubble-content">
          <span v-html="msg.content"></span>
        </div>
      </div>
      <div v-if="loading" class="chat-bubble ai loading">
        <span class="avatar ai-avatar">🤖</span>
        <div class="bubble-content">
          <span class="dot-flashing"></span>
        </div>
      </div>
    </div>
    <div class="chat-input-bar">
      <input
        v-model="input"
        @keyup.enter="sendMessage"
        :disabled="loading"
        placeholder="请输入您的健康问题..."
        class="chat-input"
      />
      <button @click="sendMessage" :disabled="loading || !input.trim()" class="send-btn">
        发送
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import axios from 'axios'
import { useStore } from 'vuex'

const store = useStore()
const messages = ref([
  { role: 'ai', content: '您好，我是您的AI健康助理，有什么可以帮您？' }
])
const input = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const sendMessage = async () => {
  if (!input.value.trim() || loading.value) return
  const userMsg = { role: 'user', content: input.value }
  messages.value.push(userMsg)
  loading.value = true
  const question = input.value
  input.value = ''
  await nextTick()
  scrollToBottom()

  try {
    const response = await axios.post('http://localhost:8000/api/record/ai-chat', {
      username: store.state.user.username,
      message: question
    })
    messages.value.push({ role: 'ai', content: response.data.reply || 'AI未能理解您的问题。' })
  } catch (e) {
    messages.value.push({ role: 'ai', content: '网络错误，请稍后重试。' })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.chatbox-container {
  max-width: 420px;
  margin: 2rem auto;
  border-radius: 1.5rem;
  box-shadow: var(--shadow-xl);
  display: flex;
  flex-direction: column;
  height: 600px;
  background: rgba(255,255,255,0.7);
  overflow: hidden;
}
.chat-header {
  padding: 1rem 1.5rem;
  background: linear-gradient(90deg, var(--primary-color), var(--secondary-color));
  color: #fff;
  font-weight: 700;
  font-size: 1.2rem;
  letter-spacing: 1px;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  background: linear-gradient(135deg, #f8fafc 60%, #e0f7fa 100%);
}
.chat-bubble {
  display: flex;
  align-items: flex-end;
  margin-bottom: 1.2rem;
}
.chat-bubble.user {
  flex-direction: row-reverse;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6rem;
  margin: 0 0.5rem;
}
.user-avatar {
  background: linear-gradient(135deg, #4ecdc4, #45b7d1);
  color: #fff;
}
.ai-avatar {
  background: linear-gradient(135deg, #6366f1, #38bdf8);
  color: #fff;
}
.bubble-content {
  max-width: 70%;
  padding: 0.9rem 1.2rem;
  border-radius: 1.2rem;
  font-size: 1rem;
  line-height: 1.7;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  word-break: break-all;
}
.chat-bubble.user .bubble-content {
  background: linear-gradient(135deg, #e0f7fa 60%, #b2ebf2 100%);
  color: #222;
  border-bottom-right-radius: 0.3rem;
}
.chat-bubble.ai .bubble-content {
  background: linear-gradient(135deg, #f3f4f6 60%, #e0e7ef 100%);
  color: #222;
  border-bottom-left-radius: 0.3rem;
}
.chat-input-bar {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  background: #f8fafc;
  border-top: 1px solid #e0e7ef;
}
.chat-input {
  flex: 1;
  border: none;
  border-radius: 1rem;
  padding: 0.8rem 1.2rem;
  font-size: 1rem;
  background: #fff;
  margin-right: 1rem;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.send-btn {
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: #fff;
  border: none;
  border-radius: 1rem;
  padding: 0.7rem 1.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.dot-flashing {
  position: relative;
  width: 1.2em;
  height: 0.4em;
  background: none;
}
.dot-flashing:before, .dot-flashing:after, .dot-flashing {
  content: '';
  display: inline-block;
  width: 0.4em;
  height: 0.4em;
  border-radius: 50%;
  background: #6366f1;
  animation: dotFlashing 1s infinite linear alternate;
  margin-right: 0.2em;
}
.dot-flashing:after {
  animation-delay: 0.5s;
}
@keyframes dotFlashing {
  0% { opacity: 0.2; }
  50% { opacity: 1; }
  100% { opacity: 0.2; }
}
</style> 
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: localStorage.getItem('user_token') || '', profile: JSON.parse(localStorage.getItem('user_profile') || 'null') }),
  actions: {
    setAuth(data) {
      this.token = data.token
      this.profile = data.profile
      localStorage.setItem('user_token', data.token)
      localStorage.setItem('user_profile', JSON.stringify(data.profile || {}))
    },
    clear() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('user_token')
      localStorage.removeItem('user_profile')
    }
  }
})

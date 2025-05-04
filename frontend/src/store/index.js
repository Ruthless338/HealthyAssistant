// src/store/index.js
import { createStore } from 'vuex';

export default createStore({
    state: {
        user: {
            isLoggedIn: false,
            id: -1,
            username: '',
            password:'',
            avatar: '',
            age: 0,
            height: 0.0,
            weight: 0.0,
            gender: '',
            goal: [],
            interest: [],
            part: []
        }
    },
    mutations: {
        setUser(state, user) {
            state.user = user;
        },
        logout(state) {
            state.user.isLoggedIn = false;
            state.user.id = -1;
            state.user.username = '';
            state.user.avatar = '';
            state.user.age = 0;
            state.user.height = 0.0;
            state.user.weight = 0.0;
            state.user.gender = '';
            state.user.goal = [];
            state.user.interest = [];
            state.user.part = [];
        }
    }
});
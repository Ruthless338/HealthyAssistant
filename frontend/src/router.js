import { createRouter, createWebHistory } from 'vue-router';
import Plan from './views/Plan.vue';
import Record from './views/Record.vue';
import Community from './views/Community.vue';
import LoginRegister from './components/LoginRegister.vue';
import Selector from './views/Selector.vue';
import UserProfile from './views/UserProfile.vue';
import Recommendations from './views/Recommendations.vue';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/Community'
        },
        {
            path:'/Plan',
            name:'Plan',
            component:Plan
        },
        {
            path:'/Record',
            name:'Record',
            component:Record
        },
        {
            path:'/Community',
            name:'Community',
            component:Community
        },
        {
            path:'/LoginRegister',
            name:'LoginRegister',
            component:LoginRegister
        },
        {
            path:'/Selector',
            name:'Selector',
            component:Selector
        },
        {
            path:'/UserProfile',
            name:'UserProfile',
            component:UserProfile
        },
        {
            path: '/Recommendations',
            name: 'Recommendations',
            component: Recommendations
        }
    ]
})

export default router;
import { createRouter, createWebHistory } from 'vue-router';
import Plan from '.views/Plan.vue';
import Record from '.views/Record.vue';
import Diet from '.views/Diet.vue';
import Community from './views/Community.vue';

const router = createRouter({
    history: createWebHistory(),
    routes: [
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
            path:'/Diet',
            name:'Diet',
            component:Diet
        },
        {
            path:'/Community',
            name:'Community',
            component:Community
        }
    ]
})

export default router;
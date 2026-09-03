import { createApp } from "vue";
import { createPinia } from "pinia";
import "./style.css";
import App from "./App.vue";
import router from "./router";
import AppNav from "./components/AppNav.vue";
import InlineAlert from "./components/InlineAlert.vue";

const app = createApp(App);

app.component("AppNav", AppNav);
app.component("InlineAlert", InlineAlert);
app.use(createPinia());
app.use(router);

app.mount("#app");

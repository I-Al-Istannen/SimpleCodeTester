<template>
  <v-app id="app">
    <navigation-bar :actionsHidden="actionsHidden"></navigation-bar>
    <v-content>
      <v-container fluid fill-height>
        <!-- Key is set so the component will be recreated when the path changes. -->
        <!-- The profile view needs this to fetch the correct data -->
        <router-view :key="$route.fullPath" @hide-nav-bar-actions="handleNavBarActionsVisibilityChange"/>
      </v-container>
    </v-content>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue } from "vue-property-decorator";
import NavigationBar from "./components/NavigationBar.vue";
import "material-design-icons-iconfont/dist/material-design-icons.css";

@Component({
  components: {
    "navigation-bar": NavigationBar
  }
})
export default class App extends Vue {
  private actionsHidden = false;

  handleNavBarActionsVisibilityChange(hidden: boolean) {
    this.actionsHidden = hidden;
  }
}
</script>

<style>
* {
  --primary: #4caf50;
  --primary-darken-1: #2d9437;
}

.v-toolbar__title {
  font-weight: 500;
}
</style>

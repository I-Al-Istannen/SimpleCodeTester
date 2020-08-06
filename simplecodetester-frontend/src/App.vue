<template>
  <v-app id="app">
    <navigation-bar :actionsHidden="actionsHidden"></navigation-bar>
    <v-main>
      <v-container fluid fill-height>
        <!-- Key is set so the component will be recreated when the path changes. -->
        <!-- The profile view needs this to fetch the correct data -->
        <router-view
          :key="$route.fullPath"
          @hide-nav-bar-actions="handleNavBarActionsVisibilityChange"
        />
      </v-container>
    </v-main>
    <theme-selector @useDarkTheme="setDarkTheme"></theme-selector>
  </v-app>
</template>

<script lang="ts">
import { Component, Vue, Watch } from "vue-property-decorator";
import NavigationBar from "./components/NavigationBar.vue";
import { Store } from "vuex";
import { RootState } from "./store/types";
import ThemeSelector from "./components/ThemeSelector.vue";

@Component({
  components: {
    "navigation-bar": NavigationBar,
    "theme-selector": ThemeSelector
  }
})
export default class App extends Vue {
  private actionsHidden = false;

  handleNavBarActionsVisibilityChange(hidden: boolean) {
    this.actionsHidden = hidden;
  }

  created() {
    this.$vuetify.theme.dark = this.isDarkTheme;
  }

  private get typedStore() {
    return this.$store as Store<RootState>;
  }

  private setDarkTheme(darkTheme: boolean) {
    this.typedStore.commit("user/darkThemeSelected", darkTheme);
  }

  @Watch("isDarkTheme")
  private onDarkThemeChanged() {
    this.$vuetify.theme.dark = this.isDarkTheme;
  }

  private get isDarkTheme() {
    return this.typedStore.state.user.darkThemeSelected();
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

/* I liked the old vuetify layout for side icons better */
.v-btn__content > .v-icon--right {
  height: 24px !important;
  width: 24px !important;
  font-size: 24px !important;
  line-height: 24px !important;
  margin-left: 16px !important;
  margin-right: 0px !important;
}
</style>

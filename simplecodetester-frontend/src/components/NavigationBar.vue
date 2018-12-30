<template>
  <nav v-if="!hidden">
    <v-toolbar
      dark
      color="primary darken-1"
      class="hidden-sm-and-down"
    >
      <v-toolbar-title>{{title}}</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items v-if="!actionsHidden">
        <v-btn
          flat
          @click="logout"
        >
          Logout
          <v-icon
            right
            dark
          >exit_to_app</v-icon>
        </v-btn>
      </v-toolbar-items>
    </v-toolbar>

    <!-- MOBILE -->
    <v-list
      dark
      id="nav-list"
      class="hidden-md-and-up elevation-6"
    >
      <v-list-group>
        <v-list-tile slot="activator">
          <v-list-tile-content>
            <v-list-tile-title id="main-title">{{title}}</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>

        <v-list-tile @click="logout">
          <v-list-tile-content>
            <v-list-tile-title>
              <v-icon class="mx-3">exit_to_app</v-icon>
              Logout
            </v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list-group>
    </v-list>
  </nav>
</template>

<script lang="ts">
import Vue from "vue";
export default Vue.extend({
  name: "NavigationBar",
  data: function() {
    return {
      title: "Simple Code Tester",
      hidden: false
    };
  },
  methods: {
    logout: function() {
      this.$store.commit("logout");
      this.$router.push("/login");
    }
  },
  props: {
    actionsHidden: Boolean
  }
});
</script>

<style scoped>
#nav-list {
  background-color: var(--primary-darken-1);
}

#main-title {
  font-size: 20px;
  font-weight: 500;
}
</style>
<template>
  <nav v-if="!hidden">
    <v-toolbar dark color="primary darken-1" class="hidden-sm-and-down">
      <v-toolbar-title>{{title}}</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-toolbar-items v-if="!actionsHidden">
        <!-- PROFILE -->
        <v-btn flat v-if="currentRoute.name !== 'Profile'" @click="$router.push('/profile')">Profile
          <v-icon right dark>person</v-icon>
        </v-btn>

        <!-- CHECK CODE -->
        <v-btn
          flat
          v-if="currentRoute.name !== 'Check Code'"
          @click="$router.push('/check-code')"
        >Check code
          <v-icon right dark>star_half</v-icon>
        </v-btn>

        <!-- LOGOUT -->
        <v-btn flat @click="logout">Logout
          <v-icon right dark>exit_to_app</v-icon>
        </v-btn>
      </v-toolbar-items>
    </v-toolbar>

    <!-- MOBILE -->
    <v-list dark id="nav-list" class="hidden-md-and-up elevation-6">
      <v-list-group>
        <v-list-tile slot="activator">
          <v-list-tile-content>
            <v-list-tile-title id="main-title">{{title}}</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>

        <!-- PROFILE -->
        <v-list-tile v-if="currentRoute.name !== 'Profile'" @click="$router.push('/profile')">
          <v-list-tile-content>
            <v-list-tile-title>
              <v-icon class="mx-3">person</v-icon>Profile
            </v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>

        <!-- CHECK-CODE -->
        <v-list-tile v-if="currentRoute.name !== 'Check Code'" @click="$router.push('/check-code')">
          <v-list-tile-content>
            <v-list-tile-title>
              <v-icon class="mx-3">star_half</v-icon>Check code
            </v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>

        <!-- LOGOUT -->
        <v-list-tile @click="logout">
          <v-list-tile-content>
            <v-list-tile-title>
              <v-icon class="mx-3">exit_to_app</v-icon>Logout
            </v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list-group>
    </v-list>
  </nav>
</template>

<script lang="ts">
import Vue from "vue";
import { Store } from "vuex";
import { RootState } from "@/store/types";
import { Route } from "vue-router";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";

@Component({})
export default class NavigationBar extends Vue {
  title = "Simple Code Tester";
  hidden = false;

  @Prop()
  actionsHidden!: Boolean;

  get currentRoute(): Route {
    return this.$route;
  }
  logout() {
    this.$store.dispatch("logout");
    this.$router.push("/login");
  }
}
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
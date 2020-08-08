<template>
  <nav>
    <v-toolbar dark color="primary darken-1">
      <v-app-bar-nav-icon v-if="!actionsHidden" @click="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title class="d-flex flex-centered">
        {{ title }}
        <a
          v-if="!actionsHidden"
          href="https://github.com/I-Al-Istannen/SimpleCodeTester"
          class="pl-4 py-0"
          style="line-height: 0px;"
          target="_blank"
          rel="noopener"
        >
          <img :src="require('@/assets/Github_Mark.png')" width="32" height="32" />
        </a>
      </v-toolbar-title>

      <v-spacer></v-spacer>
      <div v-if="!actionsHidden" class="hidden-sm-and-down">
        <!-- PROFILE -->
        <v-btn text v-if="currentRoute.name !== 'profile'" :to="{ name: 'profile' }">
          Profile
          <v-icon right dark>{{ profileIcon }}</v-icon>
        </v-btn>

        <!-- CHECK CODE -->
        <v-btn text v-if="currentRoute.name !== 'checkCode'" :to="{ name: 'checkCode' }">
          Check code
          <v-icon right dark>{{ checkCodeIcon }}</v-icon>
        </v-btn>

        <!-- LOGOUT -->
        <v-btn text @click="logout">
          Logout
          <v-icon right dark>{{ logoutIcon }}</v-icon>
        </v-btn>
      </div>
    </v-toolbar>

    <!-- DRAWER -->
    <v-navigation-drawer v-model="drawer" app temporary v-if="!actionsHidden">
      <v-toolbar dark color="primary darken-1">
        <v-list>
          <v-list-item>
            <v-list-item-title class="title">Navigation</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-toolbar>

      <v-divider></v-divider>

      <v-list dense class="pt-0">
        <v-list-item
          v-for="item in applicableItems"
          :key="item.title"
          :to="item.route"
          :class="{selected: item.predicatePath === currentRoute.name, headline: true}"
        >
          <v-list-item-icon>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-icon>

          <v-list-item-content>
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
  </nav>
</template>

<script lang="ts">
import Vue from "vue";
import { Store } from "vuex";
import { RootState } from "@/store/types";
import { Route } from "vue-router";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";
import {
  mdiAccount,
  mdiAccountMultiple,
  mdiExitToApp,
  mdiFormatListBulleted,
  mdiHistory,
  mdiLock,
  mdiPlusCircleOutline,
  mdiShape,
  mdiStarHalf
} from "@mdi/js";

@Component({})
export default class NavigationBar extends Vue {
  title = "Simple Code Tester";
  drawer = false;

  // ================ Icons ====================
  private addCheckIcon = mdiPlusCircleOutline;
  private checkCodeIcon = mdiStarHalf;
  private profileIcon = mdiAccount;
  private lastCheckResultIcon = mdiHistory;
  private allChecksIcon = mdiFormatListBulleted;
  private checkCategoryIcon = mdiShape;
  private usersIcon = mdiAccountMultiple;
  private changePasswordIcon = mdiLock;
  private logoutIcon = mdiExitToApp;
  // ===========================================

  navigationItems = [
    {
      icon: this.profileIcon,
      title: "Profile",
      route: "/profile",
      predicatePath: "profile"
    },
    {
      icon: this.checkCodeIcon,
      title: "Check Code",
      route: "/check-code",
      predicatePath: "checkCode"
    },
    {
      icon: this.lastCheckResultIcon,
      title: "View last check result",
      route: "/view-check-result",
      predicatePath: "viewCheckResult",
      statefulIf: function(state: RootState): boolean {
        return state.checkresult.checkResult !== null;
      }
    },
    {
      icon: this.addCheckIcon,
      title: "Submit check",
      route: "/submit-check",
      predicatePath: "submitCheck"
    },
    {
      icon: this.allChecksIcon,
      title: "All checks",
      route: "/view-checks",
      predicatePath: "viewChecks"
    },
    {
      icon: this.checkCategoryIcon,
      title: "Manage check categories",
      route: "/view-check-categories",
      predicatePath: "viewCheckCategories",
      admin: true
    },
    {
      icon: this.usersIcon,
      title: "Manage Users",
      route: "/view-users",
      predicatePath: "viewUsers",
      admin: true
    },
    {
      icon: this.changePasswordIcon,
      title: "Change password",
      route: "/change-own-password",
      predicatePath: "changePassword"
    },
    {
      icon: this.logoutIcon,
      title: "Logout",
      route: "/logout",
      predicatePath: "login"
    }
  ];

  @Prop()
  actionsHidden!: Boolean;

  get currentRoute(): Route {
    return this.$route;
  }

  get applicableItems(): Array<any> {
    return this.navigationItems.filter(it => {
      // only for admins and you are none
      if (it.admin && (it.admin && !this.isAdmin)) {
        return false;
      }
      // does not have further restrictions
      if (!it.statefulIf) {
        return true;
      }
      return it.statefulIf(this.$store.state);
    });
  }

  get isAdmin(): boolean {
    const userState = (this.$store as Store<RootState>).state.user;
    return userState.roles && userState.roles.some(it => it === "ROLE_ADMIN");
  }

  logout() {
    this.$store.dispatch("logout");
    this.$router.push("/login");
  }

  navigate(item: any) {
    if (item.title === "Logout") {
      this.logout();
    } else {
      this.$router.push(item.route);
    }
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

<style>
.v-list__tile {
  font-size: 15px !important;
}
.flex-centered {
  justify-content: center;
  align-items: center;
}
</style>

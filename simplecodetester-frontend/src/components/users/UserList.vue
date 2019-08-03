<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Manage Users</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            :append-icon="searchIcon"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-iterator
            :items="users.users"
            :footer-props="footerProps"
            :search="search"
            :items-per-page="rowsPerPage"
            @update:items-per-page="setRowsPerPage"
          >
            <template v-slot:default="props">
              <v-layout column>
                <v-expansion-panels multiple popout>
                  <v-expansion-panel
                    :readonly="item.roles.length === 0"
                    v-for="item in props.items"
                    :key="item.id"
                  >
                    <v-expansion-panel-header class="d-flex flex-container py-0">
                      <span class="subtitle-1">
                        <span class="font-weight-medium">{{ item.displayName }}</span>
                        ({{ item.id }})
                      </span>
                      <user-modify-actions
                        class="aside"
                        @error="setError"
                        :user="item"
                        :users="users"
                      ></user-modify-actions>
                    </v-expansion-panel-header>
                    <v-expansion-panel-content>
                      <div v-if="item.roles.length > 0" class="ma-2 mx-4">
                        <v-chip
                          v-for="role in item.roles"
                          :key="role"
                          disabled
                          color="accent"
                          outlined
                        >{{ role }}</v-chip>
                      </div>
                    </v-expansion-panel-content>
                  </v-expansion-panel>
                </v-expansion-panels>
              </v-layout>
            </template>
          </v-data-iterator>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-dialog v-model="addDialogOpened" max-width="700">
            <template v-slot:activator="{ on }">
              <v-btn color="primary" v-on="on">
                Add user
                <v-icon dark right>{{ addPersonIcon }}</v-icon>
              </v-btn>
            </template>
            <new-user
              ref="addUserModification"
              :users="users"
              @close="addDialogOpened = false"
              @user="addUser"
            ></new-user>
          </v-dialog>
          <v-spacer></v-spacer>
        </v-card-actions>
        <v-alert type="error" :value="error.length > 0">{{ error }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import { Users, UserToAdd } from "@/components/users/Users";
import { extractErrorMessage } from "@/util/requests";
import Component from "vue-class-component";
import UserModificationComponent from "@/components/users/UserModificationComponent.vue";
import UserModifyActions from "@/components/users/UserModifyActions.vue";
import { Watch } from "vue-property-decorator";
import { RootState } from "@/store/types";
import { mdiAccountPlus, mdiMagnify } from "@mdi/js";

@Component({
  components: {
    "new-user": UserModificationComponent,
    "user-modify-actions": UserModifyActions
  }
})
export default class UserList extends Vue {
  private search: string = "";
  private error: string = "";
  private users: Users = new Users();
  private footerProps = {
    itemsPerPageOptions: [4, 10, 20, 50, 100],
    pagination: {
      rowsPerPage: this.rowsPerPage
    }
  };
  private addDialogOpened = false;

  get rowsPerPage(): number {
    return (this.$store.state as RootState).miscsettings.itemsPerPage;
  }

  @Watch("footerProps.pagination.rowsPerPage", { deep: true })
  setRowsPerPage(rows: number) {
    this.$store.commit("miscsettings/setItemsPerPage", rows);
  }

  setError(error: string) {
    this.error = error;
  }

  addUser(user: UserToAdd) {
    this.addDialogOpened = false;
    (this.$refs["addUserModification"] as UserModificationComponent).clear();
    this.handlePromise(this.users.addItem(user));
  }

  mounted() {
    this.handlePromise(this.users.fetchAll());
  }

  handlePromise(promise: Promise<any>) {
    promise.catch(error => this.setError(extractErrorMessage(error)));
  }

  // ICONS
  private searchIcon = mdiMagnify;
  private addPersonIcon = mdiAccountPlus;
}
</script>

<style scoped>
.flex-container {
  justify-content: space-between;
  align-items: center;
}
.flex-container > .aside {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.v-chip {
  opacity: 1;
}
</style>

<style>
.v-expansion-panel__header {
  padding-top: 5px;
  padding-bottom: 5px;
}
</style>

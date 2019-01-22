<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Manage Users</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            append-icon="search"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-iterator
            :items="users.users"
            :rows-per-page-items="rowsPerPageItems"
            :pagination.sync="pagination"
            :search="search"
            content-tag="v-layout"
            column
            wrap
          >
            <v-flex slot="item" slot-scope="props" xs12 sm6 md4 lg3>
              <v-expansion-panel>
                <v-expansion-panel-content>
                  <div slot="header" class="d-flex flex-container">
                    <span class="subheading">
                      <span class="font-weight-medium">{{ props.item.displayName }}</span>
                      ({{ props.item.id }})
                    </span>
                    <user-modify-actions
                      class="aside"
                      @error="setError"
                      :user="props.item"
                      :users="users"
                    ></user-modify-actions>
                  </div>
                  <div v-if="props.item.roles.length > 0" class="ma-2 mx-4">
                    <v-chip
                      v-for="role in props.item.roles"
                      :key="role"
                      disabled
                      color="accent"
                      outline
                    >{{ role }}</v-chip>
                  </div>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-flex>
          </v-data-iterator>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-dialog v-model="addDialogOpened" max-width="700">
            <v-btn color="primary" slot="activator">Add user
              <v-icon dark right>person_add</v-icon>
            </v-btn>
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
import { Users, User, UserToAdd } from "@/components/users/Users";
import { extractErrorMessage } from "@/util/requests";
import Component from "vue-class-component";
import UserModificationComponent from "@/components/users/UserModificationComponent.vue";
import UserModifyActions from "@/components/users/UserModifyActions.vue";
import { Watch } from "vue-property-decorator";

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
  private rowsPerPageItems = [4, 10, 20, 50, 100];
  private pagination = {
    rowsPerPage: 10
  };
  private addDialogOpened = false;

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
</style>

<style>
.v-expansion-panel__header {
  padding-top: 5px;
  padding-bottom: 5px;
}
</style>

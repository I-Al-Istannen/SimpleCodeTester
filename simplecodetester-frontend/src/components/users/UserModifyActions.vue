<template>
  <span>
    <span v-if="!user.enabled" class="subheading disabled mr-4">(Disabled)</span>
    <v-menu offset-y>
      <v-btn slot="activator" icon>
        <v-icon>edit</v-icon>
      </v-btn>
      <v-list>
        <v-list-tile @click="blackhole">
          <v-list-tile-title v-if="!user.enabled" @click="setEnabled(user, true)">Enable user</v-list-tile-title>
          <v-list-tile-title v-else @click="setEnabled(user,false)">Disable user</v-list-tile-title>
        </v-list-tile>
        <v-dialog v-model="changeDialogOpened" max-width="600">
          <v-list-tile slot="activator" @click="blackhole">
            <v-list-tile-title>Change password</v-list-tile-title>
          </v-list-tile>
          <change-password @input="submitPasswordChange" :canSubmit="!requestPending"></change-password>
        </v-dialog>
      </v-list>
    </v-menu>
    <v-btn class="ma-0" icon @click="deleteUser(user)">
      <v-icon color="#FF6347">delete</v-icon>
    </v-btn>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import { Prop, Component } from "vue-property-decorator";
import { Users, User, UserToAdd } from "@/components/users/Users";
import { extractErrorMessage } from "@/util/requests";
import ChangePassword from "@/components/users/ChangePassword.vue";
import Axios from "axios";

@Component({
  components: {
    "change-password": ChangePassword
  }
})
export default class UserModifyActions extends Vue {
  private changeDialogOpened = false;
  private requestPending = false;

  @Prop()
  private user!: User;

  @Prop()
  private users!: Users;

  setEnabled(user: User, enabled: boolean) {
    this.handlePromise(this.users.setEnabled(user, enabled));
  }

  deleteUser(user: User) {
    if (confirm(`Do you really want to delete ${user.id}?`)) {
      this.handlePromise(this.users.deleteUser(user));
    }
  }

  handlePromise(promise: Promise<any>) {
    promise.catch(error => this.$emit("error", extractErrorMessage(error)));
  }

  submitPasswordChange(password: string) {
    this.requestPending = true;

    const data = new FormData();
    data.append("newPassword", password);
    data.append("userId", this.user.id);

    Axios.post("/admin/set-password", data)
      .then(response => {
        this.changeDialogOpened = false;
      })
      .catch(error => {
        this.$emit("error", extractErrorMessage(error));
      })
      .finally(() => {
        this.requestPending = false;
      });
  }

  blackhole() {}
}
</script>

<style scoped>
</style>


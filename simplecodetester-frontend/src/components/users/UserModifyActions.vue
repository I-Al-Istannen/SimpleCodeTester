<template>
  <crud-modify-actions
    ref="modify-actions"
    @requestFinished="requestPending = false"
    :repository="users"
    :element="user"
  >
    <span slot="preActions" v-show="!user.enabled" class="subtitle-1 disabled mr-4">(Disabled)</span>

    <template slot="customMenuActions">
      <!-- Prevent clicks from triggering twice for some godforsaken reason (the click.stop) -->
      <v-list-item @click.stop="blackhole">
        <v-list-item-title v-if="!user.enabled" @click="setEnabled(user, true)">Enable user</v-list-item-title>
        <v-list-item-title v-else @click="setEnabled(user,false)">Disable user</v-list-item-title>
      </v-list-item>

      <!-- Prevent clicks from triggering twice and instantly closing the dialog (the click.stop) -->
      <v-list-item @click.stop="blackhole">
        <v-dialog hide-overlay v-model="changeDialogOpened" max-width="600">
          <template v-slot:activator="{ on }">
            <v-list-item-title v-on="on">Change password</v-list-item-title>
          </template>
          <change-password @input="submitPasswordChange" :canSubmit="!requestPending"></change-password>
        </v-dialog>
      </v-list-item>

      <!-- Prevent clicks from triggering twice and instantly closing the dialog (the click.stop) -->
      <v-list-item @click.stop="blackhole">
        <v-dialog hide-overlay v-model="editDialogOpened" max-width="600">
          <template v-slot:activator="{ on }">
            <v-list-item-title v-on="on">Edit user</v-list-item-title>
          </template>
          <edit-user
            @close="editDialogOpened = false"
            :users="users"
            :user="userCopy"
            @user="submitUserChange"
            :canSubmit="!requestPending"
            editing="true"
          ></edit-user>
        </v-dialog>
      </v-list-item>
    </template>
  </crud-modify-actions>
</template>

<script lang="ts">
import Vue from "vue";
import { Component, Prop } from "vue-property-decorator";
import { User, Users, UserToAdd } from "@/components/users/Users";
import { extractErrorMessage } from "@/util/requests";
import ChangePassword from "@/components/users/ChangePassword.vue";
import CrudModifyActions from "@/components/crud/CrudModifyActions.vue";
import UserModificationComponent from "@/components/users/UserModificationComponent.vue";

@Component({
  components: {
    "change-password": ChangePassword,
    "crud-modify-actions": CrudModifyActions,
    "edit-user": UserModificationComponent
  }
})
export default class UserModifyActions extends Vue {
  private changeDialogOpened = false;
  private editDialogOpened = false;
  private requestPending = false;

  @Prop()
  private user!: User;

  @Prop()
  private users!: Users;

  get userCopy() {
    return new User(
      this.user.id,
      this.user.displayName,
      this.user.enabled,
      this.user.roles
    );
  }

  setEnabled(user: User, enabled: boolean) {
    this.handlePromise(this.users.setEnabled(user, enabled));
  }

  handlePromise(promise: Promise<any>) {
    promise
      .catch(error => this.$emit("error", extractErrorMessage(error)))
      .finally(() => (this.requestPending = false));
  }

  submitPasswordChange(password: string) {
    this.requestPending = true;
    this.changeDialogOpened = false;

    const userToAdd = new UserToAdd(
      this.user.id,
      this.user.displayName,
      this.user.roles,
      password
    );
    this.$emit("updated", userToAdd);

    (this.$refs["modify-actions"] as CrudModifyActions<
      User,
      UserToAdd
    >).updateElement(userToAdd);
  }

  submitUserChange(user: UserToAdd) {
    this.requestPending = true;
    this.editDialogOpened = false;

    (this.$refs["modify-actions"] as CrudModifyActions<
      User,
      UserToAdd
    >).updateElement(user);
  }

  blackhole() {}
}
</script>

<style scoped>
.disabled {
  color: #ff6347;
}
</style>


<template>
  <v-layout justify-center align-center>
    <v-flex xs12 sm8 md6 class="elevation-10">
      <change-password @input="submitChange" :canSubmit="!requestPending">
        <v-toolbar color="primary" dark slot="header">
          <v-toolbar-title>Change your password</v-toolbar-title>
        </v-toolbar>
        <v-alert slot="endOfCard" :type="messageType" :value="message.length > 0">{{ message }}</v-alert>
      </change-password>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import ChangePassword from "@/components/users/ChangePassword.vue";
import Axios from "axios";
import { extractErrorMessage } from "@/util/requests";

@Component({
  components: {
    "change-password": ChangePassword
  }
})
export default class ChangeOwnPassword extends Vue {
  private message = "";
  private messageType = "error";
  private requestPending = false;

  submitChange(password: string) {
    this.requestPending = true;

    const data = new FormData();
    data.append("newPassword", password);

    Axios.post("/set-own-password", data)
      .then(response => {
        this.messageType = "success";
        this.message = "Successfully changed your password!";
      })
      .catch(error => {
        this.messageType = "error";
        this.message = extractErrorMessage(error);
      })
      .finally(() => {
        this.requestPending = false;
      });
  }
}
</script>

<style scoped>
</style>

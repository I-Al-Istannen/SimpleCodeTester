<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Login</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-form v-model="formValid">
            <v-text-field
              @keydown.enter.prevent="login"
              prepend-icon="person"
              name="login"
              label="Name"
              type="text"
              v-model="username"
              :rules="[notEmpty]"
            ></v-text-field>
            <v-text-field
              @keydown.enter.prevent="login"
              prepend-icon="lock"
              name="password"
              label="Passwort"
              type="password"
              v-model="password"
              :rules="[notEmpty]"
            ></v-text-field>
          </v-form>
          <p class="error-message" :display="error.length !== 0">{{ error }}</p>
        </v-card-text>
        <v-card-actions class="pr-3 pb-3">
          <v-spacer></v-spacer>
          <v-btn color="primary" @click="login" :disabled="!formValid">Login</v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-flex>
    <v-card></v-card>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import { UserLoginInfo } from "@/store/types";
import { extractErrorMessage } from "../util/requests";
import { AxiosError } from "axios";
import Component from "vue-class-component";

@Component({})
export default class Login extends Vue {
  private formValid = true;
  private username = "";
  private password = "";
  private error = "";

  /**
   * Tries to log the user in and redirects/sets errors.
   */
  login() {
    if (!this.formValid) {
      return;
    }

    this.$store
      .dispatch("user/login", new UserLoginInfo(this.username, this.password))
      .catch((error: AxiosError) => {
        this.error = extractErrorMessage(error);
      })
      .then(value => {
        this.$router.push("/profile");
      });
  }

  /**
   * Enforces that the input is not empty and returns an appropriate error message.
   *
   * @param input the input to check
   */
  notEmpty(input: string): string | boolean {
    return input.length == 0 ? "This field can not be empty." : true;
  }

  /**
   * Lifecycle method, hide nav buttons for this view
   */
  created() {
    this.$emit("hide-nav-bar-actions", true);
  }

  /**
   * Lifecycle method, show nav buttons for other views
   */
  destroyed() {
    this.$emit("hide-nav-bar-actions", false);
  }
}
</script>

<style scoped>
.error-message {
  color: red;
  font-size: 1.2rem;
  text-align: center;
}
</style>

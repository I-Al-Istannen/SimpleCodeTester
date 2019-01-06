<template>
  <v-card>
    <slot name="header"></slot>
    <v-card-text>
      <v-form v-model="formValid" ref="form">
        <v-text-field
          label="Password"
          type="password"
          prepend-icon="lock"
          v-model="password"
          :rules="[notEmpty, passwordsMatch]"
        ></v-text-field>
        <v-text-field
          label="Confirm password"
          type="password"
          prepend-icon="lock"
          v-model="confirmPassword"
          :rules="[notEmpty, passwordsMatch]"
        ></v-text-field>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        color="primary"
        :disabled="!formValid || !canSubmit"
        @click="changePassword"
      >Change password</v-btn>
      <v-spacer></v-spacer>
    </v-card-actions>
    <slot name="endOfCard"></slot>
  </v-card>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Watch, Prop } from "vue-property-decorator";

@Component
export default class ChangePassword extends Vue {
  private formValid = false;
  private password = "";
  private confirmPassword = "";

  @Prop()
  private canSubmit!: boolean;

  @Watch("password")
  onPasswordChange() {
    (this.$refs["form"] as any).validate();
  }

  @Watch("confirmPassword")
  onPasswordConfirmChange() {
    (this.$refs["form"] as any).validate();
  }

  passwordsMatch() {
    return this.password === this.confirmPassword
      ? true
      : "The passwords to not match!";
  }

  notEmpty(input: string) {
    return input.length > 0 ? true : "This field can not be empty!";
  }

  changePassword() {
    if (this.password !== this.confirmPassword) {
      return;
    }
    this.$emit("input", this.password);
  }
}
</script>


<style scoped>
</style>

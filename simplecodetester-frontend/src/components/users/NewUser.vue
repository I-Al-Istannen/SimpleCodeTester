<template>
  <v-card>
    <v-toolbar color="primary" dark>
      <v-toolbar-title>Add user</v-toolbar-title>
    </v-toolbar>
    <v-card-text>
      <v-form v-model="formValid">
        <v-text-field :rules="[idRule]" label="Id" v-model="id"></v-text-field>
        <v-text-field label="Display name" v-model="displayName"></v-text-field>
        <v-text-field label="Password" v-model="password" type="password"></v-text-field>
        <v-textarea label="Roles (one per line)" v-model="roleString"></v-textarea>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn :disabled="!formValid" color="primary" class="mr-3" @click="emitUser">Submit</v-btn>
      <v-btn color="red" class="ml-3" dark @click="$emit('close')">Close</v-btn>
      <v-spacer></v-spacer>
    </v-card-actions>
  </v-card>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Watch, Prop } from "vue-property-decorator";
import { User, UserToAdd, Users } from "@/components/users/Users";

@Component
export default class NewUser extends Vue {
  private id: string = "";
  private displayName: string = "";
  private password: string = "";
  private roles: Array<string> = [];
  private formValid = true;

  @Prop()
  private users!: Users;

  @Prop()
  private editing!: boolean;

  get roleString() {
    return this.roles.join("\n");
  }

  set roleString(input: string) {
    this.roles = input.split("\n");
  }

  idRule(input: string) {
    if (this.editing) {
      return true;
    }
    return this.users.users.some(it => it.id == input)
      ? "A user with that Id exists already!"
      : true;
  }

  emitUser() {
    this.$emit(
      "user",
      new UserToAdd(this.id, this.displayName, this.roles, this.password)
    );
  }
}
</script>

<style scoped>
</style>

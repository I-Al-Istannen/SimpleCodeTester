<template>
  <v-card>
    <v-toolbar color="primary" dark>
      <v-toolbar-title v-if="editing">Edit user</v-toolbar-title>
      <v-toolbar-title v-else>Add user</v-toolbar-title>
    </v-toolbar>
    <v-card-text>
      <v-form v-model="formValid">
        <v-text-field v-if="!editing" :rules="[idRule]" label="Id" v-model="user.id"></v-text-field>
        <v-text-field label="Display name" v-model="user.displayName"></v-text-field>
        <v-text-field
          v-if="editing"
          label="Password (leave blank to leave unchanged)"
          type="password"
        ></v-text-field>
        <v-text-field v-else label="Password" v-model="user.password" type="password"></v-text-field>
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
export default class UserModificationComponent extends Vue {
  private formValid = true;

  @Prop()
  private users!: Users;

  @Prop()
  private editing!: boolean;

  @Prop({ default: () => new UserToAdd("", "", [], "") })
  private user!: UserToAdd;

  get roleString() {
    return this.user.roles.join("\n");
  }

  set roleString(input: string) {
    this.user.roles = input.split("\n");
  }

  public clear() {
    this.user = new UserToAdd("", "", [], "");
  }

  idRule(input: string) {
    if (this.editing) {
      return true;
    }
    return this.users.users.some(it => it.id == input)
      ? "A user with that id exists already!"
      : true;
  }

  emitUser() {
    this.$emit("user", this.user);
  }
}
</script>

<style scoped>
</style>

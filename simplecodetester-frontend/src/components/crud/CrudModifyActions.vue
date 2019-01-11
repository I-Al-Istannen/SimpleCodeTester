<template>
  <span>
    <slot name="preActions"></slot>
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
import Component from "vue-class-component";

@Component
export default class CrudModifyActions extends Vue {}
</script>


<style scoped>
</style>

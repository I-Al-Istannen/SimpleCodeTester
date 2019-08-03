<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title class="headline">You appear to be '{{ displayName }}'.</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <p class="text-center subtitle-1 mb-3">This is your current data:</p>
          <template>
            <v-data-table :headers="headers" :items="items" class="elevation-1" hide-default-footer>
              <template v-slot:header.name="{ header }">
                <span class="title">{{ header.text }}</span>
              </template>
              <template v-slot:header.value="{ header }">
                <span class="title">{{ header.text }}</span>
              </template>

              <template v-slot:body="{ items }">
                <tbody>
                  <tr v-for="item in items" :key="item.name">
                    <td class="subtitle-1 text-center">{{ item.title }}</td>
                    <td class="subtitle-1 text-center">{{ item.value }}</td>
                  </tr>
                </tbody>
              </template>
            </v-data-table>
          </template>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" class="mr-4" :to="{ name: 'checkCode' }">
            Check code
            <v-icon right dark>{{ checkCodeIcon }}</v-icon>
          </v-btn>

          <v-btn color="primary" class="ml-4" :to="{ name: 'submitCheck' }">
            Submit check
            <v-icon right dark>{{ submitCheckIcon }}</v-icon>
          </v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import { RootState } from "@/store/types";
import { Store } from "vuex";
import Component from "vue-class-component";
import { mdiCloudUpload, mdiPlusCircleOutline } from "@mdi/js";

@Component
export default class Profile extends Vue {
  private headers = [
    { text: "Name", value: "name", align: "center" },
    { text: "Value", value: "value", align: "center" }
  ];

  get store(): Store<RootState> {
    return this.$store as Store<RootState>;
  }
  get userName() {
    return this.store.state.user.userName;
  }
  get displayName() {
    return this.store.state.user.displayName;
  }

  get items() {
    return [
      {
        title: "ID",
        value: this.store.state.user.userName
      },
      {
        title: "Roles",
        value: this.store.state.user.roles.join(", ")
      }
    ];
  }

  // Icons
  private checkCodeIcon = mdiCloudUpload;
  private submitCheckIcon = mdiPlusCircleOutline;
}
</script>

<style scoped>
</style>

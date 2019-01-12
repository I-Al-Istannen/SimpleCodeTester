<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title class="headline">You appear to be '{{ displayName }}'.</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <p class="text-xs-center subheading mb-3">This is your current data:</p>
          <template>
            <v-data-table :headers="headers" :items="items" class="elevation-1" hide-actions>
              <template slot="headerCell" slot-scope="props">
                <span class="title">{{ props.header.text }}</span>
              </template>

              <template slot="items" slot-scope="props">
                <td class="subheading text-xs-center">{{ props.item.title }}</td>
                <td class="subheading text-xs-center">{{ props.item.value }}</td>
              </template>
            </v-data-table>
          </template>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" class="mr-4" @click="checkCode">Check code
            <v-icon right dark>star_half</v-icon>
          </v-btn>

          <v-btn color="primary" class="ml-4" @click="submitCheck">Submit check
            <v-icon right dark>add_circle_outline</v-icon>
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

  submitCheck() {
    this.$router.push("/submit-check");
  }
  checkCode() {
    this.$router.push("/check-code");
  }
}
</script>

<style scoped>
</style>

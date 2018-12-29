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
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import { RootState } from "@/store/types";
import { Store } from "vuex";
export default Vue.extend({
  data: function() {
    return {
      headers: [
        { text: "Name", value: "name", align: "center" },
        { text: "Value", value: "value", align: "center" }
      ]
    };
  },
  computed: {
    items: function() {
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
    },
    store: function(): Store<RootState> {
      return this.$store as Store<RootState>;
    },
    userName: function() {
      return this.store.state.user.userName;
    },
    displayName: function() {
      return this.store.state.user.displayName;
    }
  }
});
</script>

<style scoped>
</style>

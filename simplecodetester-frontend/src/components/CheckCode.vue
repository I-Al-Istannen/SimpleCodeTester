<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit code to check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-tabs slider-color="accent">
            <v-tab ripple>Paste source</v-tab>
            <v-tab-item class="flex">
              <highlighted-code v-model="code"></highlighted-code>
            </v-tab-item>

            <v-tab ripple>File upload</v-tab>
            <v-tab-item>Item 2</v-tab-item>
          </v-tabs>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import HighlightedCode from "./highlighting/HighlightedCode.vue";

@Component({
  components: {
    "highlighted-code": HighlightedCode
  }
})
export default class CheckCode extends Vue {
  private code: string = "hello world";
}
</script>

<style scoped>
</style>

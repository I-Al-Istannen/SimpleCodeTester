<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit code to check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>Paste source</v-tab>
            <v-tab-item class="flex">
              <highlighted-code v-model="code"></highlighted-code>
            </v-tab-item>

            <v-tab ripple>File upload</v-tab>
            <v-tab-item>Item 2</v-tab-item>
          </v-tabs>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" ripple @click="upload">
            Upload {{ selectedTab }}
            <v-icon right dard>cloud_upload</v-icon>
          </v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
        <v-alert type="error" :value="error.length > 0">{{ error }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import HighlightedCode from "./highlighting/HighlightedCode.vue";
import Axios from "axios";
import { extractErrorMessage } from "@/util/requests";
import { CheckResultState } from "@/store/types";
import { Store } from "vuex";

@Component({
  components: {
    "highlighted-code": HighlightedCode
  }
})
export default class CheckCode extends Vue {
  private code: string = "Nothing set";
  private error: string = "";

  private selectedTab: Number = 0;

  upload() {
    if (this.selectedTab == 0) {
      this.uploadSource();
    } else {
      this.uploadFile();
    }
  }

  uploadSource() {
    this.$store
      .dispatch("checkresult/checkSingle", this.code)
      .then(response => this.$router.push("/view-check-result"))
      .catch(error => {
        this.error = extractErrorMessage(error);
      });
  }

  uploadFile() {}
}
</script>

<style scoped>
</style>

<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit code to check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <check-category-selection @input="setCategory"></check-category-selection>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>File upload</v-tab>
            <v-tab-item>
              <multi-file-select @input="filesSelected"></multi-file-select>
            </v-tab-item>

            <v-tab ripple>Paste source</v-tab>
            <v-tab-item class="flex">
              <highlighted-code v-model="code"></highlighted-code>
            </v-tab-item>
          </v-tabs>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn :disabled="uploading || !uploadPossible" color="primary" ripple @click="upload">
            Upload {{ selectedTab == 0 ? "files" : "source" }}
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
import MultiFileSelect from "./upload/MultiFileSelect.vue";
import Axios, { AxiosPromise } from "axios";
import { extractErrorMessage } from "@/util/requests";
import {
  CheckResultState,
  RootState,
  CheckCategory,
  Pair
} from "@/store/types";
import { Store } from "vuex";
import CheckCategorySelection from "@/components/CheckCategorySelection.vue";

@Component({
  components: {
    "highlighted-code": HighlightedCode,
    "multi-file-select": MultiFileSelect,
    "check-category-selection": CheckCategorySelection
  }
})
export default class CheckCode extends Vue {
  private code: string = "";
  private files: Array<File> = [];
  private error: string = "";
  private uploading: boolean = false;
  private checkCategory: CheckCategory | null = null;

  private selectedTab: Number = 0;

  get uploadPossible() {
    const inputProvided =
      (this.code.length > 0 && this.selectedTab === 1) ||
      (this.files.length > 0 && this.selectedTab === 0);
    return inputProvided && this.checkCategory;
  }

  setCategory(checkCategory: CheckCategory) {
    this.checkCategory = checkCategory;
  }

  upload() {
    if (this.selectedTab == 1) {
      this.uploadSource();
    } else {
      this.uploadFile();
    }
  }

  uploadSource() {
    this.uploading = true;
    this.handleUploadResult(
      this.$store.dispatch(
        "checkresult/checkSingle",
        new Pair(this.checkCategory, this.code)
      )
    );
  }

  private handleUploadResult(promise: AxiosPromise<any>) {
    promise
      .then(response => this.$router.push("/view-check-result"))
      .catch(error => {
        this.error = extractErrorMessage(error);
      })
      .finally(() => (this.uploading = false));
  }

  uploadFile() {
    this.uploading = true;

    const zipFile = this.files.find(elem => {
      return elem.name.endsWith(".zip");
    });

    if (zipFile) {
      this.handleUploadResult(
        this.$store.dispatch(
          "checkresult/checkZip",
          new Pair(this.checkCategory, zipFile)
        )
      );
    } else {
      this.handleUploadResult(
        this.$store.dispatch(
          "checkresult/checkMultiple",
          new Pair(this.checkCategory, this.files)
        )
      );
    }
  }

  filesSelected(files: Array<File>) {
    this.files = files;
  }
}
</script>

<style scoped>
</style>

<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit code to check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <check-category-selection :checkCategory="checkCategory" @input="setCategory"></check-category-selection>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>File upload</v-tab>
            <v-tab-item>
              <multi-file-select @input="filesSelected"></multi-file-select>
            </v-tab-item>

            <v-tab ripple>Paste source</v-tab>
            <v-tab-item class="flex">
              <v-textarea v-model="code"></v-textarea>
            </v-tab-item>
          </v-tabs>
        </v-card-text>
        <v-card-actions class="sticky-bottom">
          <v-spacer></v-spacer>
          <v-btn :disabled="uploading || !uploadPossible" color="primary" ripple @click="upload">
            Upload {{ selectedTab == 0 ? "files" : "source" }}
            <v-icon right dard>{{ uploadIcon }}</v-icon>
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
import MultiFileSelect from "./upload/MultiFileSelect.vue";
import { AxiosPromise } from "axios";
import { extractErrorMessage } from "@/util/requests";
import { CheckCategory, Pair, RootState } from "@/store/types";
import CheckCategorySelection from "@/components/CheckCategorySelection.vue";
import { mdiCloudUpload } from "@mdi/js";

@Component({
  components: {
    "multi-file-select": MultiFileSelect,
    "check-category-selection": CheckCategorySelection
  }
})
export default class CheckCode extends Vue {
  private code: string = "";
  private files: Array<File> = [];
  private error: string = "";
  private uploading: boolean = false;

  private selectedTab: Number = 0;

  get uploadPossible() {
    const inputProvided =
      (this.code.length > 0 && this.selectedTab === 1) ||
      (this.files.length > 0 && this.selectedTab === 0);
    return inputProvided && this.checkCategory;
  }

  get checkCategory() {
    return (this.$store.state as RootState).miscsettings.category;
  }

  setCategory(checkCategory: CheckCategory | null) {
    this.$store.commit("miscsettings/setCategory", checkCategory);
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
      .then(() => this.$router.push("/view-check-result"))
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

  // ICONS
  private uploadIcon = mdiCloudUpload;
}
</script>

<style scoped>
.sticky-bottom {
  position: sticky;
  bottom: 0px;
}
</style>

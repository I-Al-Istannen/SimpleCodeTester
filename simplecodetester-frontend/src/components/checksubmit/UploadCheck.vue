<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit a new check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <check-category-selection :checkCategory="checkCategory" @input="setCategory"></check-category-selection>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>Input-Output check</v-tab>
            <v-tab-item>
              <io-check v-model="ioCheck"></io-check>
            </v-tab-item>
          </v-tabs>

          <check-submit-error-dialog
            @close="displayDialog = false"
            :problems="problems"
            :displayed="displayDialog"
          ></check-submit-error-dialog>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            :disabled="uploading || !uploadPossible"
            color="primary"
            ripple
            @click="upload"
          >Upload I/O check
            <v-icon right dard>cloud_upload</v-icon>
          </v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
        <v-alert
          class="title"
          :type="feedbackMessageType"
          :value="feedbackMessage.length > 0"
        >{{ feedbackMessage }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import Axios, { AxiosPromise, AxiosError } from "axios";
import { extractErrorMessage } from "@/util/requests";
import HighlightedCode from "@/components/highlighting/HighlightedCode.vue";
import CheckSubmitErrorDialogVue from "@/components/checksubmit/CheckSubmitErrorDialog.vue";
import IOCheckComponent from "@/components/checksubmit/IOCheckComponent.vue";
import { CheckCategoryState, CheckCategory, RootState } from "@/store/types";
import { Store } from "vuex";
import CheckCategorySelection from "@/components/CheckCategorySelection.vue";
import { IOCheck } from "@/components/checklist/CheckTypes";

@Component({
  components: {
    "highlighted-code": HighlightedCode,
    "check-submit-error-dialog": CheckSubmitErrorDialogVue,
    "io-check": IOCheckComponent,
    "check-category-selection": CheckCategorySelection
  }
})
export default class UploadCheck extends Vue {
  private uploading = false;
  private feedbackMessage = "";
  private feedbackMessageType = "error";
  private problems: Array<string> = [];
  private displayDialog: boolean = false;
  private selectedTab = 0;

  private ioCheck: IOCheck | null = null;

  get uploadPossible() {
    const dataEntered = this.ioCheck && this.ioCheck.name.length > 0;
    return dataEntered && this.checkCategory;
  }

  get checkCategory(): CheckCategory | null {
    return (this.$store.state as RootState).miscsettings.category;
  }

  setCategory(checkCategory: CheckCategory) {
    this.$store.commit("miscsettings/setCategory", checkCategory);
  }

  upload() {
    this.uploading = true;
    this.uploadIOCheck();
  }

  uploadIOCheck() {
    const check: any = {
      data: this.ioCheck,
      name: this.ioCheck!.name
    };

    this.handleUploadResult(
      Axios.post(`/checks/add/${this.checkCategory!.id}`, {
        value: JSON.stringify(check),
        class: "InterleavedStaticIOCheck"
      })
    );
  }

  handleUploadResult(promise: AxiosPromise<any>) {
    promise
      .then(response => {
        this.feedbackMessage = "Your check has the ID " + response.data.id;
        if (!response.data.approved) {
          this.feedbackMessage +=
            ". Your check is currently unapproved and will not be run.";
        }
        this.feedbackMessageType = "success";
      })
      .catch((error: AxiosError) => {
        if (
          error.response &&
          error.response.data &&
          error.response.data.output
        ) {
          const diags = error.response.data.output.diagnostics;
          const fileName = Object.keys(diags)[0];
          this.problems = diags[fileName] as Array<string>;

          this.displayDialog = true;

          this.feedbackMessage = "";
          return;
        }

        this.feedbackMessage = extractErrorMessage(error);
        this.feedbackMessageType = "error";
      })
      .finally(() => (this.uploading = false));
  }
}
</script>

<style scoped>
</style>

<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Submit a new check</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>Paste source</v-tab>
            <v-tab-item class="flex">
              <highlighted-code v-model="code"></highlighted-code>
            </v-tab-item>

            <v-tab ripple>Input-Output check</v-tab>
            <v-tab-item>
              <v-text-field label="Check name" v-model="ioName"></v-text-field>
              <v-textarea label="Input. Hit enter for a new line." v-model="ioInput"></v-textarea>
              <v-textarea label="Expected output. Hit enter for a new line." v-model="ioOutput"></v-textarea>
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
          <v-btn :disabled="uploading || !uploadPossible" color="primary" ripple @click="upload">
            Upload {{ selectedTab == 0 ? "source" : "I/O check" }}
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

@Component({
  components: {
    "highlighted-code": HighlightedCode,
    "check-submit-error-dialog": CheckSubmitErrorDialogVue
  }
})
export default class UploadCheck extends Vue {
  private uploading = false;
  private feedbackMessage = "";
  private feedbackMessageType = "error";
  private problems: Array<string> = [];
  private displayDialog: boolean = false;
  private code = "";
  private selectedTab = 0;

  private ioInput = "";
  private ioOutput = "";
  private ioName = "";

  get uploadPossible() {
    return (
      (this.code.length > 0 && this.selectedTab === 0) ||
      (this.selectedTab == 1 &&
        (this.ioInput.length >= 0 && this.ioOutput.length >= 0))
    );
  }

  upload() {
    this.uploading = true;
    if (this.selectedTab === 0) {
      this.uploadSource();
    } else {
      this.uploadIOCheck();
    }
  }

  uploadSource() {
    this.handleUploadResult(
      Axios.post("/checks/add", this.code, {
        headers: { "Content-Type": "text/plain" }
      })
    );
  }

  uploadIOCheck() {
    const formData = new FormData();
    formData.append("input", this.ioInput);
    formData.append("output", this.ioOutput);
    formData.append("name", this.ioName);
    this.handleUploadResult(Axios.post("/checks/add-io", formData));
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

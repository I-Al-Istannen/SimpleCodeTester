<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <v-card class="elevation-10">
        <v-toolbar dark color="primary">
          <v-toolbar-title class="mr-4">Submit a new public check</v-toolbar-title>
          <submit-help></submit-help>
        </v-toolbar>
        <v-card-text>
          <check-category-selection :checkCategory="checkCategory" @input="setCategory"></check-category-selection>
          <v-tabs slider-color="accent" v-model="selectedTab">
            <v-tab ripple>Input-Output check</v-tab>
            <v-tab-item>
              <io-check
                fileUploadPrompt="Test-Datei hinzufügen (nicht deine Lösung!!)"
                v-model="ioCheck"
              ></io-check>
            </v-tab-item>
          </v-tabs>

          <check-submit-error-dialog
            @close="displayDialog = false"
            :problems="problems"
            :displayed="displayDialog"
          ></check-submit-error-dialog>
        </v-card-text>
        <v-card-actions class="sticky-bottom">
          <v-spacer></v-spacer>
          <v-btn :disabled="uploading || !uploadPossible" color="primary" ripple @click="upload">
            Upload I/O check
            <v-icon right dard>{{ uploadIcon }}</v-icon>
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
import Axios, { AxiosError, AxiosPromise } from "axios";
import { extractErrorMessage } from "@/util/requests";
import CheckSubmitErrorDialogVue from "@/components/checksubmit/CheckSubmitErrorDialog.vue";
import IOCheckComponent from "@/components/checksubmit/IOCheckComponent.vue";
import { CheckCategory, RootState } from "@/store/types";
import CheckCategorySelection from "@/components/CheckCategorySelection.vue";
import { IOCheck } from "@/components/checklist/CheckTypes";
import { mdiCloudUpload } from "@mdi/js";
import SubmitHelpDialog from "./SubmitHelpDialog.vue";

@Component({
  components: {
    "check-submit-error-dialog": CheckSubmitErrorDialogVue,
    "io-check": IOCheckComponent,
    "check-category-selection": CheckCategorySelection,
    "submit-help": SubmitHelpDialog
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
    if (
      !window.confirm(
        "Hey, du bist dabei einen öffentlichen Test zu erstellen.\n" +
          "Danke dir! :) " +
          "\n\nBitte gehe noch einmal sicher, dass du keine privaten " +
          "Daten oder Lösungen hochlädst.\n\n" +
          "Falls du dies gerade machst, klicke bitte auf 'Abbrechen'.\n" +
          "Ansonsten klicke auf 'OK'. Vielen Dank :)"
      )
    ) {
      return;
    }
    this.uploading = true;
    this.uploadIOCheck();
  }

  uploadIOCheck() {
    this.handleUploadResult(
      Axios.post(`/checks/add/${this.checkCategory!.id}`, {
        payload: this.ioCheck!.serializeForSending("InterleavedStaticIOCheck"),
        files: this.ioCheck!.files
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

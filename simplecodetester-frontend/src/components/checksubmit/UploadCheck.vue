<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
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
              <v-dialog v-model="ioHelpOpened" max-width="700" class="help-dialog">
                <template v-slot:activator="{ on }">
                  <v-btn v-on="on" icon>
                    <v-icon color="accent" large>{{ helpIcon }}</v-icon>
                  </v-btn>
                </template>
                <v-card>
                  <v-toolbar dark color="primary">
                    <v-toolbar-title>Help</v-toolbar-title>
                  </v-toolbar>
                  <v-card-text class="body-1">
                    <h3 class="title pb-3">Required fields</h3>

                    <ul>
                      <li>
                        <span class="body-2">Check category:</span>
                        <br />The category of the check, typically the assignment.
                      </li>
                      <li>
                        <span class="body-2">Check name:</span>
                        <br />The name of the check
                      </li>
                      <li>
                        <span class="body-2">Check data:</span>
                        <br/>The actual data of the check.
                        <p>It consists of a few different parts:
                          <ul class="mb-3">
                            <li>Input lines (prefixed with <span class="literal">> </span>).
                              The single space after the ">" is <em>important!</em>
                            </li>
                            <li>Regular output lines (no prefix)</li>
                            <li>Error output lines (prefixed with <span class="literal">&lt;e</span>) that match any error</li>
                            <li>Regular expression output lines (prefixed with <span class="literal">&lt;r</span>)
                              that match a regular expression.
                              <br>The regular expression is anchored at front and end, so it needs to match the whole line.
                            </li>
                            <li>Parameter lines (prefixed with <span class="literal">$$ </span>).
                              The single space after the "$$" is <em>important!</em> Note that the codetester will not split
                              at spaces. Each line is a SINGLE entry in the "args" parameter in your java program.
                            </li>
                            <li>Literal output lines (prefixed with <span class="literal">&lt;l</span>) that match whatever
                              is after the <span class="literal">&lt;l</span> literally.
                              <br>You can use them if the program needs to output <span class="literal">&lt;e</span>, for example.
                            </li>
                            <li>Comments (prefixed with <span class="literal">#</span>) that are ignored, but help organize it.</li>
                          </ul>
                          <span class="body-2">Example:</span><br>
                          <pre class="example">
                            # Set a parameter. Will not split at spaces!
                            $$ I am a parameter!
                            > start
                            OK
                            # Can not start an already started game
                            > start
                            &lt;e
                            > abort
                            > abort
                            &lt;e
                            > quit
                          </pre>
                        </p>
                      </li>
                    </ul>
                  </v-card-text>
                  <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="primary" @click="ioHelpOpened = false">Close</v-btn>
                    <v-spacer></v-spacer>
                  </v-card-actions>
                </v-card>
              </v-dialog>
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
import HighlightedCode from "@/components/highlighting/HighlightedCode.vue";
import CheckSubmitErrorDialogVue from "@/components/checksubmit/CheckSubmitErrorDialog.vue";
import IOCheckComponent from "@/components/checksubmit/IOCheckComponent.vue";
import { CheckCategory, RootState } from "@/store/types";
import CheckCategorySelection from "@/components/CheckCategorySelection.vue";
import { IOCheck } from "@/components/checklist/CheckTypes";
import { mdiCloudUpload, mdiHelpCircleOutline } from "@mdi/js";

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
  private ioHelpOpened = false;

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

  // ICONS
  private helpIcon = mdiHelpCircleOutline;
  private uploadIcon = mdiCloudUpload;
}
</script>

<style scoped>
.help-dialog {
  display: flex !important;
  justify-content: flex-end !important;
}
.literal {
  padding: 0px 5px 0px 5px;
  font-family: monospace;
  white-space: pre;
  background-color: rgba(128, 128, 128, 0.2);
}
.example {
  white-space: pre-line;
}
.sticky-bottom {
  position: sticky;
  bottom: 0px;
}
</style>

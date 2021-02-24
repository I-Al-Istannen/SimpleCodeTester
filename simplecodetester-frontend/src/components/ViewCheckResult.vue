<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <v-card class="elevation-12 pb-2">
        <v-toolbar dark :color="allPassed && items.length > 0 ? 'primary' : '#ff6347'">
          <v-toolbar-title>
            Check results
            <span v-if="!allPassed">({{ failCount }} classes failed)</span>
            <span v-if="items.length == 0">(No tests run)</span>
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn v-if="!showTimes" text @click="onShowTimes">Show execution time</v-btn>
        </v-toolbar>
        <v-card-title v-if="items.length === 0">
          <h2
            class="headline"
          >No tests were run against your code. Are you sure it has a main class?</h2>
        </v-card-title>
        <v-card-text class="scrollable-container">
          <v-expansion-panels multiple accordion v-model="failureBooleanArray">
            <v-expansion-panel v-for="(item, i) in items" :key="i">
              <v-expansion-panel-header class="monospaced">
                <v-icon v-if="item.successful" color="green">{{ successfulIcon }}</v-icon>
                <v-icon v-else color="#ff6347">{{ failedIcon }}</v-icon>
                Class '{{ item.fileName }}'
                <v-spacer v-if="showTimes"></v-spacer>
                <span
                  v-if="item.totalDuration && showTimes"
                  class="duration-tag"
                >Sum: ~{{ fudgeDuration(item.totalDuration) }} ms</span>
              </v-expansion-panel-header>
              <v-expansion-panel-content>
                <v-card>
                  <v-card-text class="check-background">
                    <!-- Inner panel -->
                    <v-expansion-panels multiple accordion class="elevation-4">
                      <v-expansion-panel v-for="(result, i) in item.results" :key="i">
                        <v-expansion-panel-header class="monospaced">
                          <v-icon v-if="!result.failed()" color="green">{{ successfulIcon }}</v-icon>
                          <v-icon v-else color="#ff6347">{{ failedIcon }}</v-icon>
                          Check '{{ result.check }}'
                          <v-spacer v-if="showTimes"></v-spacer>
                          <span v-if="result.durationMillis && showTimes" class="duration-tag">
                            <span
                              :class="[ result.durationMillis > 2 * item.averageDuration ? 'above-average' : '' ]"
                            >{{ fudgeDuration(result.durationMillis) }}</span> ms
                          </span>
                        </v-expansion-panel-header>
                        <v-expansion-panel-content>
                          <v-card>
                            <v-card-text class="check-background">
                              <div>
                                <v-btn
                                  v-if="result.output.length > 0"
                                  outlined
                                  class="elevation-1 mb-4 ml-0 mr-4"
                                  color="highlighted_button_color"
                                  @click="copyFullInput(result, findButtonOnCopy($event.srcElement))"
                                >Copy full input</v-btn>
                                <v-btn
                                  v-if="result.failed() && result.output.length > 0"
                                  outlined
                                  class="elevation-1 mb-4 mr-4"
                                  color="highlighted_button_color"
                                  @click="copyInputUntilError(result, findButtonOnCopy($event.srcElement))"
                                >Copy input until error</v-btn>
                                <v-btn
                                  v-if="result.output.length > 0"
                                  outlined
                                  class="elevation-1 mb-4"
                                  color="highlighted_button_color"
                                  @click="copyFullOutput(result, findButtonOnCopy($event.srcElement))"
                                >Copy full output</v-btn>
                              </div>
                              <pre class="monospaced" v-if="result.message">{{ result.message }}</pre>
                              <pre class="monospaced" v-if="result.errorOutput">{{ result.errorOutput }}</pre>
                              <highlighted-code
                                v-if="result.output.length > 0"
                                :io-lines="result.output"
                                readonly
                              ></highlighted-code>
                              <display-files-component
                                v-if="result.files.length > 0"
                                :value="result.files"
                                :editable="false"
                              ></display-files-component>
                            </v-card-text>
                          </v-card>
                        </v-expansion-panel-content>
                      </v-expansion-panel>
                    </v-expansion-panels>
                    <!-- End of inner panel -->
                  </v-card-text>
                </v-card>
              </v-expansion-panel-content>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-card-text>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Store } from "vuex";
import {
  CheckResultType,
  FileCheckResult,
  IoLine,
  IoLineType,
  RootState
} from "@/store/types";
import { mdiCheckCircleOutline, mdiCloseCircleOutline } from "@mdi/js";
import TextfieldFileAddComponent from "./checksubmit/TextfieldFileAddComponent.vue";
import HighlightedCode from "./highlighting/HighlightedCode.vue";

class SingleFileResult {
  fileName: string;
  results: Array<FileCheckResult>;
  successful: boolean;
  totalDuration: number;
  averageDuration: number;

  constructor(
    fileName: string,
    results: Array<FileCheckResult>,
    successful: boolean
  ) {
    this.fileName = fileName;
    this.results = results;
    this.successful = successful;

    this.totalDuration = this.results
      .map(it => it.durationMillis || 0)
      .reduce((acc, next) => acc + next);
    this.averageDuration = this.totalDuration / this.results.length;

    // Pull up failed checks, sort alphabetically inside the groups
    this.results.sort((a, b) => {
      if (
        a.result !== CheckResultType.FAILED &&
        b.result === CheckResultType.FAILED
      ) {
        return 1;
      } else if (
        a.result === CheckResultType.FAILED &&
        b.result !== CheckResultType.FAILED
      ) {
        return -1;
      }
      return a.check.localeCompare(b.check);
    });
  }
}

@Component({
  components: {
    "display-files-component": TextfieldFileAddComponent,
    "highlighted-code": HighlightedCode
  }
})
export default class Test extends Vue {
  private items: Array<SingleFileResult> = [];
  private showTimes: boolean = false;

  get allPassed() {
    return this.items.every(elem => elem.successful);
  }

  get failCount() {
    return this.items.filter(elem => !elem.successful).length;
  }

  get failureBooleanArray() {
    return Array.from(this.items)
      .slice()
      .map(it => !it.successful);
  }
  // We just ignore it, as we  don't need it
  set failureBooleanArray(array: Array<boolean>) {}

  findButtonOnCopy(element: HTMLElement) {
    if (element.tagName !== "BUTTON") {
      return element.parentElement!!;
    }
    return element;
  }

  private onShowTimes() {
    const promptMessage =
      "Only DESIGN and FUNCTIONALITY are relevant for this module.\n\n" +
      "Please DO NOT OPTIMIZE your code for performance and DO NOT COMPARE your times with others.\n\n" +
      "These times are just here to help you debug timeouts.";

    if (window.confirm(promptMessage)) {
      this.showTimes = true;
    }
  }

  private fudgeDuration(millis: number) {
    if (millis < 30) {
      return "<30";
    }
    return Math.ceil(millis / 20) * 20;
  }

  copyFullInput(result: FileCheckResult, element: HTMLElement) {
    let input = result.output
      .filter(line => line.lineType === IoLineType.INPUT)
      .map(line => line.content)
      .map(line => line.replace(/^> /, ""))
      .join("\n");
    this.copyText(input, element);
  }

  copyInputUntilError(result: FileCheckResult, element: HTMLElement) {
    let linesUntilError: Array<IoLine> = [];

    for (let i = 0; i < result.output.length; i++) {
      if (result.output[i].lineType === IoLineType.ERROR) {
        break;
      }
      linesUntilError.push(result.output[i]);
    }
    // do not include the output producing the error
    linesUntilError.pop();

    let input = linesUntilError
      .filter(line => line.lineType === IoLineType.INPUT)
      .map(line => line.content)
      .map(line => line.replace(/^> /, ""))
      .join("\n");
    this.copyText(input, element);
  }

  copyFullOutput(result: FileCheckResult, element: HTMLElement) {
    this.copyText(result.output.map(line => line.content).join("\n"), element);
  }

  private copyText(text: string, element: HTMLElement) {
    let textArea = document.createElement("textarea") as HTMLTextAreaElement;
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
      let successful = document.execCommand("copy");
      if (successful) {
        element.classList.add("flash-green");
        setTimeout(() => {
          element.classList.remove("flash-green");
        }, 400);
      }
    } finally {
      document.body.removeChild(textArea);
    }
  }

  mounted() {
    const checkResult = (this.$store as Store<RootState>).state.checkresult
      .checkResult;

    if (!checkResult) {
      return;
    }

    checkResult.results.forEach(({ key, value }) => {
      const allSuccessful = value.every(
        elem => elem.result !== CheckResultType.FAILED
      );

      this.items.push(new SingleFileResult(key, value.slice(), allSuccessful));
    });

    // Pull up failed checks, sort alphabetically inside the groups
    this.items.sort((a, b) => {
      if (a.successful && !b.successful) {
        return 1;
      } else if (!a.successful && b.successful) {
        return -1;
      }
      return a.fileName.localeCompare(b.fileName);
    });
  }

  // ICONS
  private successfulIcon = mdiCheckCircleOutline;
  private failedIcon = mdiCloseCircleOutline;
}
</script>


<style scoped>
.check-background {
  background-color: var(--v-check_background-base);
}
.monospaced {
  font-family: monospace;
  overflow-x: auto;
  align-items: center;
}

.monospaced > .v-icon {
  flex-grow: 0;
  margin-right: 1em;
}

.scrollable-container {
  height: 70vh;
  overflow-y: auto;
  margin-top: 8px;
}

.duration-tag {
  text-align: end;
  font-family: monospace;
  color: var(--v-muted-base);
}

.theme--dark .duration-tag .above-average {
  color: white;
}

.theme--light .duration-tag .above-average {
  color: black;
}
</style>
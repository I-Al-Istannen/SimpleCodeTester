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
        </v-toolbar>
        <v-card-title v-if="items.length === 0">
          <h2
            class="headline"
          >No tests were run against your code. Are you sure it has a main class?</h2>
        </v-card-title>
        <v-card-text class="scrollable-container">
          <v-expansion-panel expand v-model="failureBooleanArray">
            <v-expansion-panel-content v-for="(item, i) in items" :key="i">
              <div slot="header" class="monospaced">
                <v-icon v-if="item.successful" color="green">check_circle_outline</v-icon>
                <v-icon v-else color="#ff6347">highlight_off</v-icon>
                Class '{{ item.fileName }}'
              </div>
              <v-card>
                <v-card-text class="grey lighten-3">
                  <!-- Inner panel -->
                  <v-expansion-panel expand class="elevation-4">
                    <v-expansion-panel-content v-for="(result, i) in item.results" :key="i">
                      <div slot="header" class="monospaced">
                        <v-icon v-if="!result.failed()" color="green">check_circle_outline</v-icon>
                        <v-icon v-else color="#ff6347">highlight_off</v-icon>
                        Check '{{ result.check }}'
                      </div>
                      <v-card>
                        <v-card-text class="grey lighten-3">
                          <pre class="monospaced" v-if="result.message">{{ result.message }}</pre>
                          <pre class="monospaced" v-if="result.errorOutput">{{ result.errorOutput }}</pre>
                          <interleaved-io
                            border="false"
                            faithfulFormat="false"
                            :lines="result.output"
                          ></interleaved-io>
                        </v-card-text>
                      </v-card>
                    </v-expansion-panel-content>
                  </v-expansion-panel>
                  <!-- End of inner panel -->
                </v-card-text>
              </v-card>
            </v-expansion-panel-content>
          </v-expansion-panel>
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
  RootState,
  FileCheckResult,
  CheckResultType,
  IoLine
} from "@/store/types";
import HighlightInterleavedIo, {
  IoLineType
} from "@/components/highlighting/HighlightedInterleavedIo.vue";

class SingleFileResult {
  fileName: string;
  results: Array<FileCheckResult>;
  successful: boolean;

  constructor(
    fileName: string,
    results: Array<FileCheckResult>,
    successful: boolean
  ) {
    this.fileName = fileName;
    this.results = results;
    this.successful = successful;

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
    "interleaved-io": HighlightInterleavedIo
  }
})
export default class Test extends Vue {
  private items: Array<SingleFileResult> = [];

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
}
</script>


<style scoped>
.monospaced {
  font-family: monospace;
  overflow-x: auto;
}

.scrollable-container {
  height: 70vh;
  overflow-y: auto;
  margin-top: 8px;
}
</style>
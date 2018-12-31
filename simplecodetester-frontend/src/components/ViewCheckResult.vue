<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-12">
        <v-toolbar dark :color="allPassed ? 'primary' : '#ff6347'">
          <v-toolbar-title>
            Check results
            <span v-if="!allPassed">({{ failCount }} classes failed)</span>
          </v-toolbar-title>
        </v-toolbar>
        <v-card-text>
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
                        <v-icon v-if="result.successful" color="green">check_circle_outline</v-icon>
                        <v-icon v-else color="#ff6347">highlight_off</v-icon>
                        Check '{{ result.check }}'
                      </div>
                      <v-card>
                        <v-card-text class="grey lighten-3">
                          <pre class="monospaced" v-if="result.message">{{ result.message }}</pre>
                          <pre class="monospaced" v-if="result.errorOutput">{{ result.errorOutput }}</pre>
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
import { RootState, FileCheckResult } from "@/store/types";

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
  }
}

@Component({})
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

  mounted() {
    const checkResult = (this.$store as Store<RootState>).state.checkresult
      .checkResult;

    if (!checkResult) {
      return;
    }

    checkResult.results.forEach(({ key, value }) => {
      const allSuccessful = value.every(elem => elem.successful);

      this.items.push(new SingleFileResult(key, value.slice(), allSuccessful));
    });
  }
}
</script>


<style scoped>
.monospaced {
  font-family: monospace;
  overflow-x: auto;
}
</style>
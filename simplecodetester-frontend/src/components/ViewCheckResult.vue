<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-12">
        <v-toolbar dark :color="allPassed ? primary : '#ff6347'">
          <v-toolbar-title>Check results
            <span v-if="!allPassed"> ({{ failCount }} failed)</span>
             </v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-expansion-panel>
            <v-expansion-panel-content v-for="(item, i) in items" :key="i">
              <div slot="header" class="check-expansion">
                <v-icon v-if="item.successful" color="green">check_circle_outline</v-icon>
                <v-icon v-else color="#ff6347">highlight_off</v-icon>
                Class '{{ item.fileName }}'
              </div>
              <v-card>
                <v-card-text class="grey lighten-3">
                  <v-data-table :headers="headers" :items="item.results">
                    <template slot="items" slot-scope="props">
                      <td class="text-xs-right">{{ props.item.name }}</td>
                      <td class="text-xs-right">{{ props.item.message }}</td>
                      <td class="text-xs-right">{{ props.item.errorOutput }}</td>
                    </template>
                  </v-data-table>
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
  private tree: Array<SingleFileResult> = [];

  private headers = [
    { text: "Check", value: "check", align: "center" },
    { text: "Message", value: "message", align: "center" },
    { text: "Error output", value: "errorOutput", align: "center" }
  ];

  get allPassed() {
    return this.items.every(elem => elem.successful);
  }

  get failCount() {
    return this.items.filter(elem => !elem.successful).length;
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
.check-expansion {
  font-family: monospace;
}
</style>
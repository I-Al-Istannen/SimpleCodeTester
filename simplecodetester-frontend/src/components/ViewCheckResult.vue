<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md4>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Check results</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-treeview v-model="tree" :items="items" activatable item-key="name" open-on-click>
            <template slot="prepend" slot-scope="{ item }">
              <v-icon>{{ item.icon }}</v-icon>
            </template>
          </v-treeview>
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

class Entry {
  name: string;
  children: Array<Entry>;
  icon: string;

  constructor(name: string, children: Array<Entry>, icon: string) {
    this.name = name;
    this.children = children;
    this.icon = icon;
  }
}

@Component({})
export default class Test extends Vue {
  private items: Array<Entry> = [];
  private tree: Array<Entry> = [];

  mounted() {
    const checkResult = (this.$store as Store<RootState>).state.checkresult
      .checkResult;

    if (!checkResult) {
      return;
    }

    checkResult.results.forEach((value, key) => {
      const children = value.map(fileCheckResult => {
        const resultProperties = [
          new Entry("Successful: " + fileCheckResult.successful, [], "build")
        ];
        if (fileCheckResult.message.length > 0) {
          resultProperties.push(
            new Entry("Message:\n" + fileCheckResult.message, [], "build")
          );
        }
        if (fileCheckResult.errorOutput.length > 0) {
          resultProperties.push(
            new Entry("Error output:\n" + fileCheckResult.errorOutput, [], "build")
          );
        }
        return new Entry(fileCheckResult.check, resultProperties, "");
      });
      const outerEntry = new Entry(key, children, "attach_file");
      this.items.push(outerEntry);
    });
  }
}
</script>


<style scoped>
</style>
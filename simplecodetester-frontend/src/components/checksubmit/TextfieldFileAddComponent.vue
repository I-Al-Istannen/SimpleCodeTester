<template>
  <v-data-iterator :items="files" item-key="name" hide-default-footer no-data-text>
    <template v-slot:default="{ items }">
      <v-container v-for="(file, index) in items" :key="index" class="field-container mt-4 py-0">
        <v-row>
          <v-col cols="auto" class="d-flex field-title py-0">
            <v-text-field
              v-model="file.name"
              @input="pushFinishedFiles"
              label="Dateiname mit Endung"
            ></v-text-field>
            <v-btn v-if="items.length > 1" icon color="red" @click="deleteFile(file)">
              <v-icon>{{ deleteIcon }}</v-icon>
            </v-btn>
          </v-col>
        </v-row>

        <v-row class="mt-2">
          <v-col class="field-content">
            <v-textarea
              v-model="file.content"
              @input="pushFinishedFiles"
              label="File content"
              filled
              auto-grow
            ></v-textarea>
          </v-col>
        </v-row>
      </v-container>
    </template>
    <template v-slot:footer>
      <v-container>
        <v-row justify="center">
          <v-col cols="auto">
            <v-btn @click="addNew()">
              <v-icon left>{{ addIcon }}</v-icon>Datei Hinzufügen
            </v-btn>
          </v-col>
        </v-row>
      </v-container>
    </template>
  </v-data-iterator>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { mdiDelete, mdiPlusCircle } from "@mdi/js";
import { IOCheckFile } from "../checklist/CheckTypes";

@Component
export default class TextfieldFileAddComponent extends Vue {
  private text = "World";
  private files: Array<IOCheckFile> = [];

  deleteFile(file: IOCheckFile) {
    this.files = this.files.filter(it => it != file);
  }

  addNew() {
    this.files.push(new IOCheckFile("", ""));
  }

  pushFinishedFiles() {
    const payload = this.files.filter(
      it => it.name !== "" && it.content !== ""
    );
    if (payload.length != 0) {
      this.$emit("input", payload);
    }
  }

  // ICONS
  private deleteIcon = mdiDelete;
  private addIcon = mdiPlusCircle;
}
</script>

<style scoped>
.field-container .row,
.field-container {
  border-style: solid;
  border-color: #e1e4e8;
}
.field-container .row {
  border-width: 2px 0 2px 0;
}
.field-container {
  border-width: 0 2px 0 2px;
}
.field-title,
.field-content {
  background-color: white;
}
</style>
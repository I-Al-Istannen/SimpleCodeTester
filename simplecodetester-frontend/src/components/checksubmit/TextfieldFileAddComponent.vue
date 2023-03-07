<template>
  <v-data-iterator
    :items="value"
    item-key="name"
    hide-default-footer
    no-data-text
    :items-per-page="25"
    :class="[plainOutline ? 'plain' : '']"
  >
    <template v-slot:default="{ items }">
      <v-container v-for="(file, index) in items" :key="index" class="field-container mt-4 py-0">
        <v-row>
          <v-col cols="auto" class="d-flex field-title py-0 align-center">
            <v-text-field :readonly="!editable" v-model="file.name" label="Dateiname mit Endung"></v-text-field>
            <v-btn v-if="editable" icon color="red" @click="deleteFile(file)">
              <v-icon>{{ deleteIcon }}</v-icon>
            </v-btn>
          </v-col>
        </v-row>

        <v-row class="mt-2">
          <v-col class="field-content">
            <v-textarea
              :readonly="!editable"
              v-model="file.content"
              label="File content"
              filled
              auto-grow
            ></v-textarea>
          </v-col>
        </v-row>
      </v-container>
    </template>
    <template v-slot:footer v-if="editable">
      <v-container>
        <v-row justify="center">
          <v-col cols="auto">
            <v-btn @click="addNew()" :disabled="value.length >= 25">
              <v-icon left>{{ addIcon }}</v-icon>
              {{ fileUploadPrompt }}
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
import { Prop } from "vue-property-decorator";

@Component
export default class TextfieldFileAddComponent extends Vue {
  @Prop({ default: true })
  editable!: boolean;

  @Prop({ default: () => [] })
  value!: Array<IOCheckFile>;

  @Prop({ default: false })
  plainOutline!: boolean;

  @Prop({ default: "Datei Hinzufügen" })
  fileUploadPrompt!: string;

  deleteFile(file: IOCheckFile) {
    this.$emit(
      "input",
      this.value.filter(it => it != file)
    );
  }

  addNew() {
    const newFiles = this.value.slice();
    newFiles.push(new IOCheckFile("", ""));
    this.$emit("input", newFiles);
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
  border-color: var(--v-code_border-base);
}

.plain .field-container .row,
.plain .field-container {
  border-style: solid;
  border-color: var(--v-muted-base);
}

.field-container .row {
  border-width: 1px 0 1px 0;
}
.field-container {
  border-width: 0 1px 0 1px;
}
</style>

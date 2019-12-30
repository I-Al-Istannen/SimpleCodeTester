<template>
  <v-data-iterator
    :items="value"
    item-key="name"
    hide-default-footer
    no-data-text
    :style="{ '--outline-color': outlineColor }"
  >
    <template v-slot:default="{ items }">
      <v-container v-for="(file, index) in items" :key="index" class="field-container mt-4 py-0">
        <v-row>
          <v-col cols="auto" class="d-flex field-title py-0">
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
import { Prop, Watch } from "vue-property-decorator";

@Component
export default class TextfieldFileAddComponent extends Vue {
  @Prop({ default: true })
  editable!: boolean;

  @Prop({ default: () => [] })
  value!: Array<IOCheckFile>;

  @Prop({ default: "#e1e4e8" })
  outlineColor!: string;

  deleteFile(file: IOCheckFile) {
    this.$emit("input", this.value.filter(it => it != file));
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
  border-color: var(--outline-color);
}
.field-container .row {
  border-width: 2px 0 2px 0;
}
.field-container {
  border-width: 0 2px 0 2px;
}
</style>
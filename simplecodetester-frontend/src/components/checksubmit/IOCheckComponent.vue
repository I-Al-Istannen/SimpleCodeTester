<template>
  <div>
    <v-text-field label="Check name" v-model="name"></v-text-field>
    <v-textarea auto-grow class="monospace-font" :label="inputFieldLabel" v-model="input"></v-textarea>
    <v-textarea
      v-if="output !== null"
      auto-grow
      class="monospace-font"
      label="Output. Hit enter for a new line."
      v-model="output"
    ></v-textarea>
    <v-container>
      <v-row justify="center">
        <v-col cols="10">
          <textfield-add-file @input="files = $event"></textfield-add-file>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop, Watch } from "vue-property-decorator";
import { IOCheck, IOCheckFile } from "@/components/checklist/CheckTypes";
import { mdiPlusCircle } from "@mdi/js";
import TextfieldFileAddComponent from "./TextfieldFileAddComponent.vue";

@Component({
  components: {
    "textfield-add-file": TextfieldFileAddComponent
  }
})
export default class IOCheckComponent extends Vue {
  private input = "";
  private name = "";
  private output: string | null = null;
  private files: Array<IOCheckFile> = [];

  @Prop()
  initialValue!: IOCheck;

  @Prop()
  value!: IOCheck;

  @Watch("initialValue")
  onInitialValueChanged() {
    this.input = this.initialValue.input;
    this.output = this.initialValue.output;
    this.name = this.initialValue.name;
  }

  @Watch("input")
  inputChanged() {
    this.emit();
  }

  @Watch("output")
  outputChanged() {
    this.emit();
  }

  @Watch("name")
  nameChanged() {
    this.emit();
  }

  @Watch("files")
  filesChanged() {
    this.emit();
  }

  get inputFieldLabel() {
    return this.output === null
      ? "Check data"
      : "Input. Hit enter for a new line.";
  }

  emit() {
    this.$emit(
      "input",
      new IOCheck(this.input, this.output, this.name, this.files)
    );
  }

  // ICONS
  private addFileIcon = mdiPlusCircle;
}
</script>

<style scoped>
.monospace-font {
  font-family: monospace !important;
}
</style>

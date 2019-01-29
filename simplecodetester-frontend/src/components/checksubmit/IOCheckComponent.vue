<template>
  <div>
    <v-text-field label="Check name" v-model="name"></v-text-field>
    <v-textarea class="monospace-font" label="Input. Hit enter for a new line." v-model="input"></v-textarea>
    <v-textarea
      class="monospace-font"
      label="Expected output. Hit enter for a new line. Lines starting with '?r' will be treated as a regular expression."
      v-model="output"
    ></v-textarea>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop, Watch } from "vue-property-decorator";
import { IOCheck } from "@/components/checklist/CheckTypes";

@Component
export default class IOCheckComponent extends Vue {
  private input = "";
  private output = "";
  private name = "";

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

  emit() {
    this.$emit("input", new IOCheck(this.input, this.output, this.name));
  }
}
</script>

<style scoped>
.monospace-font {
  font-family: monospace !important;
}
</style>

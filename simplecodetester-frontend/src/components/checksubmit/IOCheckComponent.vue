<template>
  <div>
    <v-text-field label="Check name" v-model="name"></v-text-field>
    <v-textarea label="Input. Hit enter for a new line." v-model="input"></v-textarea>
    <v-textarea label="Expected output. Hit enter for a new line." v-model="output"></v-textarea>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop, Watch } from "vue-property-decorator";

export class IOCheck {
  input: string;
  output: string;
  name: string;

  constructor(input: string, output: string, name: string) {
    this.input = input;
    this.output = output;
    this.name = name;
  }
}

@Component
export default class IOCheckComponent extends Vue {
  private input = "";
  private output = "";
  private name = "";

  @Prop()
  value!: IOCheck;

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
</style>

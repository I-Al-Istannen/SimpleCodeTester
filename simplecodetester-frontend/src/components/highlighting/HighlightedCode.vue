<template>
  <v-textarea @input="onInput" :text="value"></v-textarea>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";

@Component({})
export default class HighlightedCode extends Vue {
  @Prop({ default: "class Test {}" })
  value!: string;

  onInput(input: string) {
    this.$emit("input", input);
  }
}
</script>

<style scoped>
</style>

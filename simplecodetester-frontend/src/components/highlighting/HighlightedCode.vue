<template>
  <div class="wrapper">
    <div ref="inputDiv" v-html="html" contenteditable @input="onInput"></div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { highlight } from "highlight.js";
import "highlight.js/styles/atom-one-light.css";
import { Prop, Watch } from "vue-property-decorator";

@Component({})
export default class HighlightedCode extends Vue {
  private language: string = "java";

  @Prop({ default: "class Test" })
  value!: string;

  html: string = "test"

  @Watch("value")
  onCodeChanged() {
    const textWhenStarted = this.value;
    setTimeout(() => {
      if (textWhenStarted == this.value) {
        this.highlight();
      }
    }, 5000);
  }

  onInput(event: Event) {
    this.$emit("input", (this.$refs["inputDiv"] as Element).textContent || "");
  }

  highlight() {
    this.html = highlight(this.language, this.value, true, undefined).value;
  }
}
</script>

<style scoped>
</style>

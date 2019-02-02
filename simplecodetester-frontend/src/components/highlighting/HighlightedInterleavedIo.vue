<template>
  <div class="container accent pa-1">
    <span v-for="(line, index) in lines" :key="index" :class="lineStyle(line)">
      <span class="prefix">{{ getPrefix(line) }}</span>
      <span class="rest">{{ getRest(line) }}</span>
    </span>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";

export class IoLine {
  lineType: IoLineType;
  content: string;

  constructor(lineType: IoLineType, content: string) {
    this.lineType = lineType;
    this.content = content;
  }
}

export enum IoLineType {
  ERROR = "error",
  INPUT = "input",
  OUTPUT = "output"
}

@Component
export default class HighlightInterleavedIo extends Vue {
  @Prop({ default: () => [] })
  lines!: Array<IoLine>;

  lineStyle(line: IoLine) {
    let object: any = { line: true };

    object[line.lineType] = true;

    return object;
  }

  getPrefix(line: IoLine) {
    if (line.content.startsWith("> ") || line.content.startsWith("<")) {
      return line.content.substring(0, 2);
    }

    return "";
  }

  getRest(line: IoLine) {
    if (this.getPrefix(line) !== "") {
      return line.content.substring(2);
    }
    return line.content;
  }
}
</script>

<style scoped>
.container {
  padding: 0px;
  border: 1px solid;
  background: transparent !important;
}
.line {
  display: block;
  font-family: monospace;
}
.line.error {
  color: tomato;
}
.line.input {
  color: royalblue;
}
.line.output {
  color: gray;
}
.line > .prefix {
  font-weight: bold;
  padding-right: 2px;
}
</style>

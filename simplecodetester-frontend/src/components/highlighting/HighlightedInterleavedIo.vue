<template>
  <div ref="container" class="container accent pa-2">
    <span v-for="(line, index) in lines" :key="index" :class="lineStyle(line)">
      <span class="prefix">{{ getPrefix(line) }}</span>
      <span class="rest">{{ getRest(line) }}</span>
    </span>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop, Watch } from "vue-property-decorator";

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

  @Prop({ default: "true" })
  faithfulFormat!: string;

  @Prop({ default: "true" })
  border!: string;

  @Watch("noBorder")
  setBorderVisiblity() {
    let classes = (this.$refs["container"] as Element).classList;
    classes.remove("container-border");

    if (this.border !== "false") {
      classes.add("container-border");
    }
  }

  @Watch("faithfulFormat")
  updateFaithfulFormat() {
    // Yea, this should be solved differently, but I won't change the format on the fly
    // and the list should be reasonably small
    this.$forceUpdate();
  }

  lineStyle(line: IoLine) {
    let object: any = { line: true };

    object[line.lineType] = true;

    if (this.faithfulFormat === "false") {
      if (line.lineType === IoLineType.INPUT) {
        object["prepend-input-label"] = true;
      }
    }

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

  mounted() {
    this.setBorderVisiblity();
  }
}
</script>

<style scoped>
.container {
  background: transparent !important;
}
.container-border {
  border: 1px solid;
}

.line {
  display: block;
  font-family: monospace;
}
.line.error {
  background-color: transparent !important;
}
.prepend-input-label::before {
  content: "> ";
}

.line.input {
  color: royalblue;
}
.line.output {
  color: gray;
}
.line.error > .rest {
  color: tomato;
}
.line > .prefix {
  font-weight: bold;
  padding-right: 2px;
  color: lightgray;
}
</style>

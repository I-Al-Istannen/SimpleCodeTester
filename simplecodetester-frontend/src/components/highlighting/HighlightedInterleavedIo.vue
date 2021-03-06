<template>
  <div v-show="lines.length > 0" ref="container" class="container pa-2">
    <span v-for="(line, index) in lines" :key="index" :class="lineStyle(line)">
      <span v-if="getPrefix(line).length > 0" class="prefix">{{ getPrefix(line) }}</span>
      <span class="rest">{{ getRest(line) }}</span>
    </span>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop, Watch } from "vue-property-decorator";
import { IoLine, IoLineType } from "@/store/types";

@Component
export default class HighlightInterleavedIo extends Vue {
  @Prop({ default: () => [] })
  lines!: Array<IoLine>;

  @Prop({ type: Boolean, default: true })
  faithfulFormat!: boolean;

  @Prop({ type: Boolean, default: true })
  border!: boolean;

  @Watch("noBorder")
  setBorderVisiblity() {
    let classes = (this.$refs["container"] as Element).classList;
    classes.remove("container-border");

    if (this.border) {
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
    let cssClasses: any = { line: true };

    cssClasses[line.lineType] = true;

    cssClasses["faithful-line"] = this.faithfulFormat;

    return cssClasses;
  }

  getPrefix(line: IoLine) {
    const regex = /^(> |<.|# |\$\$ )/;
    const match = regex.exec(line.content);
    if (match) {
      return match[1];
    }

    return "";
  }

  getRest(line: IoLine) {
    let content: string;
    if (this.getPrefix(line) !== "") {
      content = line.content.substring(this.getPrefix(line).length);
    } else {
      content = line.content;
    }

    if (
      line.lineType == IoLineType.INPUT ||
      line.lineType == IoLineType.PARAMETER
    ) {
      return this.replaceSpacesWithSpecialChar(content);
    }

    return content;
  }

  private replaceSpacesWithSpecialChar(input: string) {
    return input.replace(/ /g, "␣");
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
  border-color: var(--v-code_border-base);
}

.line {
  display: block;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
.line.error {
  background-color: transparent !important;
}

.line.input {
  color: var(--v-line_input-base);
}
.line.parameter {
  color: var(--v-line_parameter-base);
}
.line.output {
  color: var(--v-line_output-base);
}
.line.error > .rest {
  color: var(--v-line_error-base);
}
.line.other {
  color: var(--v-line_other-base);
}
.line:not(.other) > .prefix,
.line:not(.other)::before {
  font-weight: bold;
  padding-right: 2px;
  color: var(--v-line_prefix-base);
}

.faithful-line > .prefix:empty {
  padding-right: 1px;
}

/* Offset the input label */
.line.output::before {
  content: "  ";
  white-space: pre;
}

.line.input:not(.faithful-line)::before {
  content: "> ";
}

.line.parameter:not(.faithful-line)::before {
  content: "$$ ";
}
</style>

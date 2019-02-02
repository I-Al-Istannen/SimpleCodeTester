<template>
  <div>
    <div class="label caption">Category</div>
    <v-chip class="ml-0 mb-3" disabled label color="accent" outline>{{ checkBase.category.name }}</v-chip>
    <div v-if="content">
      <span v-if="checkBase.checkType === 'IO'">
        <v-textarea
          class="monospace-font"
          label="Input"
          :value="contentJson.input.join('\n')"
          readonly
        ></v-textarea>
        <v-textarea
          class="monospace-font"
          label="Expected output"
          :value="contentJson.expectedOutput"
          readonly
        ></v-textarea>
      </span>
      <span v-if="contentJson.class && contentJson.class.endsWith('InterleavedStaticIOCheck')">
        <interleaved-io :lines="interleavedLines" faithfulFormat=true></interleaved-io>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
/// <reference path="../../vue-prism-component.d.ts" />

import Vue from "vue";
import Component from "vue-class-component";
import "prismjs";
import "prismjs/themes/prism.css";
require("prismjs/components/prism-java.min.js");
import Prism from "vue-prism-component";
import { CheckBase } from "@/components/checklist/CheckTypes";
import { Prop, Provide } from "vue-property-decorator";
import HighlightInterleavedIo, {
  IoLine,
  IoLineType
} from "@/components/highlighting/HighlightedInterleavedIo.vue";

@Component({
  components: {
    prism: Prism,
    "interleaved-io": HighlightInterleavedIo
  }
})
export default class CheckDisplay extends Vue {
  @Prop()
  private checkBase!: CheckBase;

  @Prop()
  private content!: any;

  get contentJson() {
    return typeof this.content === "object"
      ? this.content
      : JSON.parse(this.content);
  }

  get interleavedLines() {
    let lines = (this.content.text as string).split(/\n/);

    return lines.map(line => {
      if (line.startsWith("> ")) {
        return new IoLine(IoLineType.INPUT, line);
      } else if (line.startsWith("<")) {
        return new IoLine(IoLineType.OUTPUT, line);
      } else {
        return new IoLine(IoLineType.OUTPUT, "  " + line);
      }
    });
  }
}
</script>

<style scoped>
.code {
  font-size: 16px;
}
.code > code {
  box-shadow: none;
  -webkit-box-shadow: none;
}
.code > code::before {
  content: "";
}

.label {
  color: rgba(0, 0, 0, 0.54);
}

.monospace-font {
  font-family: monospace !important;
}
</style>

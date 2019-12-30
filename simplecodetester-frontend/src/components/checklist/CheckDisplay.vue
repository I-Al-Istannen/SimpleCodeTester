<template>
  <div>
    <div class="label caption">Category</div>
    <v-chip class="ml-0 mb-3" disabled label color="accent" outlined>{{ checkBase.category.name }}</v-chip>
    <div v-if="content">
      <span v-if="content.class === 'StaticInputOutputCheck'">
        <v-textarea class="monospace-font" label="Input" :value="content.check.input" readonly></v-textarea>
        <v-textarea
          class="monospace-font"
          label="Expected output"
          :value="content.check.output"
          readonly
        ></v-textarea>
      </span>
      <span v-if="content.class === 'InterleavedStaticIOCheck'">
        <interleaved-io :lines="interleavedLines" :faithfulFormat="true"></interleaved-io>
      </span>
      <span v-if="content.check.files.length != 0">
        <display-files-component
          outlineColor="#eeaef9"
          :value="content.check.files"
          :editable="false"
        ></display-files-component>
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
import Prism from "vue-prism-component";
import { CheckBase } from "@/components/checklist/CheckTypes";
import { Prop } from "vue-property-decorator";
import HighlightInterleavedIo from "@/components/highlighting/HighlightedInterleavedIo.vue";
import { IoLine, IoLineType } from "@/store/types";
import TextfieldFileAddComponent from "../checksubmit/TextfieldFileAddComponent.vue";

require("prismjs/components/prism-java.min.js");

@Component({
  components: {
    prism: Prism,
    "interleaved-io": HighlightInterleavedIo,
    "display-files-component": TextfieldFileAddComponent
  }
})
export default class CheckDisplay extends Vue {
  @Prop()
  private checkBase!: CheckBase;

  @Prop()
  private content!: any;

  get interleavedLines() {
    let lines = (this.content.check.input as string).split(/\n/);

    return lines.map(line => {
      if (line.startsWith("> ")) {
        return new IoLine(IoLineType.INPUT, line);
      } else if (line.startsWith("<")) {
        return new IoLine(IoLineType.OUTPUT, line);
      } else if (line.startsWith("#")) {
        return new IoLine(IoLineType.OTHER, line);
      } else if (line.startsWith("$$")) {
        return new IoLine(IoLineType.PARAMETER, line);
      } else {
        return new IoLine(IoLineType.OUTPUT, line);
      }
    });
  }
}
</script>

<style scoped>
.v-chip {
  opacity: 1;
}

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

<template>
  <div>
    <div class="label caption">Category</div>
    <v-chip class="ml-0 mb-3" label color="accent" outline>{{ checkBase.category.name }}</v-chip>
    <div v-if="content">
      <prism v-if="checkBase.checkType === 'SOURCE_CODE'" class="code" language="java">{{ content }}</prism>
      <span v-if="checkBase.checkType === 'IO'">
        <v-textarea label="Input" :value="contentJson.input.join('\n')" readonly></v-textarea>
        <v-textarea label="Expected output" :value="contentJson.expectedOutput"></v-textarea>
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

@Component({
  components: {
    prism: Prism
  }
})
export default class CheckDisplay extends Vue {
  @Prop()
  private checkBase!: CheckBase;

  @Prop()
  private content!: any;

  get contentJson() {
    return JSON.parse(this.content);
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
  color: rgba(0, 0, 0, 0.54)
}
</style>

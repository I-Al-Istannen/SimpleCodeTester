<template>
  <div class="container pa-2" :class="[readonly ? 'read-only' : '']">
    <prism-editor
      :readonly="readonly"
      @input="$emit('input', $event)"
      :value="effectiveCode"
      :highlight="highlight"
    ></prism-editor>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Model, Prop } from "vue-property-decorator";
import { PrismEditor } from "vue-prism-editor";
import { guessLineType, highlight } from "@/util/Highlighting";
import "vue-prism-editor/dist/prismeditor.min.css"; // import the styles somewhere
import { IoLine } from "@/store/types";

@Component({
  components: {
    "prism-editor": PrismEditor
  }
})
export default class HighlightedCode extends Vue {
  @Model("input", { default: "" })
  private readonly code!: string;

  @Prop({ default: () => [] })
  private readonly ioLines!: IoLine[];

  @Prop({ default: false, type: Boolean })
  private readonly readonly!: boolean;

  private get effectiveCode() {
    if (this.ioLines.length > 0) {
      return this.ioLines.map(it => it.content).join("\n");
    }
    return this.code;
  }

  private highlight(code: string) {
    if (this.ioLines.length > 0) {
      return highlight(this.ioLines);
    }
    return highlight(
      code.split("\n").map(it => new IoLine(guessLineType(it), it))
    );
  }

  private mounted() {
    Array.from(this.$el.getElementsByTagName("textarea")).forEach(area => {
      area.placeholder = "> Some Input\nSome output";
    });
  }
}
</script>

<style scoped>
.container {
  background: transparent !important;
  border: 1px solid;
  border-color: var(--v-code_border-base);
}
</style>

<style>
.prism-editor__textarea {
  font-family: monospace !important;
}
.prism-editor__textarea:focus {
  outline: none;
}
.line {
  display: block;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
.line.line-error {
  background-color: transparent !important;
}

.line.line-input {
  color: var(--v-line_input-base);
}
.line.line-parameter {
  color: var(--v-line_parameter-base);
}
.line.line-output {
  color: var(--v-line_output-base);
}
.line.line-error > .rest {
  color: var(--v-line_error-base);
}
.line.line-other {
  color: var(--v-line_other-base);
}
.line:not(.line-other) > .prefix,
.line:not(.line-other)::before {
  font-weight: bold;
  color: var(--v-line_prefix-base);
}

/* Offset the input label */
.read-only .line.line-output::before {
  content: "  ";
  white-space: pre;
}
</style>

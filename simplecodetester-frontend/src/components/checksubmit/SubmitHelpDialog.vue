<template>
  <v-dialog v-model="ioHelpOpened" max-width="700" class="help-dialog">
    <template v-slot:activator="{ on }">
      <v-btn v-on="on" icon>
        <v-icon large>{{ helpIcon }}</v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-toolbar dark color="primary">
        <v-toolbar-title>Help</v-toolbar-title>
      </v-toolbar>
      <v-card-text class="body-1">
        <div>
          <h3 class="title pb-3">Required fields</h3>

          <ul>
            <li>
              <span class="body-2">Check category:</span>
              <br />The category of the check, typically the assignment.
            </li>
            <li>
              <span class="body-2">Check name:</span>
              <br />The name of the check
            </li>
            <li>
              <span class="body-2">Check data:</span>
              <br />The actual data of the check.
              <p></p>It consists of a few different parts:
              <ul class="mb-3">
                <li>
                  Input lines (prefixed with
                  <span class="literal">></span>).
                  The single space after the ">" is
                  <em>important!</em>
                </li>
                <li>Regular output lines (no prefix)</li>
                <li>
                  Error output lines (prefixed with
                  <span class="literal">&lt;e</span>) that match any error
                </li>
                <li>
                  Regular expression output lines (prefixed with
                  <span class="literal">&lt;r</span>)
                  that match a regular expression.
                  <br />The regular expression is anchored at front and end, so it needs to match the whole line.
                </li>
                <li>
                  Parameter lines (prefixed with
                  <span class="literal">$$</span>).
                  The single space after the "$$" is
                  <em>important!</em> Note that the codetester will not split
                  at spaces. Each line is a SINGLE entry in the "args" parameter in your java program.
                </li>
                <li>
                  Literal output lines (prefixed with
                  <span class="literal">&lt;l</span>) that match whatever
                  is after the
                  <span class="literal">&lt;l</span> literally.
                  <br />You can use them if the program needs to output
                  <span class="literal">&lt;e</span>, for example.
                </li>
                <li>
                  Comments (prefixed with
                  <span class="literal">#</span>) that are ignored, but help organize it.
                </li>
              </ul>
              <span class="body-2">Example:</span>
              <br />
              <pre class="example">
            # Set a parameter. Will not split at spaces!
            $$ I am a parameter!
            > start
            OK
            # Can not start an already started game
            > start
            &lt;e
            > abort
            > abort
            &lt;e
            > quit
        </pre>
            </li>
          </ul>
        </div>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" @click="ioHelpOpened = false">Close</v-btn>
        <v-spacer></v-spacer>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { mdiHelpCircleOutline } from "@mdi/js";

@Component
export default class SubmitHelpDialog extends Vue {
  private ioHelpOpened: boolean = false;

  // ICONS
  private helpIcon = mdiHelpCircleOutline;
}
</script>

<style scoped>
.help-dialog {
  display: flex !important;
  justify-content: flex-end !important;
}

.literal {
  padding: 0px 5px 0px 5px;
  font-family: monospace;
  white-space: pre;
  background-color: rgba(128, 128, 128, 0.2);
}
.example {
  white-space: pre-line;
}
</style>
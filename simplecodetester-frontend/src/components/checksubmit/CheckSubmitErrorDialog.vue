<template>
  <v-dialog :value="displayed" width="700" persistent>
    <v-card>
      <v-toolbar dark color="#ff6347">
        <v-toolbar-title>Errors with your check:</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-list>
          <template v-for="(item, i) in problems">
            <v-list-tile :key="i" class="big-list-tile mb-2" @click="blackhole">
              <v-list-tile-content>
                <div class="fakePre">{{ item }}</div>
              </v-list-tile-content>
            </v-list-tile>
            <v-divider v-if="i + 1 < problems.length" :key="`divider-${i}`" class="my-3"></v-divider>
          </template>
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="primary" flat @click="$emit('close')">Close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";

@Component({})
export default class CheckSubmitErrorDialog extends Vue {
  @Prop()
  private problems!: Array<string>;

  @Prop()
  private displayed!: boolean;

  blackhole() {}
}
</script>

<style>
.big-list-tile > .v-list__tile {
  height: 100%;
}
.fakePre {
  overflow-x: auto;
  font-family: monospace;
  white-space: pre-line;
}
</style>

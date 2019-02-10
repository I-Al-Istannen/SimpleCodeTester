<template>
  <v-layout align-center justify-center>
    <v-flex xs12>
      <div class="ma-2">
        <label for="file-upload" class="custom-file-upload">Click me to select file(s). I can also handle source zips :)</label>
        <input accept=".java, .zip" multiple @input="fileSelected" id="file-upload" type="file">
      </div>
      <v-list>
        <v-list-tile v-for="file in files" :key="file.name" @click="blackhole">
          <v-list-tile-content>
            <v-list-tile-title v-html="file.name"></v-list-tile-title>
          </v-list-tile-content>

          <v-list-tile-action>
            <v-btn icon @click="deleteFile(file)">
              <v-icon color="#FF6347">delete</v-icon>
            </v-btn>
          </v-list-tile-action>
        </v-list-tile>
      </v-list>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";

@Component({})
export default class MultiFileSelect extends Vue {
  private files: Array<File> = [];

  deleteFile(file: File) {
    const index = this.files.indexOf(file);

    if (index < 0) {
      return;
    }

    this.files.splice(index, 1);

    this.$emit("input", this.files);
  }

  fileSelected(event: Event) {
    if (!event.target) {
      return;
    }
    const files = (event.target as HTMLInputElement).files;
    if (!files) {
      return;
    }

    Array.from(files).forEach(file => {
      if (this.files.indexOf(file) < 0) {
        this.files.push(file);
      }
    });

    this.$emit("input", this.files);
  }

  /**
   * Only there to make a nice hover background.
   */
  blackhole() {}
}
</script>

<style scoped>
input[type="file"] {
  display: none;
}

.custom-file-upload {
  border-width: 4px;
  border-style: dashed;
  border-color: var(--primary-darken-1);

  display: flex;
  align-items: center;
  justify-content: center;

  cursor: pointer;

  height: 100px;
}
.custom-file-upload:hover {
  box-shadow: 0 0 6px rgba(35, 173, 278, 1);
}
</style>

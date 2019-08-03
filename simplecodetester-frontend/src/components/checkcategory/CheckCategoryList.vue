<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>Check categories</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            :append-icon="searchIcon"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-table
            :items="categories"
            :headers="headers"
            :footer-props="footerProps"
            :search="search"
            :items-per-page="rowsPerPage"
            @update:items-per-page="setRowsPerPage"
          >
            <template v-slot:header.name="{ header }">
              <span class="title">{{ header.text }}</span>
            </template>
            <template v-slot:header.id="{ header }">
              <span class="title">{{ header.text }}</span>
            </template>
            <template v-slot:header.actions="{ header }">
              <span class="title">{{ header.text }}</span>
            </template>

            <template v-slot:body="{ items }">
              <tbody>
                <tr v-for="item in items" :key="item.name">
                  <td class="subtitle-1 text-center">{{ item.name }}</td>
                  <td class="subtitle-1 text-center">{{ item.id }}</td>
                  <td class="subtitle-1 text-center" v-if="isAdmin">
                    <v-btn icon @click="deleteCheckCategory(item)">
                      <v-icon color="red">{{ deleteIcon }}</v-icon>
                    </v-btn>
                  </td>
                </tr>
              </tbody>
            </template>
          </v-data-table>
        </v-card-text>
        <v-card-actions v-if="isAdmin">
          <v-spacer></v-spacer>
          <v-dialog v-model="addDialogOpened" max-width="700">
            <template v-slot:activator="{ on }">
              <v-btn v-on="on" color="primary">Add category</v-btn>
            </template>
            <v-card>
              <v-toolbar dark color="primary">
                <v-toolbar-title>Add new check category</v-toolbar-title>
              </v-toolbar>
              <v-card-text>
                <v-text-field label="Name" single-line v-model="newCheckName"></v-text-field>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn :disabled="!canSubmit" color="primary" @click="addCategory">Submit</v-btn>
                <v-spacer></v-spacer>
              </v-card-actions>
            </v-card>
          </v-dialog>
          <v-spacer></v-spacer>
        </v-card-actions>
        <v-alert :type="messageType" :value="message.length > 0">{{ message }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { CheckCategory, RootState } from "@/store/types";
import { Store } from "vuex";
import { extractErrorMessage } from "@/util/requests";
import { Watch } from "vue-property-decorator";
import { mdiDelete, mdiMagnify } from "@mdi/js";

@Component
export default class CheckCategoryList extends Vue {
  private message: string = "";
  private messageType: string = "error";
  private search: string = "";
  private footerProps = {
    itemsPerPageOptions: [4, 10, 20, 50, 100]
  };
  private newCheckName = "";
  private addDialogOpened = false;
  private ipp = 40;

  get headers() {
    if (this.isAdmin) {
      return [
        { text: "Name", value: "name", align: "center" },
        { text: "Id", value: "id", align: "center" },
        { text: "Actions", value: "actions", align: "center" }
      ];
    }
    return [
      { text: "Name", value: "name", align: "center" },
      { text: "Id", value: "id", align: "center" }
    ];
  }

  get categories(): Array<CheckCategory> {
    return (this.$store as Store<RootState>).state.checkcategory.categories;
  }

  get isAdmin() {
    return (this.$store as Store<RootState>).state.user.isAdmin();
  }

  get canSubmit() {
    return this.addDialogOpened && this.newCheckName.length > 0;
  }

  get rowsPerPage(): number {
    return (this.$store.state as RootState).miscsettings.itemsPerPage;
  }

  @Watch("footerProps.pagination.rowsPerPage", { deep: true })
  setRowsPerPage(rows: number) {
    this.$store.commit("miscsettings/setItemsPerPage", rows);
  }

  addCategory() {
    this.addDialogOpened = false;
    this.handlePromise(
      this.$store
        .dispatch("checkcategory/addNew", this.newCheckName)
        .then(it => {
          this.message = "The check category was added with id " + it.id;
          this.messageType = "success";
        })
    );
    this.newCheckName = "";
  }

  deleteCheckCategory(category: CheckCategory) {
    const confirmDelete = confirm(
      `Do you really want to delete '${category.name}' (${category.id})`
    );

    if (!confirmDelete) {
      return;
    }
    this.handlePromise(
      this.$store.dispatch("checkcategory/deleteCheck", category).then(it => {
        this.message = "";
      })
    );
  }

  handlePromise(promise: Promise<any>) {
    promise.catch(error => {
      this.message = extractErrorMessage(error);
      this.messageType = "error";
    });
  }

  mounted() {
    this.handlePromise(this.$store.dispatch("checkcategory/fetchAll"));
  }

  // ICONS
  private deleteIcon = mdiDelete;
  private searchIcon = mdiMagnify;
}
</script>

<style scoped>
</style>

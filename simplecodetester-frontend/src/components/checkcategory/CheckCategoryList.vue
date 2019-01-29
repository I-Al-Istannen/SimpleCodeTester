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
            append-icon="search"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-table
            :items="categories"
            :headers="headers"
            :rows-per-page-items="rowsPerPageItems"
            :pagination.sync="pagination"
            :search="search"
          >
            <template slot="headerCell" slot-scope="props">
              <span class="title">{{ props.header.text }}</span>
            </template>
            <template slot="items" slot-scope="props">
              <td class="subheading text-xs-center">{{ props.item.name }}</td>
              <td class="subheading text-xs-center">{{ props.item.id }}</td>
              <td class="subheading text-xs-center" v-if="isAdmin">
                <v-btn icon @click="deleteCheckCategory(props.item)">
                  <v-icon color="red">delete</v-icon>
                </v-btn>
              </td>
            </template>
          </v-data-table>
        </v-card-text>
        <v-card-actions v-if="isAdmin">
          <v-spacer></v-spacer>
          <v-dialog v-model="addDialogOpened" max-width="700">
            <v-btn slot="activator" color="primary">Add category</v-btn>
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
import { Watch } from 'vue-property-decorator';

@Component
export default class CheckCategoryList extends Vue {
  private message: string = "";
  private messageType: string = "error";
  private search: string = "";
  private rowsPerPageItems = [4, 10, 20, 50, 100];
  private pagination = {
    rowsPerPage: this.rowsPerPage
  };
  private newCheckName = "";
  private addDialogOpened = false;

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

  @Watch("pagination.rowsPerPage")
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
}
</script>

<style scoped>
</style>

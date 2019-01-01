<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>All checks</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            append-icon="search"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-iterator
            :items="checks"
            :rows-per-page-items="rowsPerPageItems"
            :pagination.sync="pagination"
            :search="search"
            content-tag="v-layout"
            column
            wrap
            :filter="filterValueHandleApproved"
          >
            <v-flex slot="item" slot-scope="props" xs12 sm6 md4 lg3>
              <v-expansion-panel expand>
                <v-expansion-panel-content @input="checkOpened(props.item)">
                  <div slot="header" class="monospaced subheading d-flex check-header">
                    <span
                      class="left-side-text"
                    >Check '{{ props.item.name }}' by '{{ props.item.creator }}' (ID: {{ props.item.id }})</span>
                    <span class="pl-2 unapproved" v-if="!props.item.approved">Unapproved</span>
                    <!-- Dummy element to push buttons to the right side -->
                    <span v-if="props.item.approved"></span>
                    <v-btn
                      v-if="isMe(props.item.creator)"
                      class="side-button ma-0"
                      icon
                      @click.stop="remove(props.item)"
                    >
                      <v-icon color="#FF6347">delete</v-icon>
                    </v-btn>

                    <!-- APPROVE BUTTONS -->
                    <v-btn
                      v-if="canApprove(props.item)"
                      class="side-button ma-0"
                      icon
                      @click.stop="changeApproval(props.item, true)"
                    >
                      <v-icon color="primary">check_circle_outline</v-icon>
                    </v-btn>
                    <v-btn
                      v-if="canRevokeApprove(props.item)"
                      class="side-button ma-0"
                      icon
                      @click.stop="changeApproval(props.item, false)"
                    >
                      <v-icon color="#FF6347">highlight_off</v-icon>
                    </v-btn>
                  </div>
                  <v-card>
                    <v-card-text class="grey lighten-3">
                      <prism class="code" language="java">{{ checkTexts[props.item.id] }}</prism>
                    </v-card-text>
                  </v-card>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-flex>
          </v-data-iterator>
        </v-card-text>
        <v-alert type="error" :value="error.length > 0">{{ error }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
/// <reference path="../vue-prism-component.d.ts" />

import Vue from "vue";
import Component from "vue-class-component";
import { Store } from "vuex";
import { RootState } from "@/store/types";
import Axios from "axios";
import { extractErrorMessage } from "@/util/requests";
import "prismjs";
import "prismjs/themes/prism.css";
require("prismjs/components/prism-java.min.js");
import Prism from "vue-prism-component";

class CheckBase {
  id: number;
  creator: string;
  name: string;
  approved: boolean;

  constructor(id: number, creator: string, name: string, approved: boolean) {
    this.id = id;
    this.creator = creator;
    this.name = name;
    this.approved = approved;
  }
}

class Check extends CheckBase {
  text: string;

  constructor(
    id: number,
    creator: string,
    name: string,
    approved: boolean,
    text: string
  ) {
    super(id, creator, name, approved);
    this.text = text;
  }
}

@Component({
  components: {
    prism: Prism
  }
})
export default class CheckList extends Vue {
  private checks: Array<CheckBase> = [];
  private error: string = "";
  private search: string = "";
  private rowsPerPageItems = [4, 8, 12];
  private pagination = {
    rowsPerPage: 4
  };
  private checkTexts: any = {};

  get userState() {
    return (this.$store as Store<RootState>).state.user;
  }

  isMe(creator: string): boolean {
    if (this.userState.isAdmin()) {
      return true;
    }
    return this.userState.displayName == creator;
  }

  canApprove(check: Check) {
    return this.userState.isAdmin() && !check.approved;
  }

  canRevokeApprove(check: Check) {
    return this.userState.isAdmin() && check.approved;
  }

  remove(check: Check) {
    if (!confirm("Delete check: " + check.name + " (" + check.id + ")")) {
      return;
    }
    Axios.delete("/checks/remove/" + check.id)
      .then(response => {
        this.error = "";

        const index = this.checks.indexOf(check);
        if (index >= 0) {
          this.checks.splice(index, 1);
          delete this.checkTexts[check.id];
        }
      })
      .catch(error => (this.error = extractErrorMessage(error)));
  }

  changeApproval(check: CheckBase, approved: boolean) {
    const formData = new FormData();
    formData.append("id", check.id.toString());
    formData.append("approved", approved ? "true" : "false");
    Axios.post("/checks/approve", formData)
      .then(response => {
        check.approved = approved;
        this.error = "";
      })
      .catch(error => {
        this.error = extractErrorMessage(error);
      });
  }

  checkOpened(checkBase: CheckBase) {
    if (this.checkTexts[checkBase.id] !== undefined) {
      return;
    }
    Axios.get("/checks/get", {
      params: {
        id: checkBase.id
      }
    })
      .then(response => {
        this.checkTexts[checkBase.id] = response.data.text;
        this.error = "";
      })
      .catch(error => (this.error = extractErrorMessage(error)));
  }

  filterValueHandleApproved(val: any, search: string) {
    if (typeof val === "boolean") {
      return (
        (search === "approved" && val) || (search === "unapproved" && !val)
      );
    }
    return (
      val != null &&
      val
        .toString()
        .toLowerCase()
        .indexOf(search) !== -1
    );
  }

  mounted() {
    Axios.get("/checks/get-all")
      .then(response => {
        this.checks = response.data as Array<CheckBase>;
        this.error = "";
        const scratchObject = {} as any;
        this.checks.forEach(it => (scratchObject[it.id] = undefined));
        // This is needed as vue can not observe property addition/deletion
        // So we just build the full object and then assign to to vue (and making it reactive)
        this.checkTexts = scratchObject;
      })
      .catch(error => (this.error = extractErrorMessage(error)));
  }
}
</script>

<style scoped>
.check-header {
  justify-content: space-between;
  align-items: center;
}
.check-header > .side-button,
.check-header > .left-side-text {
  flex: none !important;
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
.unapproved {
  color: tomato;
}
</style>
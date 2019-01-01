<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm8 md6>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>All checks</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-expansion-panel expand>
            <v-expansion-panel-content v-for="(check, i) in checks" :key="i">
              <div slot="header" class="monospaced subheading d-flex check-header">
                <span
                  class="left-side-text"
                >Check '{{ check.name }}' by '{{ check.creator }}' (ID: {{ check.id }})</span>
                <span class="pl-2 unapproved" v-if="!check.approved">Unapproved</span>
                <!-- Dummy element to push buttons to the right side -->
                <span v-if="check.approved"></span>
                <v-btn
                  v-if="isMe(check.creator)"
                  class="side-button ma-0"
                  icon
                  @click.stop="remove(check)"
                >
                  <v-icon color="#FF6347">delete</v-icon>
                </v-btn>

                <!-- APPROVE BUTTONS -->
                <v-btn
                  v-if="canApprove(check)"
                  class="side-button ma-0"
                  icon
                  @click.stop="changeApproval(check, true)"
                >
                  <v-icon color="primary">check_circle_outline</v-icon>
                </v-btn>
                <v-btn
                  v-if="canRevokeApprove(check)"
                  class="side-button ma-0"
                  icon
                  @click.stop="changeApproval(check, false)"
                >
                  <v-icon color="#FF6347">highlight_off</v-icon>
                </v-btn>
              </div>
              <v-card>
                <v-card-text class="grey lighten-3">
                  <prism class="code" language="java">{{ check.text }}</prism>
                </v-card-text>
              </v-card>
            </v-expansion-panel-content>
          </v-expansion-panel>
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

class Check {
  id: number;
  creator: string;
  text: string;
  name: string;
  approved: boolean;

  constructor(
    id: number,
    creator: string,
    text: string,
    name: string,
    approved: boolean
  ) {
    this.id = id;
    this.creator = creator;
    this.text = text;
    this.name = name;
    this.approved = approved;
  }
}

@Component({
  components: {
    prism: Prism
  }
})
export default class CheckList extends Vue {
  private checks: Array<Check> = [];
  private error: string = "";

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
        }
      })
      .catch(error => (this.error = extractErrorMessage(error)));
  }

  changeApproval(check: Check, approved: boolean) {
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

  mounted() {
    Axios.get("/checks/get-all")
      .then(response => {
        this.checks = response.data as Array<Check>;
        this.error = "";
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
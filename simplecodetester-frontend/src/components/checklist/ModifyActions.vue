<template>
  <div class="d-flex" id="wrapper">
    <v-btn
      v-if="false && canModifyCheck(myCheck.creator)"
      class="side-button ma-0"
      icon
      @click.stop="edit(myCheck)"
    >
      <v-icon>edit</v-icon>
    </v-btn>
    <v-btn
      v-if="canModifyCheck(myCheck.creator)"
      class="side-button ma-0"
      icon
      @click.stop="remove(myCheck)"
    >
      <v-icon color="#FF6347">delete</v-icon>
    </v-btn>

    <!-- APPROVE BUTTONS -->
    <v-btn
      v-if="canApprove(myCheck)"
      class="side-button ma-0"
      icon
      @click.stop="changeApproval(myCheck, true)"
    >
      <v-icon color="primary">check_circle_outline</v-icon>
    </v-btn>
    <v-btn
      v-if="canRevokeApprove(myCheck)"
      class="side-button ma-0"
      icon
      @click.stop="changeApproval(myCheck, false)"
    >
      <v-icon color="#FF6347">highlight_off</v-icon>
    </v-btn>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { CheckBase, Check } from "@/components/checklist/types";
import { Prop } from "vue-property-decorator";
import { UserState } from "@/store/types";
import Axios from "axios";
import { extractErrorMessage } from "@/util/requests";

@Component
export default class ModifyActions extends Vue {
  @Prop()
  private userState!: UserState;

  @Prop()
  private checks!: Array<CheckBase>;

  @Prop()
  private myCheck!: CheckBase;

  @Prop()
  private checkTexts!: any;

  emitError(message: string) {
    this.$emit("error", message);
  }

  canModifyCheck(creator: string): boolean {
    if (this.userState.isAdmin()) {
      return true;
    }
    return this.userState.displayName == creator;
  }

  canApprove(check: CheckBase) {
    return this.userState.isAdmin() && !check.approved;
  }

  canRevokeApprove(check: CheckBase) {
    return this.userState.isAdmin() && check.approved;
  }

  remove(check: CheckBase) {
    if (!confirm("Delete check: " + check.name + " (" + check.id + ")")) {
      return;
    }
    Axios.delete("/checks/remove/" + check.id)
      .then(response => {
        this.emitError("");

        const index = this.checks.indexOf(check);
        if (index >= 0) {
          this.checks.splice(index, 1);
          delete this.checkTexts[check.id];
        }
      })
      .catch(error => this.emitError(extractErrorMessage(error)));
  }

  changeApproval(check: CheckBase, approved: boolean) {
    const formData = new FormData();
    formData.append("id", check.id.toString());
    formData.append("approved", approved ? "true" : "false");
    Axios.post("/checks/approve", formData)
      .then(response => {
        check.approved = approved;
        this.emitError("");
      })
      .catch(error => this.emitError(extractErrorMessage(error)));
  }
}
</script>

<style scoped>
.side-button,
.left-side-text {
  flex: none !important;
}

#wrapper {
  justify-content: end;
}
</style>

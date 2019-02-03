<template>
  <div class="d-flex" id="wrapper">
    <span class="pr-4 unapproved aside" v-if="!myCheck.approved">Unapproved</span>

    <v-dialog
      v-model="editDialogOpened"
      v-if="isIoCheck && canModifyCheck(myCheck.creator)"
      class="aside"
      max-width="500"
    >
      <v-btn slot="activator" icon class="ma-0">
        <v-icon>edit</v-icon>
      </v-btn>
      <v-card>
        <v-card-text>
          <io-check-component v-if="isIoCheck" :initialValue="ioCheck"
                              @input="setCheck"></io-check-component>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn :disabled="!canUploadEdit" color="primary" @click="changeCheck">Submit change</v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-btn
      v-if="canModifyCheck(myCheck.creator)"
      class="aside ma-0"
      icon
      @click.stop="remove(myCheck)"
    >
      <v-icon color="#FF6347">delete</v-icon>
    </v-btn>

    <!-- APPROVE BUTTONS -->
    <v-btn
      v-if="canApprove(myCheck)"
      class="aside ma-0"
      icon
      @click.stop="changeApproval(myCheck, true)"
    >
      <v-icon color="primary">check_circle_outline</v-icon>
    </v-btn>
    <v-btn
      v-if="canRevokeApprove(myCheck)"
      class="aside ma-0"
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
  import {CheckBase, CheckCollection, IOCheck} from "@/components/checklist/CheckTypes";
  import {Prop, Watch} from "vue-property-decorator";
  import {UserState} from "@/store/types";
  import {extractErrorMessage} from "@/util/requests";
  import IOCheckComponent from "@/components/checksubmit/IOCheckComponent.vue";

  @Component({
  components: {
    "io-check-component": IOCheckComponent
  }
})
export default class ModifyActions extends Vue {
  private check: any = null;
    private ioCheck: IOCheck | null = null;
  private editDialogOpened = false;
    private checkClass: string | null = null;

  @Prop()
  private userState!: UserState;

  @Prop()
  private checks!: CheckCollection;

  @Prop()
  private myCheck!: CheckBase;

  get isIoCheck() {
    return this.myCheck.name;
  }

  get canUploadEdit() {
    return (
      this.check != null &&
      this.check.name != null &&
      this.check.name.length > 0
    );
  }

  @Watch("editDialogOpened")
  onEditDialogOpened() {
    if (!this.editDialogOpened) {
      return;
    }
    this.checks.fetchContent(this.myCheck).then(content => {
      let check = content.check as IOCheck;
      this.ioCheck = new IOCheck(check.input, check.output, check.name);
      this.checkClass = content.class;
    });
  }

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
    this.checks
      .deleteCheck(check)
      .then(() => this.emitError(""))
      .catch(error => this.emitError(extractErrorMessage(error)));
  }

  changeApproval(check: CheckBase, approved: boolean) {
    this.checks
      .setCheckApproval(check, approved)
      .then(() => this.emitError(""))
      .catch(error => this.emitError(extractErrorMessage(error)));
  }

  setCheck(check: IOCheck) {
    this.check = check;
  }

  changeCheck() {
    if (this.check == null) {
      return;
    }
    this.checks
    .updateIoCheck(this.check as IOCheck, this.myCheck.id, this.checkClass!)
      .then(() => {
        this.emitError("");
        this.editDialogOpened = false;
      })
      .catch(error => this.emitError(extractErrorMessage(error)));
  }
}
</script>

<style scoped>
.aside {
  flex: none !important;
}

#wrapper {
  justify-content: flex-end;
  align-items: center;
}

.unapproved {
  color: tomato;
}
</style>

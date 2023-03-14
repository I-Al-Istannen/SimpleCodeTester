<template>
  <!-- Prevent clicks from expanding the wrapper it is in (the click.stop) -->
  <div class="d-flex" id="wrapper" @click.stop="() => {}">
    <span class="pr-4 unapproved aside" v-if="!myCheck.approved">Unapproved</span>

    <v-dialog v-model="editDialogOpened" v-show="isIoCheck" class="aside" max-width="900">
      <template v-slot:activator="{ on }">
        <v-btn v-on="on" icon class="ma-0">
          <v-icon>{{ canModifyCheck(myCheck.creator) ? editIcon : viewRawIcon }}</v-icon>
        </v-btn>
      </template>
      <v-card>
        <v-card-actions class="pa-0 ma-0 top-actions">
          <v-spacer></v-spacer>
          <v-btn @click="editDialogOpened = false" icon>
            <v-icon>{{ closeIcon }}</v-icon>
          </v-btn>
        </v-card-actions>
        <v-card-text class="pt-2">
          <io-check-component
            v-if="isIoCheck"
            :readOnly="!canModifyCheck(myCheck.creator)"
            :initialValue="ioCheck"
            @input="setCheck"
          ></io-check-component>
        </v-card-text>
        <v-card-actions class="sticky-bottom">
          <v-spacer></v-spacer>
          <v-btn
            v-if="canModifyCheck(myCheck.creator)"
            :disabled="!canUploadEdit"
            color="primary"
            @click="changeCheck"
          >Submit change</v-btn>
          <v-spacer></v-spacer>
        </v-card-actions>
        <v-alert type="error" :value="error.length > 0">{{ error }}</v-alert>
      </v-card>
    </v-dialog>

    <v-btn
      v-show="canDeleteCheck(myCheck.creator)"
      class="aside ma-0"
      icon
      @click.stop="remove(myCheck)"
    >
      <v-icon color="#FF6347">{{ deleteIcon }}</v-icon>
    </v-btn>

    <!-- APPROVE BUTTONS -->
    <v-btn
      v-show="canApprove(myCheck)"
      class="aside ma-0"
      icon
      @click.stop="changeApproval(myCheck, true)"
    >
      <v-icon color="primary">{{ approvedIcon }}</v-icon>
    </v-btn>
    <v-btn
      v-show="canRevokeApprove(myCheck)"
      class="aside ma-0"
      icon
      @click.stop="changeApproval(myCheck, false)"
    >
      <v-icon color="#FF6347">{{ unapprovedIcon }}</v-icon>
    </v-btn>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import {
  CheckBase,
  CheckCollection,
  IOCheck
} from "@/components/checklist/CheckTypes";
import { Prop, Watch } from "vue-property-decorator";
import { UserState } from "@/store/types";
import { extractErrorMessage } from "@/util/requests";
import IOCheckComponent from "@/components/checksubmit/IOCheckComponent.vue";
import {
  mdiCheckCircleOutline,
  mdiCloseCircleOutline,
  mdiDelete,
  mdiPencil,
  mdiFormatText,
  mdiClose
} from "@mdi/js";

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

  @Prop({ default: "" })
  error!: string;

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
      this.ioCheck = new IOCheck(
        check.input,
        check.output,
        check.name,
        check.files
      );
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
    if (this.userState.isEditor()) {
      return true;
    }
    return this.userState.userName == creator;
  }

  canDeleteCheck(creator: string): boolean {
    if (this.userState.isAdmin()) {
      return true;
    }
    if (this.userState.isEditor()) {
      return true;
    }
    return this.userState.userName == creator;
  }

  canApprove(check: CheckBase) {
    return (
      (this.userState.isAdmin() || this.userState.isEditor()) && !check.approved
    );
  }

  canRevokeApprove(check: CheckBase) {
    return (
      (this.userState.isAdmin() || this.userState.isEditor()) && check.approved
    );
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

  // ICONS
  private deleteIcon = mdiDelete;
  private editIcon = mdiPencil;
  private approvedIcon = mdiCheckCircleOutline;
  private unapprovedIcon = mdiCloseCircleOutline;
  private viewRawIcon = mdiFormatText;
  private closeIcon = mdiClose;
}
</script>

<style scoped>
.aside {
  flex: none !important;
}

.top-actions {
  position: absolute;
  top: 8px;
  right: 8px;
}

#wrapper {
  justify-content: flex-end;
  align-items: center;
}

.unapproved {
  color: tomato;
}

.sticky-bottom {
  position: sticky;
  bottom: 0px;
}
</style>

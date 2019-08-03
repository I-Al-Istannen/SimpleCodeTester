<template>
  <!-- Prevent clicks from expanding the wrapper it is in (the click.stop) -->
  <span @click.stop="() => {}" @updated="updateElement">
    <slot name="preActions"></slot>

    <!-- Additional items in an edit menu -->
    <v-menu offset-y v-if="customMenuActionsSet">
      <template v-slot:activator="{ on }">
        <v-btn v-on="on" text>
          <slot name="menuActivator">
            <v-icon>{{ editIcon }}</v-icon>
          </slot>
        </v-btn>
      </template>

      <v-list>
        <slot name="customMenuActions"></slot>
      </v-list>
    </v-menu>

    <!-- Additional buttons not in menu -->
    <slot name="customActions"></slot>

    <v-btn class="ma-0" icon @click.stop="deleteElement">
      <v-icon color="#FF6347">{{ deleteIcon }}</v-icon>
    </v-btn>
  </span>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";
import { CrudRepository, Identifiable } from "@/components/crud/CrudTypes";
import { extractErrorMessage } from "@/util/requests";
import { mdiDelete, mdiPencil } from "@mdi/js";

/**
 * Slots:
 * * "menuActivator" -- the button opening the menu. Default: "edit" icon
 * * "customMenuActions" -- the actions in that menu
 * * "customActions" -- additional custom actions
 */
@Component
export default class CrudModifyActions<
  T extends Identifiable,
  A extends Identifiable
> extends Vue {
  @Prop()
  private repository!: CrudRepository<T, A>;

  @Prop()
  private element!: T;

  get customMenuActionsSet(): boolean {
    const customActionsSlot = this.$slots.customMenuActions;

    if (!customActionsSlot || !customActionsSlot[0]) {
      return false;
    }
    return true;
  }

  deleteElement() {
    if (!confirm("Do you really want to delete this item?")) {
      return;
    }
    this.handlePromise(this.repository.deleteItem(this.element));
  }

  updateElement(newElement: A) {
    this.handlePromise(this.repository.updateItem(newElement));
  }

  handlePromise(promise: Promise<any>) {
    promise
      .then(success => this.emitError(""))
      .catch(error => this.emitError(extractErrorMessage(error)))
      .finally(() => this.$emit("requestFinished"));
  }

  emitError(error: string) {
    this.$emit("error", error);
  }

  // Icons
  private deleteIcon = mdiDelete;
  private editIcon = mdiPencil;
}
</script>


<style scoped>
</style>

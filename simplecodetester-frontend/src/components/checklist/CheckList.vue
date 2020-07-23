<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>All checks</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            :append-icon="searchIcon"
            label="Search..."
            single-line
          ></v-text-field>

          <v-data-iterator
            :items="checks.checkBases"
            :footer-props="footerProps"
            :search="search"
            column
            style="overflow-x: auto;"
            wrap
            :custom-filter="filterHandlingApproved"
            :items-per-page="rowsPerPage"
            @update:items-per-page="setRowsPerPage"
          >
            <template v-slot:default="props">
              <v-layout column>
                <v-expansion-panels inset multiple>
                  <v-expansion-panel
                    v-for="item in props.items"
                    :key="item.id"
                    @change="fetchCheckText(item)"
                  >
                    <v-expansion-panel-header
                      class="monospaced subtitle-1 d-flex check-header py-0"
                    >
                      <span>Check '{{ item.name }}' by '{{ item.creator }}' (ID: {{ item.id }})</span>
                      <modify-actions
                        :checks="checks"
                        :userState="userState"
                        :myCheck="item"
                        @error="setError"
                        :error="error"
                      ></modify-actions>
                    </v-expansion-panel-header>
                    <v-expansion-panel-content>
                      <v-card>
                        <v-card-text class="check-background">
                          <check-display :checkBase="item" :content="checks.checkContents[item.id]"></check-display>
                        </v-card-text>
                      </v-card>
                    </v-expansion-panel-content>
                  </v-expansion-panel>
                </v-expansion-panels>
              </v-layout>
            </template>
          </v-data-iterator>
        </v-card-text>
        <v-alert type="error" :value="error.length > 0">{{ error }}</v-alert>
      </v-card>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Store } from "vuex";
import { RootState } from "@/store/types";
import { extractErrorMessage } from "@/util/requests";
import ModifyActions from "@/components/checklist/CheckModifyActions.vue";
import { CheckBase, CheckCollection } from "@/components/checklist/CheckTypes";
import CheckDisplay from "@/components/checklist/CheckDisplay.vue";
import { Watch } from "vue-property-decorator";
import { mdiMagnify } from "@mdi/js";

@Component({
  components: {
    "modify-actions": ModifyActions,
    "check-display": CheckDisplay,
  },
})
export default class CheckList extends Vue {
  private checks: CheckCollection = new CheckCollection();
  private error: string = "";
  private search: string = "";
  private footerProps = {
    itemsPerPageOptions: [4, 10, 20, 50, 100],
    pagination: {
      rowsPerPage: this.rowsPerPage,
    },
  };

  get rowsPerPage(): number {
    return (this.$store.state as RootState).miscsettings.itemsPerPage;
  }

  @Watch("footerProps.pagination.rowsPerPage", { deep: true })
  setRowsPerPage(rows: number) {
    this.$store.commit("miscsettings/setItemsPerPage", rows);
  }

  get userState() {
    return (this.$store as Store<RootState>).state.user;
  }

  setError(error: string) {
    this.error = error;
  }

  fetchCheckText(checkBase: CheckBase) {
    this.checks
      .fetchContent(checkBase)
      .then(() => this.setError(""))
      .catch((error) => this.setError(extractErrorMessage(error)));
  }

  filterHandlingApproved(items: any[], search: string): any[] {
    return items.filter((val) => {
      return this.checkMatchesSearch(val, search);
    });
  }

  checkMatchesSearch(check: CheckBase, search: string) {
    search = search.toLowerCase();
    if ("approved".startsWith(search) && check.approved) {
      return true;
    }
    if ("unapproved".startsWith(search) && !check.approved) {
      return true;
    }
    if (check.name.toLowerCase().indexOf(search) !== -1) {
      return true;
    }
    if (check.creator.toLowerCase().indexOf(search) !== -1) {
      return true;
    }
    if (check.category.name.toLowerCase().indexOf(search) !== -1) {
      return true;
    }
    if (check.id.toString().indexOf(search) !== -1) {
      return true;
    }
    return false;
  }

  mounted() {
    this.checks
      .fetchAll()
      .then(() => this.setError(""))
      .catch((error) => this.setError(extractErrorMessage(error)));
  }

  // ICONS
  private searchIcon = mdiMagnify;
}
</script>

<style scoped>
.check-background {
  background-color: var(--v-check_background-base);
}
.check-header {
  justify-content: space-between;
  align-items: center;
}
</style>

<style>
.v-data-iterator {
  overflow-x: never;
}
</style>
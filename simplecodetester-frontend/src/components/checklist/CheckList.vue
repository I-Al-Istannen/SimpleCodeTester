<template>
  <v-layout align-center justify-center>
    <v-flex xs12 sm10 md8>
      <v-card class="elevation-12">
        <v-toolbar dark color="primary">
          <v-toolbar-title>All checks</v-toolbar-title>
        </v-toolbar>
        <v-card-text>
          <v-row no-gutters style="flex-wrap: nowrap" align="center" class="mx-4">
            <v-col cols="auto">
              <v-icon>{{ filterCategoryIcon }}</v-icon>
            </v-col>
            <v-col cols="auto">
              <v-chip-group v-model="selectedCategories" multiple show-arrows>
                <v-chip
                  v-for="category in allCategories"
                  filter
                  :key="category.id"
                  :value="category"
                  :outlined="!selectedCategories.includes(category)"
                >{{ category.name }}</v-chip>
              </v-chip-group>
            </v-col>
          </v-row>

          <v-text-field
            class="mx-5 mb-2"
            v-model="search"
            :append-icon="searchIcon"
            label="Search for category, id, check name, author or update time ('after <ISO8601>')..."
            single-line
          ></v-text-field>

          <v-data-iterator
            :items="filteredChecks"
            :footer-props="footerProps"
            :search="search"
            item-key="id"
            column
            style="overflow-x: auto;"
            wrap
            :custom-filter="filterHandlingApproved"
            :items-per-page="rowsPerPage"
            @update:items-per-page="setRowsPerPage"
            @update:page="openedItems = []"
          >
            <template v-slot:default="props">
              <v-layout column>
                <v-expansion-panels inset multiple v-model="openedItems">
                  <v-expansion-panel
                    v-for="item in props.items"
                    :key="item.id"
                    @change="fetchCheckText(item)"
                  >
                    <v-expansion-panel-header
                      class="monospaced subtitle-1 d-flex check-header py-0"
                    >
                      <span v-if="item.creator != null">
                        Check '{{ item.name }}' by '{{ item.creator }}' (ID: {{ item.id }})
                      </span>
                      <span v-else>
                        Check '{{ item.name }}' (ID: {{ item.id }})
                      </span>
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
import { CheckCategory, RootState } from "@/store/types";
import { extractErrorMessage } from "@/util/requests";
import ModifyActions from "@/components/checklist/CheckModifyActions.vue";
import { CheckBase, CheckCollection } from "@/components/checklist/CheckTypes";
import CheckDisplay from "@/components/checklist/CheckDisplay.vue";
import { Watch } from "vue-property-decorator";
import { mdiFilterPlusOutline, mdiMagnify } from "@mdi/js";

@Component({
  components: {
    "modify-actions": ModifyActions,
    "check-display": CheckDisplay
  }
})
export default class CheckList extends Vue {
  private checks: CheckCollection = new CheckCollection();
  private selectedCategories: CheckCategory[] = [];
  private error: string = "";
  private search: string = "";
  private footerProps = {
    itemsPerPageOptions: [4, 10, 20, 50, 100],
    pagination: {
      rowsPerPage: this.rowsPerPage
    }
  };
  // needed to reset open expansion panels on page navigation
  private openedItems: number[] = [];

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

  private get allCategories() {
    return (this.$store.state as RootState).checkcategory.categories
      .slice()
      .sort((a, b) => b.id - a.id);
  }

  private get filteredChecks() {
    if (this.selectedCategories.length === 0) {
      return this.checks.getCheckBases();
    }
    const allowedCategories = new Set(this.selectedCategories.map(it => it.id));
    return this.checks
      .getCheckBases()
      .filter(it => allowedCategories.has(it.category.id));
  }

  fetchCheckText(checkBase: CheckBase) {
    this.checks
      .fetchContent(checkBase)
      .then(() => this.setError(""))
      .catch(error => this.setError(extractErrorMessage(error)));
  }

  filterHandlingApproved(items: any[], search: string): any[] {
    return items.filter(val => {
      return this.checkMatchesSearch(val, search);
    });
  }

  private containsWordsInAnyOrder(needle: string, haystack: string): boolean {
    const words = needle.split(" ");

    for (const word of words) {
      if (!haystack.includes(word)) {
        return false;
      }
    }
    return true;
  }

  private filterCategory(search: string, categoryName: string): boolean {
    const words: string[] = [];

    let soFar = "";
    for (const char of search) {
      if (char === " " && soFar !== "final" && soFar !== "task") {
        words.push(soFar);
        soFar = "";
      } else {
        soFar += char;
      }
    }
    words.push(soFar);

    for (const word of words) {
      if (!categoryName.includes(word)) {
        return false;
      }
    }
    return true;
  }

  checkMatchesSearch(check: CheckBase, search: string) {
    search = search.toLowerCase();
    if ("approved".startsWith(search) && check.approved) {
      return true;
    }
    if ("unapproved".startsWith(search) && !check.approved) {
      return true;
    }
    if (this.containsWordsInAnyOrder(search, check.name.toLowerCase())) {
      return true;
    }
    if (check.creator && this.containsWordsInAnyOrder(search, check.creator.toLowerCase())) {
      return true;
    }
    if (this.filterCategory(search, check.category.name.toLowerCase())) {
      return true;
    }
    if (check.id.toString().indexOf(search) !== -1) {
      return true;
    }
    if (search.startsWith("after ")) {
      const givenTimeMS = Date.parse(search.replace("after ", "").trim());
      if (!isNaN(givenTimeMS) && check.updateTimeMS >= givenTimeMS) {
        return true;
      }
    }
    return false;
  }

  mounted() {
    this.checks
      .fetchAll()
      .then(() => this.setError(""))
      .catch(error => this.setError(extractErrorMessage(error)));

    this.$store.dispatch("checkcategory/fetchAll");
  }

  // ICONS
  private searchIcon = mdiMagnify;
  private filterCategoryIcon = mdiFilterPlusOutline;
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

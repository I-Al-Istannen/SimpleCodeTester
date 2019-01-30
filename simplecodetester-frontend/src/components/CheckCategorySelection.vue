<template>
  <v-select
    :value="checkCategory"
    @input="updateCategory"
    outline
    :items="allCheckCategories"
    label="Check category"
  >
    <template slot="selection" slot-scope="data">
      <span>{{ data.item.name }}</span>
    </template>
    <template slot="item" slot-scope="data">{{ data.item.name }}</template>
  </v-select>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Store } from "vuex";
import { RootState, CheckCategoryState, CheckCategory } from "@/store/types";
import { Prop, Watch } from "vue-property-decorator";

@Component
export default class CheckCategorySelection extends Vue {
  @Prop({ default: null })
  checkCategory!: CheckCategory | null;

  updateCategory(newCategory: CheckCategory) {
    this.$emit("input", newCategory);
  }

  get allCheckCategories() {
    return (this.$store as Store<RootState>).state.checkcategory.categories;
  }

  mounted() {
    this.$store.dispatch("checkcategory/fetchAll");
  }
}
</script>

<style scoped>
</style>

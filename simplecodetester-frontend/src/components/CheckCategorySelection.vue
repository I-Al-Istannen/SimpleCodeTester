<template>
  <v-select
    :value="checkCategory"
    @input="updateCategory"
    outlined
    :items="allCheckCategories"
    label="Check category"
    item-text="name"
    item-value="id"
  ></v-select>
</template>

<script lang="ts">
import Vue from "vue";
import Component from "vue-class-component";
import { Store } from "vuex";
import { CheckCategory, RootState } from "@/store/types";
import { Prop } from "vue-property-decorator";

@Component
export default class CheckCategorySelection extends Vue {
  @Prop({ default: null })
  checkCategory!: CheckCategory | null;

  updateCategory(newCategoryId: number) {
    let newCategory = this.allCheckCategories.filter(
      it => it.id === newCategoryId
    )[0];
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

import Vue from "vue";
import Vuetify, { VLayout } from "vuetify/lib";

Vue.use(Vuetify);

export default new Vuetify({
  icons: {
    iconfont: "mdiSvg",
  },
  components: {
    VLayout,
  },
  theme: {
    options: {
      customProperties: true,
    },
    dark: false,
    themes: {
      light: {
        primary: "#4CAF50",
        accent: "#E040FB",
        red: "#FF6347",
        line_input: "#4169E1", // royalblue
        line_parameter: "#808080", // gray
        line_output: "#455a64",
        line_error: "#FF6347", // tomato
        line_other: "#008000", // green
        line_prefix: "#999999", // lightgray
        highlighted_button_color: "#4169e1",
        check_background: "#eeeeee",
        code_border: "#E040FB",
        muted: "#848484"
      },
      dark: {
        primary: "#4CAF50",
        secondary: "#c51162",
        accent: "#f8f32b",
        line_input: "#81f499",
        line_parameter: "#808080", // geay
        line_output: "#3da35d",
        line_error: "#FF6347", // tomato
        line_other: "#5da0e4",
        line_prefix: "#7c8c8f",
        highlighted_button_color: "#e980fc",
        check_background: "#303030",
        code_border: "#808080",
        muted: "#7c8c8f"
      },
    },
  },
});

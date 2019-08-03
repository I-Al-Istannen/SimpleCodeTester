import Vue from 'vue';
import Vuetify, {VLayout} from 'vuetify/lib';

Vue.use(Vuetify)

export default new Vuetify({
  icons: {
    iconfont: 'mdiSvg',
  },
  components: {
    VLayout
  },
  theme: {
    dark: false,
    themes: {
      light: {
        primary: '#4CAF50',
        accent: '#E040FB',
        red: '#FF6347'
      },
    }
  }
});

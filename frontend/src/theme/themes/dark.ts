import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const darkTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm ,
  token: {
    fontFamily: "'Exo 2', sans-serif",
    colorTextBase:"#efefef",
    colorTextHeading: "#f0f0f0",

  },
  components: {
   Layout: {
    headerBg: "#1b1b1B",
    headerHeight: 48,

   }
  }
};

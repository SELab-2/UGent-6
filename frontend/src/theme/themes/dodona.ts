import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const dodonaTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm ,
  token: {
    fontFamily: "'Exo 2', sans-serif",
    colorBgLayout: '#3c3b3f',
    colorTextHeading: "#9ccaff",
    colorPrimary: "#00497F"
  },
  components: {
    Layout: {
      headerBg: "#9ccaff",
      headerHeight: 48,

    },
    Button: {
      primaryColor: "#D0E4FF"
    },
  }
};

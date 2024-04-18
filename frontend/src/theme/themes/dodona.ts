import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const dodonaTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm ,
  token: {
    fontFamily: "'Exo 2', sans-serif",
    colorBgLayout: '#3c3b3f',
    colorTextHeading: "#D0E4FF",
    colorPrimary: "#9CCAFF",
    colorBgContainer: "#303034"
  },
  components: {
    Layout: {
      headerBg: "#9ccaff",
      headerHeight: 48,
    },
    Card: {
      headerBg: "rgba(255, 255, 255, 0.1)",
    },
    Button: {
      primaryColor: "#D0E4FF"
    },
  }
};

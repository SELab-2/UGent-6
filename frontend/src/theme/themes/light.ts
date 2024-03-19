import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const lightTheme: ThemeConfig = {
  algorithm: theme.defaultAlgorithm,
  token: {
    fontFamily: "'Exo 2', sans-serif",
    colorBgLayout: "#FBF8FD",
    colorBgBase: "#FEFBFF",
    colorPrimaryBg: "#D0E3FE",
    colorBorder: "rgba(0, 0, 0, 0.2)",
  },
  components: {
    Layout: {
      headerBg: "#1D64C7",
      headerHeight: 48,
    }

  }
};

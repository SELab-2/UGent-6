import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const lightTheme: ThemeConfig = {
  algorithm: theme.defaultAlgorithm,
  token: {
    colorBgElevated: "#FBFBFA",
  },
  components: {
    Layout: {
      headerBg: "#E9E9EA",
      headerHeight: 48,
    },

  }
};

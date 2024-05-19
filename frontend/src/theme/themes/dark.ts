import { theme } from 'antd';
import { ThemeConfig } from 'antd/es/config-provider';

export const darkTheme: ThemeConfig = {
  algorithm: theme.darkAlgorithm ,
  token: {
    fontFamily: "'Exo 2', sans-serif",
    colorTextBase:"#efefef",
    colorTextHeading: "#f0f0f0",
    colorPrimaryBg: "#0050b3",
   colorBgLayout:"#141414",
   colorBgBase: "#090909"
  },
  components: {
   Layout: {
    headerBg: "#002b60", // #002766 #1b1b1B
    headerHeight: 48,

   },
   Dropdown: {
    colorPrimary: "#eee"
   },
   Calendar: {
    itemActiveBg: "#002b60"
   },
   Table: {
    headerSortActiveBg:"#252525"
   }
  },
  
};

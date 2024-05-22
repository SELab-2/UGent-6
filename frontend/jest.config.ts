import type { Config } from '@jest/types';

const config: Config.InitialOptions = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleNameMapper: {
    '\\.(jpg|jpeg|png|gif|webp|svg)$': '<rootDir>/__mocks__/fileMock.js',
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    'react-markdown': 'jest-transform-stub',
    'react-syntax-highlighter': 'jest-transform-stub',
    '@azure/msal-react': 'jest-transform-stub',
    '@fontsource/jetbrains-mono': 'jest-transform-stub',
  },
    globals: {
    'ts-jest': {
      tsconfig: 'tsconfig.json',
    },
  },
  transform: {
    // '^.+\\.(ts|tsx)$': 'ts-jest',
    '^.+\\.(js|jsx)$': 'babel-jest',
  },
  transformIgnorePatterns: [
    'node_modules/(?!(jest-)?@azure/msal-react|react-markdown|deck.gl|ng-dynamic)',
  ],
  testMatch: [
    '**/?(*.)+(spec|test).ts?(x)',
  ],
};

export default config;
import { render, screen } from '@testing-library/react';
import AuthNav from '../components/layout/nav/AuthNav';
import "./mocks"

jest.mock('../auth/AuthConfig.ts', () => ({
  msalConfig: {
    clientId: 'mockedClientId',
    authority: "https://login.microsoftonline.com/d7811cde-ecef-496c-8f91-a1786241b99c", //  "https://login.microsoftonline.com/62835335-e5c4-4d22-98f2-9d5b65a06d9d",
    redirectUri: "/",
  },
}));



test('renders learn react link', () => {
  // render(<AuthNav  />);
  // expect(screen.getByText("Pigeonhole")).toBeInTheDocument()


});

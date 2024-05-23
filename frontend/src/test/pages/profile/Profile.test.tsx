import { render, screen } from '@testing-library/react';
import { Profile } from '../../../pages/profile/Profile';
import useUser from '../../../hooks/useUser';

jest.mock('../../../hooks/useUser');

describe('Profile', () => {
  it('renders loading state when user is null', () => {
    (useUser as jest.Mock).mockReturnValue({ user: null });

    render(<Profile />);
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it.todo('renders ProfileCard when user is not null');
  //     , () => {
  //   // const mockUser = { name: 'Test User', email: 'test@example.com' };
  //   // (useUser as jest.Mock).mockReturnValue({ user: mockUser });
  //   //
  //   // render(<Profile />);
  //   // expect(screen.getByText(mockUser.name)).toBeInTheDocument();
  //   // expect(screen.getByText(mockUser.email)).toBeInTheDocument();
  // });
});
import React from 'react'
import ReactDOM from 'react-dom'
import Error from './Error'

// Hulpmethode voor het monteren van de component
// @ts-ignore
const mountComponent = (props) => {
  const div = document.createElement('div');
  ReactDOM.render(<Error {...props} />, div);
  return div;
};

// Testcase
describe('<Error />', () => {
  it('mounts', () => {
    // Monteer de component met de hulpmethode
    mountComponent({ errorCode: 404, errorMessage: "TEST" });
  });
});
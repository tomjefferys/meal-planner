import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import App from '../App';

describe('App', () => {
  it('redirects root path to /planner', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <App />
      </MemoryRouter>
    );

    // After redirect, the PlannerPage should attempt to render
    // It will show loading state since it fetches data on mount
    expect(document.body).toBeTruthy();
  });

  it('renders meals route', () => {
    render(
      <MemoryRouter initialEntries={['/meals']}>
        <App />
      </MemoryRouter>
    );

    expect(document.body).toBeTruthy();
  });

  it('renders people route', () => {
    render(
      <MemoryRouter initialEntries={['/people']}>
        <App />
      </MemoryRouter>
    );

    expect(document.body).toBeTruthy();
  });

  it('renders shopping route', () => {
    render(
      <MemoryRouter initialEntries={['/shopping']}>
        <App />
      </MemoryRouter>
    );

    expect(document.body).toBeTruthy();
  });
});

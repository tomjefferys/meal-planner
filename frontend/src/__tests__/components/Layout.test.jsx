import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Layout from '../../components/Layout';

describe('Layout', () => {
  it('renders sidebar with app title', () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    expect(screen.getByText('🍽️ Meal Planner')).toBeTruthy();
  });

  it('renders navigation links', () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    expect(screen.getAllByText('Weekly Plan').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Meals').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Family').length).toBeGreaterThan(0);
    expect(screen.getAllByText('Shopping').length).toBeGreaterThan(0);
  });

  it('renders correct navigation icons', () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    expect(screen.getAllByText('📅').length).toBeGreaterThan(0);
    expect(screen.getAllByText('🍲').length).toBeGreaterThan(0);
    expect(screen.getAllByText('👨‍👩‍👧‍👦').length).toBeGreaterThan(0);
    expect(screen.getAllByText('🛒').length).toBeGreaterThan(0);
  });

  it('renders navigation links with correct paths', () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>
    );

    const plannerLinks = screen.getAllByText('Weekly Plan');
    expect(plannerLinks[0].closest('a')).toHaveAttribute('href', '/planner');

    const mealsLinks = screen.getAllByText('Meals');
    expect(mealsLinks[0].closest('a')).toHaveAttribute('href', '/meals');

    const familyLinks = screen.getAllByText('Family');
    expect(familyLinks[0].closest('a')).toHaveAttribute('href', '/people');

    const shoppingLinks = screen.getAllByText('Shopping');
    expect(shoppingLinks[0].closest('a')).toHaveAttribute('href', '/shopping');
  });
});

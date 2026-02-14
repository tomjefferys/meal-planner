import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import MealForm from '../../components/MealForm';

describe('MealForm', () => {
  const defaultProps = {
    onSave: vi.fn(),
    onCancel: vi.fn(),
  };

  it('renders empty form for new meal', () => {
    render(<MealForm {...defaultProps} />);

    expect(screen.getByPlaceholderText('e.g. Spaghetti Bolognese')).toHaveValue('');
    expect(screen.getByPlaceholderText('Brief description of the meal...')).toHaveValue('');
    expect(screen.getByText('Add Meal')).toBeTruthy();
  });

  it('renders pre-filled form for editing', () => {
    const meal = {
      title: 'Pasta',
      description: 'Italian pasta',
      prepTimeMinutes: 10,
      cookTimeMinutes: 20,
      effort: 'EASY',
      ingredients: [{ name: 'Pasta', quantity: 500, unit: 'g' }],
    };

    render(<MealForm {...defaultProps} meal={meal} />);

    expect(screen.getByPlaceholderText('e.g. Spaghetti Bolognese')).toHaveValue('Pasta');
    expect(screen.getByText('Update Meal')).toBeTruthy();
  });

  it('calls onCancel when cancel button is clicked', () => {
    const onCancel = vi.fn();
    render(<MealForm onSave={vi.fn()} onCancel={onCancel} />);

    fireEvent.click(screen.getByText('Cancel'));

    expect(onCancel).toHaveBeenCalled();
  });

  it('calls onSave with form data on submit', () => {
    const onSave = vi.fn();
    render(<MealForm onSave={onSave} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByPlaceholderText('e.g. Spaghetti Bolognese'), {
      target: { value: 'New Meal', name: 'title' },
    });

    fireEvent.submit(screen.getByText('Add Meal').closest('form'));

    expect(onSave).toHaveBeenCalledWith(
      expect.objectContaining({ title: 'New Meal' })
    );
  });

  it('adds new ingredient row when clicking add ingredient', () => {
    render(<MealForm {...defaultProps} />);

    const addBtn = screen.getByText('+ Add Ingredient');
    fireEvent.click(addBtn);

    const ingredientInputs = screen.getAllByPlaceholderText('Ingredient name');
    expect(ingredientInputs).toHaveLength(2);
  });

  it('removes ingredient row when clicking remove', () => {
    const meal = {
      title: 'Pasta',
      description: '',
      prepTimeMinutes: null,
      cookTimeMinutes: null,
      effort: 'MEDIUM',
      ingredients: [
        { name: 'Pasta', quantity: 500, unit: 'g' },
        { name: 'Sauce', quantity: 300, unit: 'ml' },
      ],
    };

    render(<MealForm {...defaultProps} meal={meal} />);

    const removeButtons = screen.getAllByTitle('Remove');
    fireEvent.click(removeButtons[0]);

    const ingredientInputs = screen.getAllByPlaceholderText('Ingredient name');
    expect(ingredientInputs).toHaveLength(1);
  });

  it('renders effort dropdown with all options', () => {
    render(<MealForm {...defaultProps} />);

    expect(screen.getByText('Easy')).toBeTruthy();
    expect(screen.getByText('Medium')).toBeTruthy();
    expect(screen.getByText('Hard')).toBeTruthy();
  });

  it('filters empty ingredient names on submit', () => {
    const onSave = vi.fn();
    render(<MealForm onSave={onSave} onCancel={vi.fn()} />);

    // The default form has one empty ingredient row
    fireEvent.change(screen.getByPlaceholderText('e.g. Spaghetti Bolognese'), {
      target: { value: 'Test', name: 'title' },
    });

    fireEvent.submit(screen.getByText('Add Meal').closest('form'));

    expect(onSave).toHaveBeenCalledWith(
      expect.objectContaining({ ingredients: [] })
    );
  });

  it('parses time fields as integers', () => {
    const onSave = vi.fn();
    render(<MealForm onSave={onSave} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByPlaceholderText('e.g. Spaghetti Bolognese'), {
      target: { value: 'Test', name: 'title' },
    });

    const prepInput = screen.getAllByRole('spinbutton')[0];
    fireEvent.change(prepInput, { target: { value: '15', name: 'prepTimeMinutes' } });

    fireEvent.submit(screen.getByText('Add Meal').closest('form'));

    expect(onSave).toHaveBeenCalledWith(
      expect.objectContaining({ prepTimeMinutes: 15 })
    );
  });
});

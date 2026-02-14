import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import PersonForm from '../../components/PersonForm';

describe('PersonForm', () => {
  const defaultProps = {
    onSave: vi.fn(),
    onCancel: vi.fn(),
  };

  it('renders empty form for new person', () => {
    render(<PersonForm {...defaultProps} />);

    expect(screen.getByPlaceholderText('e.g. Mum, Dad, Sarah...')).toHaveValue('');
    expect(screen.getByPlaceholderText('Dietary requirements, likes, dislikes...')).toHaveValue('');
    expect(screen.getByPlaceholderText('What they enjoy cooking, skill level...')).toHaveValue('');
    expect(screen.getByText('Add Family Member')).toBeTruthy();
  });

  it('renders pre-filled form for editing', () => {
    const person = {
      name: 'Mum',
      eatingPreferences: 'Vegetarian',
      cookingPreferences: 'Enjoys baking',
    };

    render(<PersonForm {...defaultProps} person={person} />);

    expect(screen.getByPlaceholderText('e.g. Mum, Dad, Sarah...')).toHaveValue('Mum');
    expect(screen.getByPlaceholderText('Dietary requirements, likes, dislikes...')).toHaveValue('Vegetarian');
    expect(screen.getByText('Update')).toBeTruthy();
  });

  it('calls onCancel when cancel button is clicked', () => {
    const onCancel = vi.fn();
    render(<PersonForm onSave={vi.fn()} onCancel={onCancel} />);

    fireEvent.click(screen.getByText('Cancel'));

    expect(onCancel).toHaveBeenCalled();
  });

  it('calls onSave with form data on submit', () => {
    const onSave = vi.fn();
    render(<PersonForm onSave={onSave} onCancel={vi.fn()} />);

    fireEvent.change(screen.getByPlaceholderText('e.g. Mum, Dad, Sarah...'), {
      target: { value: 'Dad', name: 'name' },
    });
    fireEvent.change(screen.getByPlaceholderText('Dietary requirements, likes, dislikes...'), {
      target: { value: 'No peanuts', name: 'eatingPreferences' },
    });
    fireEvent.change(screen.getByPlaceholderText('What they enjoy cooking, skill level...'), {
      target: { value: 'BBQ expert', name: 'cookingPreferences' },
    });

    fireEvent.submit(screen.getByText('Add Family Member').closest('form'));

    expect(onSave).toHaveBeenCalledWith({
      name: 'Dad',
      eatingPreferences: 'No peanuts',
      cookingPreferences: 'BBQ expert',
    });
  });

  it('handles person with null fields', () => {
    const person = {
      name: null,
      eatingPreferences: null,
      cookingPreferences: null,
    };

    render(<PersonForm {...defaultProps} person={person} />);

    expect(screen.getByPlaceholderText('e.g. Mum, Dad, Sarah...')).toHaveValue('');
  });

  it('shows Update button when editing', () => {
    render(<PersonForm {...defaultProps} person={{ name: 'Alice' }} />);

    expect(screen.getByText('Update')).toBeTruthy();
  });

  it('shows Add Family Member button for new person', () => {
    render(<PersonForm {...defaultProps} />);

    expect(screen.getByText('Add Family Member')).toBeTruthy();
  });
});

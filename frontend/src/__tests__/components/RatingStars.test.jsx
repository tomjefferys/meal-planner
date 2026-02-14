import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import RatingStars from '../../components/RatingStars';

describe('RatingStars', () => {
  it('renders 5 star buttons', () => {
    render(<RatingStars />);

    const stars = screen.getAllByRole('button');
    expect(stars).toHaveLength(5);
  });

  it('highlights active stars based on value', () => {
    render(<RatingStars value={3} readonly />);

    const stars = screen.getAllByRole('button');
    expect(stars[0]).toHaveClass('active');
    expect(stars[1]).toHaveClass('active');
    expect(stars[2]).toHaveClass('active');
    expect(stars[3]).not.toHaveClass('active');
    expect(stars[4]).not.toHaveClass('active');
  });

  it('calls onChange when star is clicked', () => {
    const handleChange = vi.fn();
    render(<RatingStars value={0} onChange={handleChange} />);

    const stars = screen.getAllByRole('button');
    fireEvent.click(stars[3]); // Click the 4th star

    expect(handleChange).toHaveBeenCalledWith(4);
  });

  it('does not call onChange when readonly', () => {
    const handleChange = vi.fn();
    render(<RatingStars value={3} onChange={handleChange} readonly />);

    const stars = screen.getAllByRole('button');
    fireEvent.click(stars[4]);

    expect(handleChange).not.toHaveBeenCalled();
  });

  it('disables buttons when readonly', () => {
    render(<RatingStars value={3} readonly />);

    const stars = screen.getAllByRole('button');
    stars.forEach((star) => {
      expect(star).toBeDisabled();
    });
  });

  it('does not disable buttons when interactive', () => {
    render(<RatingStars value={3} onChange={vi.fn()} />);

    const stars = screen.getAllByRole('button');
    stars.forEach((star) => {
      expect(star).not.toBeDisabled();
    });
  });

  it('applies readonly class when readonly', () => {
    const { container } = render(<RatingStars value={3} readonly />);

    expect(container.firstChild).toHaveClass('readonly');
  });

  it('defaults value to 0 when not provided', () => {
    render(<RatingStars />);

    const stars = screen.getAllByRole('button');
    stars.forEach((star) => {
      expect(star).not.toHaveClass('active');
    });
  });

  it('renders star character ★ in each button', () => {
    render(<RatingStars value={0} />);

    const stars = screen.getAllByRole('button');
    stars.forEach((star) => {
      expect(star.textContent).toBe('★');
    });
  });
});

import { describe, it, expect } from 'vitest';
import { toISODate, addDays, getSaturday } from '../../pages/PlannerPage';

describe('PlannerPage date utilities', () => {
  describe('toISODate', () => {
    it('returns correct local date string regardless of timezone', () => {
      const date = new Date(2026, 3, 12); // April 12, 2026
      expect(toISODate(date)).toBe('2026-04-12');
    });

    it('pads single-digit month and day', () => {
      const date = new Date(2026, 0, 5); // January 5, 2026
      expect(toISODate(date)).toBe('2026-01-05');
    });

    it('handles end of month', () => {
      const date = new Date(2026, 2, 31); // March 31, 2026
      expect(toISODate(date)).toBe('2026-03-31');
    });

    it('handles Dec 31 without shifting to previous year', () => {
      const date = new Date(2026, 11, 31); // December 31, 2026
      expect(toISODate(date)).toBe('2026-12-31');
    });
  });

  describe('addDays', () => {
    it('adds days and returns correct local date', () => {
      expect(addDays('2026-04-11', 1)).toBe('2026-04-12');
    });

    it('handles crossing month boundary', () => {
      expect(addDays('2026-03-31', 1)).toBe('2026-04-01');
    });

    it('adds a full week', () => {
      expect(addDays('2026-04-04', 7)).toBe('2026-04-11');
    });

    it('subtracts days with negative value', () => {
      expect(addDays('2026-04-12', -7)).toBe('2026-04-05');
    });
  });

  describe('getSaturday', () => {
    it('returns the same date when given a Saturday', () => {
      const sat = getSaturday(new Date(2026, 3, 11)); // April 11, 2026 is Saturday
      expect(toISODate(sat)).toBe('2026-04-11');
    });

    it('returns previous Saturday when given a weekday', () => {
      const sat = getSaturday(new Date(2026, 3, 15)); // April 15, 2026 is Wednesday
      expect(toISODate(sat)).toBe('2026-04-11');
    });

    it('returns previous Saturday when given a Friday', () => {
      const sat = getSaturday(new Date(2026, 3, 17)); // April 17, 2026 is Friday
      expect(toISODate(sat)).toBe('2026-04-11');
    });
  });
});

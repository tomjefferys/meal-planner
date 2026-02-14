import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mealsApi, peopleApi, mealPlansApi, ratingsApi } from '../api';

// Mock global fetch
const mockFetch = vi.fn();
global.fetch = mockFetch;

function mockResponse(data, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(data),
    text: () => Promise.resolve(JSON.stringify(data)),
  };
}

function mockNoContentResponse() {
  return {
    ok: true,
    status: 204,
    json: () => Promise.resolve(null),
    text: () => Promise.resolve(''),
  };
}

beforeEach(() => {
  mockFetch.mockReset();
});

// ─── Meals API ──────────────────────────────────────────────

describe('mealsApi', () => {
  describe('getAll', () => {
    it('fetches all meals without search', async () => {
      const meals = [{ id: 1, title: 'Pasta' }];
      mockFetch.mockResolvedValue(mockResponse(meals));

      const result = await mealsApi.getAll();

      expect(mockFetch).toHaveBeenCalledWith('/api/meals', expect.objectContaining({
        headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
      }));
      expect(result).toEqual(meals);
    });

    it('fetches meals with search query', async () => {
      mockFetch.mockResolvedValue(mockResponse([]));

      await mealsApi.getAll('pasta');

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/meals?search=pasta',
        expect.any(Object)
      );
    });

    it('encodes search query', async () => {
      mockFetch.mockResolvedValue(mockResponse([]));

      await mealsApi.getAll('mac & cheese');

      expect(mockFetch).toHaveBeenCalledWith(
        '/api/meals?search=mac%20%26%20cheese',
        expect.any(Object)
      );
    });
  });

  describe('getById', () => {
    it('fetches meal by id', async () => {
      const meal = { id: 1, title: 'Pasta' };
      mockFetch.mockResolvedValue(mockResponse(meal));

      const result = await mealsApi.getById(1);

      expect(mockFetch).toHaveBeenCalledWith('/api/meals/1', expect.any(Object));
      expect(result).toEqual(meal);
    });
  });

  describe('create', () => {
    it('posts new meal', async () => {
      const meal = { title: 'New Meal', effort: 'EASY' };
      mockFetch.mockResolvedValue(mockResponse({ id: 1, ...meal }));

      const result = await mealsApi.create(meal);

      expect(mockFetch).toHaveBeenCalledWith('/api/meals', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(meal),
      }));
      expect(result.id).toBe(1);
    });
  });

  describe('update', () => {
    it('puts updated meal', async () => {
      const meal = { title: 'Updated Meal' };
      mockFetch.mockResolvedValue(mockResponse({ id: 1, ...meal }));

      await mealsApi.update(1, meal);

      expect(mockFetch).toHaveBeenCalledWith('/api/meals/1', expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify(meal),
      }));
    });
  });

  describe('delete', () => {
    it('deletes meal by id', async () => {
      mockFetch.mockResolvedValue(mockNoContentResponse());

      const result = await mealsApi.delete(1);

      expect(mockFetch).toHaveBeenCalledWith('/api/meals/1', expect.objectContaining({
        method: 'DELETE',
      }));
      expect(result).toBeNull();
    });
  });

  describe('exportAll', () => {
    it('fetches all meals for export', async () => {
      const meals = [{ id: 1, title: 'Pasta' }];
      mockFetch.mockResolvedValue(mockResponse(meals));

      const result = await mealsApi.exportAll();

      expect(mockFetch).toHaveBeenCalledWith('/api/meals/export', expect.any(Object));
      expect(result).toEqual(meals);
    });
  });

  describe('importAll', () => {
    it('posts meals for import', async () => {
      const meals = [{ title: 'Imported Meal' }];
      const response = { imported: 1, skipped: 0 };
      mockFetch.mockResolvedValue(mockResponse(response));

      const result = await mealsApi.importAll(meals);

      expect(mockFetch).toHaveBeenCalledWith('/api/meals/import', expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(meals),
      }));
      expect(result).toEqual(response);
    });
  });

  describe('error handling', () => {
    it('throws on non-ok response', async () => {
      mockFetch.mockResolvedValue({
        ok: false,
        status: 500,
        text: () => Promise.resolve('Internal Server Error'),
      });

      await expect(mealsApi.getAll()).rejects.toThrow('API error 500: Internal Server Error');
    });
  });
});

// ─── People API ─────────────────────────────────────────────

describe('peopleApi', () => {
  it('getAll fetches all people', async () => {
    const people = [{ id: 1, name: 'Alice' }];
    mockFetch.mockResolvedValue(mockResponse(people));

    const result = await peopleApi.getAll();

    expect(mockFetch).toHaveBeenCalledWith('/api/people', expect.any(Object));
    expect(result).toEqual(people);
  });

  it('getById fetches person by id', async () => {
    const person = { id: 1, name: 'Alice' };
    mockFetch.mockResolvedValue(mockResponse(person));

    const result = await peopleApi.getById(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/people/1', expect.any(Object));
    expect(result).toEqual(person);
  });

  it('create posts new person', async () => {
    const person = { name: 'Bob' };
    mockFetch.mockResolvedValue(mockResponse({ id: 2, ...person }));

    const result = await peopleApi.create(person);

    expect(mockFetch).toHaveBeenCalledWith('/api/people', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify(person),
    }));
    expect(result.id).toBe(2);
  });

  it('update puts updated person', async () => {
    const person = { name: 'Bob Updated' };
    mockFetch.mockResolvedValue(mockResponse({ id: 1, ...person }));

    await peopleApi.update(1, person);

    expect(mockFetch).toHaveBeenCalledWith('/api/people/1', expect.objectContaining({
      method: 'PUT',
    }));
  });

  it('delete removes person', async () => {
    mockFetch.mockResolvedValue(mockNoContentResponse());

    await peopleApi.delete(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/people/1', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});

// ─── Meal Plans API ─────────────────────────────────────────

describe('mealPlansApi', () => {
  it('getAll fetches all plans', async () => {
    mockFetch.mockResolvedValue(mockResponse([]));

    await mealPlansApi.getAll();

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans', expect.any(Object));
  });

  it('getWeek fetches plan for date', async () => {
    mockFetch.mockResolvedValue(mockResponse({ id: 1 }));

    await mealPlansApi.getWeek('2025-02-08');

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/meal-plans/week?date=2025-02-08',
      expect.any(Object)
    );
  });

  it('getById fetches plan by id', async () => {
    mockFetch.mockResolvedValue(mockResponse({ id: 1 }));

    await mealPlansApi.getById(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans/1', expect.any(Object));
  });

  it('addEntry posts new entry', async () => {
    const entry = { mealId: 1, dayOfWeek: 'MONDAY', mealType: 'DINNER' };
    mockFetch.mockResolvedValue(mockResponse({ id: 1, ...entry }));

    await mealPlansApi.addEntry(1, entry);

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans/1/entries', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify(entry),
    }));
  });

  it('updateEntry puts updated entry', async () => {
    const entry = { dayOfWeek: 'TUESDAY' };
    mockFetch.mockResolvedValue(mockResponse({ id: 1, ...entry }));

    await mealPlansApi.updateEntry(1, entry);

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans/entries/1', expect.objectContaining({
      method: 'PUT',
    }));
  });

  it('deleteEntry deletes entry', async () => {
    mockFetch.mockResolvedValue(mockNoContentResponse());

    await mealPlansApi.deleteEntry(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans/entries/1', expect.objectContaining({
      method: 'DELETE',
    }));
  });

  it('updateNotes sends notes', async () => {
    const notes = { MONDAY: 'Takeaway' };
    mockFetch.mockResolvedValue(mockResponse({ id: 1 }));

    await mealPlansApi.updateNotes(1, notes);

    expect(mockFetch).toHaveBeenCalledWith('/api/meal-plans/1/notes', expect.objectContaining({
      method: 'PUT',
      body: JSON.stringify({ dayNotes: notes }),
    }));
  });

  it('getShoppingList fetches shopping list for plan', async () => {
    mockFetch.mockResolvedValue(mockResponse([{ name: 'Pasta', totalQuantity: 500 }]));

    await mealPlansApi.getShoppingList(1);

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/meal-plans/1/shopping-list',
      expect.any(Object)
    );
  });

  it('getShoppingListByDateRange fetches shopping list by dates', async () => {
    mockFetch.mockResolvedValue(mockResponse([]));

    await mealPlansApi.getShoppingListByDateRange('2025-02-08', '2025-02-14');

    expect(mockFetch).toHaveBeenCalledWith(
      '/api/meal-plans/shopping-list?startDate=2025-02-08&endDate=2025-02-14',
      expect.any(Object)
    );
  });
});

// ─── Ratings API ────────────────────────────────────────────

describe('ratingsApi', () => {
  it('getByMeal fetches ratings for meal', async () => {
    mockFetch.mockResolvedValue(mockResponse([]));

    await ratingsApi.getByMeal(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/ratings/meal/1', expect.any(Object));
  });

  it('getAverage fetches average rating', async () => {
    mockFetch.mockResolvedValue(mockResponse({ averageRating: 4.5 }));

    const result = await ratingsApi.getAverage(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/ratings/meal/1/average', expect.any(Object));
    expect(result.averageRating).toBe(4.5);
  });

  it('create posts new rating', async () => {
    const rating = { mealId: 1, personId: 1, rating: 5 };
    mockFetch.mockResolvedValue(mockResponse({ id: 1, ...rating }));

    await ratingsApi.create(rating);

    expect(mockFetch).toHaveBeenCalledWith('/api/ratings', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify(rating),
    }));
  });

  it('update puts updated rating', async () => {
    const rating = { rating: 4, comment: 'Updated' };
    mockFetch.mockResolvedValue(mockResponse({ id: 1, ...rating }));

    await ratingsApi.update(1, rating);

    expect(mockFetch).toHaveBeenCalledWith('/api/ratings/1', expect.objectContaining({
      method: 'PUT',
    }));
  });

  it('delete removes rating', async () => {
    mockFetch.mockResolvedValue(mockNoContentResponse());

    await ratingsApi.delete(1);

    expect(mockFetch).toHaveBeenCalledWith('/api/ratings/1', expect.objectContaining({
      method: 'DELETE',
    }));
  });
});

const API_BASE = '/api';

async function request(url, options = {}) {
  const res = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(`API error ${res.status}: ${text}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

// Meals
export const mealsApi = {
  getAll: (search) => request(search ? `/meals?search=${encodeURIComponent(search)}` : '/meals'),
  getById: (id) => request(`/meals/${id}`),
  create: (meal) => request('/meals', { method: 'POST', body: JSON.stringify(meal) }),
  update: (id, meal) => request(`/meals/${id}`, { method: 'PUT', body: JSON.stringify(meal) }),
  delete: (id) => request(`/meals/${id}`, { method: 'DELETE' }),
};

// People
export const peopleApi = {
  getAll: () => request('/people'),
  getById: (id) => request(`/people/${id}`),
  create: (person) => request('/people', { method: 'POST', body: JSON.stringify(person) }),
  update: (id, person) => request(`/people/${id}`, { method: 'PUT', body: JSON.stringify(person) }),
  delete: (id) => request(`/people/${id}`, { method: 'DELETE' }),
};

// Meal Plans
export const mealPlansApi = {
  getAll: () => request('/meal-plans'),
  getWeek: (date) => request(`/meal-plans/week?date=${date}`),
  getById: (id) => request(`/meal-plans/${id}`),
  addEntry: (planId, entry) =>
    request(`/meal-plans/${planId}/entries`, { method: 'POST', body: JSON.stringify(entry) }),
  updateEntry: (entryId, entry) =>
    request(`/meal-plans/entries/${entryId}`, { method: 'PUT', body: JSON.stringify(entry) }),
  deleteEntry: (entryId) =>
    request(`/meal-plans/entries/${entryId}`, { method: 'DELETE' }),
  getShoppingList: (planId) => request(`/meal-plans/${planId}/shopping-list`),
};

// Ratings
export const ratingsApi = {
  getByMeal: (mealId) => request(`/ratings/meal/${mealId}`),
  getAverage: (mealId) => request(`/ratings/meal/${mealId}/average`),
  create: (rating) => request('/ratings', { method: 'POST', body: JSON.stringify(rating) }),
  update: (id, rating) => request(`/ratings/${id}`, { method: 'PUT', body: JSON.stringify(rating) }),
  delete: (id) => request(`/ratings/${id}`, { method: 'DELETE' }),
};

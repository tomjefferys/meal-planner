import { useState, useEffect, useCallback } from 'react';
import { mealsApi, ratingsApi, peopleApi } from '../api';
import MealForm from '../components/MealForm';
import RatingStars from '../components/RatingStars';

export default function MealsPage() {
  const [meals, setMeals] = useState([]);
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [ratings, setRatings] = useState({});
  const [mealRatings, setMealRatings] = useState({});
  const [ratingModal, setRatingModal] = useState(null);
  const [ratingForm, setRatingForm] = useState({ personId: '', rating: 0, comment: '' });
  const [search, setSearch] = useState('');

  const loadMeals = useCallback(async () => {
    try {
      const data = await mealsApi.getAll(search || undefined);
      setMeals(data);

      // Load average and individual ratings
      const ratingsMap = {};
      const mealRatingsMap = {};
      for (const meal of data) {
        try {
          const avg = await ratingsApi.getAverage(meal.id);
          ratingsMap[meal.id] = avg.averageRating;
        } catch {
          /* ignore */
        }
        try {
          const individualRatings = await ratingsApi.getByMeal(meal.id);
          mealRatingsMap[meal.id] = individualRatings;
        } catch {
          /* ignore */
        }
      }
      setRatings(ratingsMap);
      setMealRatings(mealRatingsMap);
    } catch (err) {
      console.error('Failed to load meals:', err);
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    loadMeals();
    peopleApi.getAll().then(setPeople).catch(console.error);
  }, [loadMeals]);

  const handleSave = async (data) => {
    try {
      if (editing) {
        await mealsApi.update(editing.id, data);
      } else {
        await mealsApi.create(data);
      }
      setShowForm(false);
      setEditing(null);
      loadMeals();
    } catch (err) {
      console.error('Failed to save meal:', err);
      alert('Failed to save meal. Please try again.');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this meal?')) return;
    try {
      await mealsApi.delete(id);
      loadMeals();
    } catch (err) {
      console.error('Failed to delete meal:', err);
    }
  };

  const handleRate = async () => {
    if (!ratingForm.personId || !ratingForm.rating) return;
    try {
      await ratingsApi.create({
        mealId: ratingModal.id,
        personId: parseInt(ratingForm.personId),
        rating: ratingForm.rating,
        comment: ratingForm.comment,
      });
      setRatingModal(null);
      setRatingForm({ personId: '', rating: 0, comment: '' });
      loadMeals();
    } catch (err) {
      console.error('Failed to save rating:', err);
    }
  };

  const handleExport = async () => {
    try {
      const data = await mealsApi.getAll();
      const exportData = data.map(({ id, ...rest }) => rest);
      const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'meals-export.json';
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export meals.');
    }
  };

  const handleImport = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const importData = JSON.parse(text);
      const result = await mealsApi.importAll(importData);
      alert(`Import complete: ${result.imported} imported, ${result.skipped} skipped (already exist).`);
      loadMeals();
    } catch (err) {
      console.error('Import failed:', err);
      alert('Failed to import meals. Please check the file format.');
    }
    event.target.value = '';
  };

  const effortBadge = (effort) => {
    const classes = { EASY: 'badge-easy', MEDIUM: 'badge-medium', HARD: 'badge-hard' };
    return (
      <span className={`badge ${classes[effort] || ''}`}>
        {effort?.charAt(0) + effort?.slice(1).toLowerCase()}
      </span>
    );
  };

  const totalTime = (meal) => {
    const total = (meal.prepTimeMinutes || 0) + (meal.cookTimeMinutes || 0);
    return total > 0 ? `${total} min` : null;
  };

  if (loading) return <div className="loading">Loading meals...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>🍲 Meals</h1>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Search meals..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{
              padding: '8px 12px',
              border: '1px solid #ddd',
              borderRadius: '8px',
              fontSize: '0.9rem',
            }}
          />
          <button
            className="btn btn-primary"
            onClick={() => {
              setEditing(null);
              setShowForm(true);
            }}
          >
            + Add Meal
          </button>
          <button className="btn btn-secondary" onClick={handleExport}>
            📤 Export
          </button>
          <label className="btn btn-secondary" style={{ cursor: 'pointer' }}>
            📥 Import
            <input
              type="file"
              accept=".json"
              onChange={handleImport}
              style={{ display: 'none' }}
            />
          </label>
        </div>
      </div>

      {meals.length === 0 ? (
        <div className="empty-state">
          <div className="icon">🍽️</div>
          <p>No meals yet. Add your first meal to get started!</p>
          <button className="btn btn-primary" onClick={() => setShowForm(true)}>
            + Add Meal
          </button>
        </div>
      ) : (
        <div className="meals-grid">
          {meals.map((meal) => (
            <div key={meal.id} className="card meal-card">
              <div className="meal-card-header">
                <h3>{meal.title}</h3>
                <div className="meal-card-actions">
                  <button
                    className="btn-icon"
                    title="Rate"
                    onClick={() => setRatingModal(meal)}
                  >
                    ⭐
                  </button>
                  <button
                    className="btn-icon"
                    title="Edit"
                    onClick={() => {
                      setEditing(meal);
                      setShowForm(true);
                    }}
                  >
                    ✏️
                  </button>
                  <button
                    className="btn-icon"
                    title="Delete"
                    onClick={() => handleDelete(meal.id)}
                  >
                    🗑️
                  </button>
                </div>
              </div>

              <div className="meal-card-meta">
                {meal.effort && effortBadge(meal.effort)}
                {totalTime(meal) && (
                  <span className="badge badge-time">⏱ {totalTime(meal)}</span>
                )}
                {ratings[meal.id] != null && (
                  <span className="meal-card-rating">
                    <RatingStars value={Math.round(ratings[meal.id])} readonly />
                    <span>({ratings[meal.id]})</span>
                  </span>
                )}
              </div>

              {meal.description && (
                <p className="meal-card-description">{meal.description}</p>
              )}

              {meal.ingredients?.length > 0 && (
                <p className="meal-card-ingredients">
                  📝 {meal.ingredients.map((i) => i.name).join(', ')}
                </p>
              )}

              {mealRatings[meal.id]?.length > 0 && (
                <div className="meal-card-person-ratings">
                  {mealRatings[meal.id].map((r) => (
                    <div key={r.id} className="person-rating-badge">
                      <span className="person-name">{r.person.name}</span>
                      <RatingStars value={r.rating} readonly />
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Meal Form Modal */}
      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{editing ? 'Edit Meal' : 'Add New Meal'}</h2>
            <MealForm
              meal={editing}
              onSave={handleSave}
              onCancel={() => {
                setShowForm(false);
                setEditing(null);
              }}
            />
          </div>
        </div>
      )}

      {/* Rating Modal */}
      {ratingModal && (
        <div className="modal-overlay" onClick={() => setRatingModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Rate: {ratingModal.title}</h2>
            <div className="form-group">
              <label>Who&apos;s rating?</label>
              <select
                value={ratingForm.personId}
                onChange={(e) => {
                  const pid = e.target.value;
                  if (pid && ratingModal && mealRatings[ratingModal.id]) {
                    const existing = mealRatings[ratingModal.id].find(
                      (r) => r.person.id === parseInt(pid)
                    );
                    if (existing) {
                      setRatingForm({ personId: pid, rating: existing.rating, comment: existing.comment || '' });
                      return;
                    }
                  }
                  setRatingForm((f) => ({ ...f, personId: pid, rating: 0, comment: '' }));
                }}
              >
                <option value="">Select person...</option>
                {people.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Rating</label>
              <RatingStars
                value={ratingForm.rating}
                onChange={(r) => setRatingForm((f) => ({ ...f, rating: r }))}
              />
            </div>
            <div className="form-group">
              <label>Comment (optional)</label>
              <textarea
                value={ratingForm.comment}
                onChange={(e) =>
                  setRatingForm((f) => ({ ...f, comment: e.target.value }))
                }
                placeholder="What did you think?"
              />
            </div>
            <div className="form-actions">
              <button className="btn btn-secondary" onClick={() => setRatingModal(null)}>
                Cancel
              </button>
              <button className="btn btn-primary" onClick={handleRate}>
                Submit Rating
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

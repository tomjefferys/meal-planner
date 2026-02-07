import { useState, useEffect } from 'react';

const EFFORTS = ['EASY', 'MEDIUM', 'HARD'];

export default function MealForm({ meal, onSave, onCancel }) {
  const [form, setForm] = useState({
    title: '',
    description: '',
    ingredients: [{ name: '', quantity: '', unit: '' }],
    prepTimeMinutes: '',
    cookTimeMinutes: '',
    effort: 'MEDIUM',
    imageUrl: '',
  });

  useEffect(() => {
    if (meal) {
      setForm({
        ...meal,
        prepTimeMinutes: meal.prepTimeMinutes ?? '',
        cookTimeMinutes: meal.cookTimeMinutes ?? '',
        ingredients:
          meal.ingredients?.length > 0
            ? meal.ingredients
            : [{ name: '', quantity: '', unit: '' }],
      });
    }
  }, [meal]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleIngredientChange = (index, field, value) => {
    const ingredients = [...form.ingredients];
    ingredients[index] = { ...ingredients[index], [field]: value };
    setForm((f) => ({ ...f, ingredients }));
  };

  const addIngredient = () => {
    setForm((f) => ({
      ...f,
      ingredients: [...f.ingredients, { name: '', quantity: '', unit: '' }],
    }));
  };

  const removeIngredient = (index) => {
    setForm((f) => ({
      ...f,
      ingredients: f.ingredients.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = {
      ...form,
      prepTimeMinutes: form.prepTimeMinutes ? parseInt(form.prepTimeMinutes) : null,
      cookTimeMinutes: form.cookTimeMinutes ? parseInt(form.cookTimeMinutes) : null,
      ingredients: form.ingredients
        .filter((ing) => ing.name.trim() !== '')
        .map((ing) => ({
          name: ing.name.trim(),
          quantity: parseFloat(ing.quantity) || 0,
          unit: ing.unit.trim(),
        })),
    };
    onSave(data);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Title *</label>
        <input
          name="title"
          value={form.title}
          onChange={handleChange}
          required
          placeholder="e.g. Spaghetti Bolognese"
        />
      </div>

      <div className="form-group">
        <label>Description</label>
        <textarea
          name="description"
          value={form.description}
          onChange={handleChange}
          placeholder="Brief description of the meal..."
        />
      </div>

      <div className="form-row">
        <div className="form-group">
          <label>Prep Time (mins)</label>
          <input
            name="prepTimeMinutes"
            type="number"
            min="0"
            value={form.prepTimeMinutes}
            onChange={handleChange}
          />
        </div>
        <div className="form-group">
          <label>Cook Time (mins)</label>
          <input
            name="cookTimeMinutes"
            type="number"
            min="0"
            value={form.cookTimeMinutes}
            onChange={handleChange}
          />
        </div>
        <div className="form-group">
          <label>Effort</label>
          <select name="effort" value={form.effort} onChange={handleChange}>
            {EFFORTS.map((e) => (
              <option key={e} value={e}>
                {e.charAt(0) + e.slice(1).toLowerCase()}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="form-group">
        <label>Ingredients</label>
        <div className="ingredients-list">
          {form.ingredients.map((ing, i) => (
            <div key={i} className="ingredient-row">
              <input
                placeholder="Ingredient name"
                value={ing.name}
                onChange={(e) => handleIngredientChange(i, 'name', e.target.value)}
              />
              <input
                placeholder="Qty"
                type="number"
                step="any"
                min="0"
                value={ing.quantity}
                onChange={(e) =>
                  handleIngredientChange(i, 'quantity', e.target.value)
                }
              />
              <input
                placeholder="Unit"
                value={ing.unit}
                onChange={(e) => handleIngredientChange(i, 'unit', e.target.value)}
              />
              {form.ingredients.length > 1 && (
                <button
                  type="button"
                  className="btn-icon"
                  onClick={() => removeIngredient(i)}
                  title="Remove"
                >
                  ✕
                </button>
              )}
            </div>
          ))}
          <button type="button" className="btn btn-secondary btn-sm" onClick={addIngredient}>
            + Add Ingredient
          </button>
        </div>
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary">
          {meal ? 'Update Meal' : 'Add Meal'}
        </button>
      </div>
    </form>
  );
}

import { useState, useEffect } from 'react';

export default function PersonForm({ person, onSave, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    eatingPreferences: '',
    cookingPreferences: '',
  });

  useEffect(() => {
    if (person) {
      setForm({
        name: person.name || '',
        eatingPreferences: person.eatingPreferences || '',
        cookingPreferences: person.cookingPreferences || '',
      });
    }
  }, [person]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(form);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Name *</label>
        <input
          name="name"
          value={form.name}
          onChange={handleChange}
          required
          placeholder="e.g. Mum, Dad, Sarah..."
        />
      </div>

      <div className="form-group">
        <label>Eating Preferences</label>
        <textarea
          name="eatingPreferences"
          value={form.eatingPreferences}
          onChange={handleChange}
          placeholder="Dietary requirements, likes, dislikes..."
        />
      </div>

      <div className="form-group">
        <label>Cooking Preferences</label>
        <textarea
          name="cookingPreferences"
          value={form.cookingPreferences}
          onChange={handleChange}
          placeholder="What they enjoy cooking, skill level..."
        />
      </div>

      <div className="form-actions">
        <button type="button" className="btn btn-secondary" onClick={onCancel}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary">
          {person ? 'Update' : 'Add Family Member'}
        </button>
      </div>
    </form>
  );
}

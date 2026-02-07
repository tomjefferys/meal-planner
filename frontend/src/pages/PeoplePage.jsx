import { useState, useEffect } from 'react';
import { peopleApi } from '../api';
import PersonForm from '../components/PersonForm';

export default function PeoplePage() {
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);

  const loadPeople = async () => {
    try {
      const data = await peopleApi.getAll();
      setPeople(data);
    } catch (err) {
      console.error('Failed to load people:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPeople();
  }, []);

  const handleSave = async (data) => {
    try {
      if (editing) {
        await peopleApi.update(editing.id, data);
      } else {
        await peopleApi.create(data);
      }
      setShowForm(false);
      setEditing(null);
      loadPeople();
    } catch (err) {
      console.error('Failed to save person:', err);
      alert('Failed to save. Please try again.');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Remove this family member?')) return;
    try {
      await peopleApi.delete(id);
      loadPeople();
    } catch (err) {
      console.error('Failed to delete person:', err);
    }
  };

  if (loading) return <div className="loading">Loading family members...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>👨‍👩‍👧‍👦 Family Members</h1>
        <button
          className="btn btn-primary"
          onClick={() => {
            setEditing(null);
            setShowForm(true);
          }}
        >
          + Add Member
        </button>
      </div>

      {people.length === 0 ? (
        <div className="empty-state">
          <div className="icon">👨‍👩‍👧‍👦</div>
          <p>No family members added yet.</p>
          <button className="btn btn-primary" onClick={() => setShowForm(true)}>
            + Add Family Member
          </button>
        </div>
      ) : (
        <div className="people-grid">
          {people.map((person) => (
            <div key={person.id} className="card person-card">
              <div className="person-card-header">
                <h3>{person.name}</h3>
                <div className="meal-card-actions">
                  <button
                    className="btn-icon"
                    title="Edit"
                    onClick={() => {
                      setEditing(person);
                      setShowForm(true);
                    }}
                  >
                    ✏️
                  </button>
                  <button
                    className="btn-icon"
                    title="Delete"
                    onClick={() => handleDelete(person.id)}
                  >
                    🗑️
                  </button>
                </div>
              </div>
              {person.eatingPreferences && (
                <p>
                  <strong>🍴 Eating:</strong> {person.eatingPreferences}
                </p>
              )}
              {person.cookingPreferences && (
                <p>
                  <strong>👨‍🍳 Cooking:</strong> {person.cookingPreferences}
                </p>
              )}
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <div className="modal-overlay" onClick={() => setShowForm(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{editing ? 'Edit Family Member' : 'Add Family Member'}</h2>
            <PersonForm
              person={editing}
              onSave={handleSave}
              onCancel={() => {
                setShowForm(false);
                setEditing(null);
              }}
            />
          </div>
        </div>
      )}
    </div>
  );
}

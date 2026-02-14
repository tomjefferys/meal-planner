import { useState, useEffect, useCallback } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { mealPlansApi, mealsApi, peopleApi } from '../api';

const DAYS = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_LABELS = ['Sat', 'Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri'];

function getSaturday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = (day - 6 + 7) % 7;
  d.setDate(d.getDate() - diff);
  return d;
}

function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
}

function addDays(dateStr, days) {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

function toISODate(date) {
  return date.toISOString().split('T')[0];
}

export default function PlannerPage() {
  const [weekStart, setWeekStart] = useState(() => toISODate(getSaturday(new Date())));
  const [plan, setPlan] = useState(null);
  const [meals, setMeals] = useState([]);
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mealSelectDay, setMealSelectDay] = useState(null);
  const [editingNoteDay, setEditingNoteDay] = useState(null);
  const [noteText, setNoteText] = useState('');

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [planData, mealsData, peopleData] = await Promise.all([
        mealPlansApi.getWeek(weekStart),
        mealsApi.getAll(),
        peopleApi.getAll(),
      ]);
      setPlan(planData);
      setMeals(mealsData);
      setPeople(peopleData);
    } catch (err) {
      console.error('Failed to load planner data:', err);
    } finally {
      setLoading(false);
    }
  }, [weekStart]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const getEntriesForDay = (day) => {
    return (plan?.entries || [])
      .filter((e) => e.dayOfWeek === day)
      .sort((a, b) => a.displayOrder - b.displayOrder);
  };

  const handleAddMeal = async (mealId, day) => {
    try {
      await mealPlansApi.addEntry(plan.id, {
        mealId,
        dayOfWeek: day,
      });
      loadData();
    } catch (err) {
      console.error('Failed to add meal:', err);
    }
    setMealSelectDay(null);
  };

  const handleRemoveEntry = async (entryId) => {
    try {
      await mealPlansApi.deleteEntry(entryId);
      loadData();
    } catch (err) {
      console.error('Failed to remove entry:', err);
    }
  };

  const handleAssignCook = async (entryId, cookId) => {
    try {
      await mealPlansApi.updateEntry(entryId, {
        assignedCookId: cookId || null,
      });
      loadData();
    } catch (err) {
      console.error('Failed to assign cook:', err);
    }
  };

  const handleDragEnd = async (result) => {
    const { source, destination, draggableId } = result;
    if (!destination) return;
    if (source.droppableId === destination.droppableId && source.index === destination.index) return;

    if (source.droppableId === 'available-meals') {
      const mealId = parseInt(draggableId.replace('avail-', ''));
      const destDay = destination.droppableId;
      await handleAddMeal(mealId, destDay);
    } else if (destination.droppableId === 'available-meals') {
      const entryId = parseInt(draggableId.replace('entry-', ''));
      await handleRemoveEntry(entryId);
    } else {
      const entryId = parseInt(draggableId.replace('entry-', ''));
      const destDay = destination.droppableId;

      await mealPlansApi.updateEntry(entryId, {
        dayOfWeek: destDay,
        displayOrder: destination.index,
      });
      loadData();
    }
  };

  const prevWeek = () => setWeekStart(addDays(weekStart, -7));
  const nextWeek = () => setWeekStart(addDays(weekStart, 7));
  const goToday = () => setWeekStart(toISODate(getSaturday(new Date())));

  const handleEditNote = (day) => {
    setEditingNoteDay(day);
    setNoteText(plan?.dayNotes?.[day] || '');
  };

  const handleSaveNote = async (day) => {
    try {
      const updatedNotes = { ...plan.dayNotes, [day]: noteText.trim() };
      if (!noteText.trim()) delete updatedNotes[day];
      await mealPlansApi.updateNotes(plan.id, updatedNotes);
      loadData();
      setEditingNoteDay(null);
      setNoteText('');
    } catch (err) {
      console.error('Failed to update note:', err);
    }
  };

  const handleCancelNote = () => {
    setEditingNoteDay(null);
    setNoteText('');
  };

  if (loading) return <div className="loading">Loading planner...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>📅 Weekly Meal Plan</h1>
      </div>

      <div className="planner-controls">
        <button className="btn btn-secondary btn-sm" onClick={prevWeek}>← Prev</button>
        <button className="btn btn-secondary btn-sm" onClick={goToday}>Today</button>
        <button className="btn btn-secondary btn-sm" onClick={nextWeek}>Next →</button>
        <h2>Week of {formatDate(weekStart)} – {formatDate(addDays(weekStart, 6))}</h2>
      </div>

      <DragDropContext onDragEnd={handleDragEnd}>
        <div className="planner-day-grid">
          {DAYS.map((day, i) => {
            const entries = getEntriesForDay(day);

            return (
              <div key={day} className="planner-day-column">
                <div className="planner-day-header">
                  <div>{DAY_LABELS[i]}</div>
                  <div className="date">{formatDate(addDays(weekStart, i))}</div>
                </div>

                <Droppable droppableId={day}>
                  {(provided, snapshot) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className={`planner-day-content ${snapshot.isDraggingOver ? 'drag-over' : ''}`}
                    >
                      {entries.map((entry, index) => (
                        <Draggable
                          key={`entry-${entry.id}`}
                          draggableId={`entry-${entry.id}`}
                          index={index}
                        >
                          {(dragProvided) => (
                            <div
                              ref={dragProvided.innerRef}
                              {...dragProvided.draggableProps}
                              {...dragProvided.dragHandleProps}
                              className="planner-meal-chip"
                            >
                              {entry.meal.title}
                              <button
                                className="remove-btn"
                                onClick={(e) => { e.stopPropagation(); handleRemoveEntry(entry.id); }}
                                onMouseDown={(e) => e.stopPropagation()}
                                onTouchStart={(e) => e.stopPropagation()}
                              >
                                ✕
                              </button>
                              {people.length > 0 && (
                                <div className="planner-meal-cook">
                                  <select
                                    value={entry.assignedCook?.id || ''}
                                    onChange={(e) =>
                                      handleAssignCook(entry.id, e.target.value ? parseInt(e.target.value) : null)
                                    }
                                    onClick={(e) => e.stopPropagation()}
                                    style={{
                                      fontSize: '0.7rem', border: 'none', background: 'transparent',
                                      cursor: 'pointer', padding: '2px', maxWidth: '100%',
                                    }}
                                  >
                                    <option value="">👨‍🍳 Cook?</option>
                                    {people.map((p) => (
                                      <option key={p.id} value={p.id}>{p.name}</option>
                                    ))}
                                  </select>
                                </div>
                              )}
                            </div>
                          )}
                        </Draggable>
                      ))}
                      {provided.placeholder}
                      <button
                        className="add-btn"
                        onClick={() => setMealSelectDay(day)}
                        title="Add meal"
                      >
                        +
                      </button>
                    </div>
                  )}
                </Droppable>

                <div className="day-notes-section">
                  {editingNoteDay === day ? (
                    <div className="notes-editor">
                      <textarea
                        value={noteText}
                        onChange={(e) => setNoteText(e.target.value)}
                        placeholder="Add notes for this day..."
                        rows={2}
                        autoFocus
                        style={{
                          width: '100%', padding: '4px', fontSize: '0.75rem',
                          border: '1px solid #ddd', borderRadius: '4px', resize: 'vertical',
                        }}
                      />
                      <div style={{ display: 'flex', gap: '4px', marginTop: '4px' }}>
                        <button onClick={() => handleSaveNote(day)} className="btn btn-primary" style={{ fontSize: '0.7rem', padding: '2px 8px' }}>Save</button>
                        <button onClick={handleCancelNote} className="btn btn-secondary" style={{ fontSize: '0.7rem', padding: '2px 8px' }}>Cancel</button>
                      </div>
                    </div>
                  ) : (
                    <div className="notes-display" onClick={() => handleEditNote(day)}>
                      {plan?.dayNotes?.[day] ? (
                        <div className="notes-text">📝 {plan.dayNotes[day]}</div>
                      ) : (
                        <div className="notes-placeholder">+ Add notes</div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>

        <div className="available-meals-panel">
          <h3>Available Meals — drag onto the planner above</h3>
          <Droppable droppableId="available-meals" direction="horizontal">
            {(provided) => (
              <div ref={provided.innerRef} {...provided.droppableProps} className="available-meals-list">
                {meals.length === 0 ? (
                  <span style={{ color: '#999', fontSize: '0.9rem' }}>
                    No meals yet. Add meals from the Meals page first.
                  </span>
                ) : (
                  meals.map((meal, index) => (
                    <Draggable key={`avail-${meal.id}`} draggableId={`avail-${meal.id}`} index={index}>
                      {(dragProvided) => (
                        <div
                          ref={dragProvided.innerRef}
                          {...dragProvided.draggableProps}
                          {...dragProvided.dragHandleProps}
                          className="available-meal-chip"
                        >
                          {meal.title}
                        </div>
                      )}
                    </Draggable>
                  ))
                )}
                {provided.placeholder}
              </div>
            )}
          </Droppable>
        </div>
      </DragDropContext>

      {mealSelectDay && (
        <div className="modal-overlay" onClick={() => setMealSelectDay(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Add Meal to {mealSelectDay.charAt(0) + mealSelectDay.slice(1).toLowerCase()}</h2>
            {meals.length === 0 ? (
              <p style={{ color: '#999' }}>No meals available. Add meals from the Meals page first.</p>
            ) : (
              <div className="meal-select-grid">
                {meals.map((meal) => (
                  <div
                    key={meal.id}
                    className="meal-select-item"
                    onClick={() => handleAddMeal(meal.id, mealSelectDay)}
                  >
                    <h4>{meal.title}</h4>
                    <div className="meta">
                      {meal.effort && <span>{meal.effort.charAt(0) + meal.effort.slice(1).toLowerCase()}</span>}
                      {(meal.prepTimeMinutes || meal.cookTimeMinutes) && (
                        <span> · {(meal.prepTimeMinutes || 0) + (meal.cookTimeMinutes || 0)} min</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
            <div className="form-actions">
              <button className="btn btn-secondary" onClick={() => setMealSelectDay(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

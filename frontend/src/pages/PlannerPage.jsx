import { useState, useEffect, useCallback } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { mealPlansApi, mealsApi, peopleApi } from '../api';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER'];

function getMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
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
  const [weekStart, setWeekStart] = useState(() => toISODate(getMonday(new Date())));
  const [plan, setPlan] = useState(null);
  const [meals, setMeals] = useState([]);
  const [people, setPeople] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mealSelectSlot, setMealSelectSlot] = useState(null);

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

  const getEntry = (day, mealType) => {
    return plan?.entries?.find(
      (e) => e.dayOfWeek === day && e.mealType === mealType
    );
  };

  const handleAddMeal = async (mealId, day, mealType) => {
    try {
      const existing = getEntry(day, mealType);
      if (existing) {
        await mealPlansApi.deleteEntry(existing.id);
      }
      await mealPlansApi.addEntry(plan.id, {
        mealId,
        dayOfWeek: day,
        mealType,
      });
      loadData();
    } catch (err) {
      console.error('Failed to add meal:', err);
    }
    setMealSelectSlot(null);
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
    if (source.droppableId === destination.droppableId) return;

    if (source.droppableId === 'available-meals') {
      // Dragging from available meals to a slot
      const mealId = parseInt(draggableId.replace('avail-', ''));
      const [day, mealType] = destination.droppableId.split('::');
      await handleAddMeal(mealId, day, mealType);
    } else if (destination.droppableId === 'available-meals') {
      // Dragging from slot back to available = remove
      const entryId = parseInt(draggableId.replace('entry-', ''));
      await handleRemoveEntry(entryId);
    } else {
      // Moving between slots
      const entryId = parseInt(draggableId.replace('entry-', ''));
      const [destDay, destType] = destination.droppableId.split('::');

      // Check if destination already has an entry
      const destEntry = getEntry(destDay, destType);
      if (destEntry) {
        // Swap: move dest entry to source slot
        const [srcDay, srcType] = source.droppableId.split('::');
        await mealPlansApi.updateEntry(destEntry.id, {
          dayOfWeek: srcDay,
          mealType: srcType,
        });
      }

      await mealPlansApi.updateEntry(entryId, {
        dayOfWeek: destDay,
        mealType: destType,
      });
      loadData();
    }
  };

  const prevWeek = () => {
    setWeekStart(addDays(weekStart, -7));
  };

  const nextWeek = () => {
    setWeekStart(addDays(weekStart, 7));
  };

  const goToday = () => {
    setWeekStart(toISODate(getMonday(new Date())));
  };

  if (loading) return <div className="loading">Loading planner...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>📅 Weekly Meal Plan</h1>
      </div>

      <div className="planner-controls">
        <button className="btn btn-secondary btn-sm" onClick={prevWeek}>
          ← Prev
        </button>
        <button className="btn btn-secondary btn-sm" onClick={goToday}>
          Today
        </button>
        <button className="btn btn-secondary btn-sm" onClick={nextWeek}>
          Next →
        </button>
        <h2>
          Week of {formatDate(weekStart)} – {formatDate(addDays(weekStart, 6))}
        </h2>
      </div>

      <DragDropContext onDragEnd={handleDragEnd}>
        {/* Planner Grid */}
        <div className="planner-grid">
          {/* Header row */}
          <div className="planner-corner" />
          {DAYS.map((day, i) => (
            <div key={day} className="planner-day-header">
              <div>{DAY_LABELS[i]}</div>
              <div className="date">{formatDate(addDays(weekStart, i))}</div>
            </div>
          ))}

          {/* Meal type rows */}
          {MEAL_TYPES.map((type) => (
            <>
              <div key={`label-${type}`} className="planner-label">
                {type.charAt(0) + type.slice(1).toLowerCase()}
              </div>
              {DAYS.map((day) => {
                const entry = getEntry(day, type);
                const slotId = `${day}::${type}`;

                return (
                  <Droppable key={slotId} droppableId={slotId}>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={`planner-slot ${
                          snapshot.isDraggingOver ? 'drag-over' : ''
                        } ${entry ? 'has-meal' : ''}`}
                      >
                        {entry ? (
                          <Draggable
                            draggableId={`entry-${entry.id}`}
                            index={0}
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
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleRemoveEntry(entry.id);
                                  }}
                                >
                                  ✕
                                </button>
                                {people.length > 0 && (
                                  <div className="planner-meal-cook">
                                    <select
                                      value={entry.assignedCook?.id || ''}
                                      onChange={(e) =>
                                        handleAssignCook(
                                          entry.id,
                                          e.target.value
                                            ? parseInt(e.target.value)
                                            : null
                                        )
                                      }
                                      onClick={(e) => e.stopPropagation()}
                                      style={{
                                        fontSize: '0.7rem',
                                        border: 'none',
                                        background: 'transparent',
                                        cursor: 'pointer',
                                        padding: '2px',
                                        maxWidth: '100%',
                                      }}
                                    >
                                      <option value="">👨‍🍳 Cook?</option>
                                      {people.map((p) => (
                                        <option key={p.id} value={p.id}>
                                          {p.name}
                                        </option>
                                      ))}
                                    </select>
                                  </div>
                                )}
                              </div>
                            )}
                          </Draggable>
                        ) : (
                          <button
                            className="add-btn"
                            onClick={() => setMealSelectSlot({ day, type })}
                            title="Add meal"
                          >
                            +
                          </button>
                        )}
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                );
              })}
            </>
          ))}
        </div>

        {/* Available Meals Panel */}
        <div className="available-meals-panel">
          <h3>Available Meals — drag onto the planner above</h3>
          <Droppable droppableId="available-meals" direction="horizontal">
            {(provided) => (
              <div
                ref={provided.innerRef}
                {...provided.droppableProps}
                className="available-meals-list"
              >
                {meals.length === 0 ? (
                  <span style={{ color: '#999', fontSize: '0.9rem' }}>
                    No meals yet. Add meals from the Meals page first.
                  </span>
                ) : (
                  meals.map((meal, index) => (
                    <Draggable
                      key={`avail-${meal.id}`}
                      draggableId={`avail-${meal.id}`}
                      index={index}
                    >
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

      {/* Quick Add Meal Modal (click + button) */}
      {mealSelectSlot && (
        <div className="modal-overlay" onClick={() => setMealSelectSlot(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>
              Select Meal for {mealSelectSlot.day.charAt(0) + mealSelectSlot.day.slice(1).toLowerCase()}{' '}
              {mealSelectSlot.type.charAt(0) + mealSelectSlot.type.slice(1).toLowerCase()}
            </h2>
            {meals.length === 0 ? (
              <p style={{ color: '#999' }}>
                No meals available. Add meals from the Meals page first.
              </p>
            ) : (
              <div className="meal-select-grid">
                {meals.map((meal) => (
                  <div
                    key={meal.id}
                    className="meal-select-item"
                    onClick={() =>
                      handleAddMeal(
                        meal.id,
                        mealSelectSlot.day,
                        mealSelectSlot.type
                      )
                    }
                  >
                    <h4>{meal.title}</h4>
                    <div className="meta">
                      {meal.effort && (
                        <span>
                          {meal.effort.charAt(0) + meal.effort.slice(1).toLowerCase()}
                        </span>
                      )}
                      {(meal.prepTimeMinutes || meal.cookTimeMinutes) && (
                        <span>
                          {' · '}
                          {(meal.prepTimeMinutes || 0) + (meal.cookTimeMinutes || 0)} min
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
            <div className="form-actions">
              <button
                className="btn btn-secondary"
                onClick={() => setMealSelectSlot(null)}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

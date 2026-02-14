import { useState, useEffect, useCallback } from 'react';
import { mealPlansApi } from '../api';

const DAYS = ['SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_NAMES = ['Saturday', 'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

function getSaturday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = (day - 6 + 7) % 7;
  d.setDate(d.getDate() - diff);
  return d;
}

function toISODate(date) {
  return date.toISOString().split('T')[0];
}

function addDays(dateStr, days) {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

function formatDateLong(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
}

function formatDateShort(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
}

export default function WeeklySummaryPage() {
  const [startDate, setStartDate] = useState(() => {
    // Default to upcoming Saturday
    const nextSat = getSaturday(new Date());
    const today = new Date();
    // If today is past Saturday, go to next week
    if (nextSat < today) {
      nextSat.setDate(nextSat.getDate() + 7);
    }
    return toISODate(nextSat);
  });
  const [endDate, setEndDate] = useState(() => addDays(toISODate(getSaturday(new Date())), 13));
  const [plans, setPlans] = useState([]);
  const [loading, setLoading] = useState(true);

  // Initialise end date when start date changes
  useEffect(() => {
    setEndDate(addDays(startDate, 6));
  }, [startDate]);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      // Load plans that cover our date range
      const allPlans = await mealPlansApi.getAll();
      setPlans(allPlans);
    } catch (err) {
      console.error('Failed to load plans:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Build the day-by-day view for the date range
  const getDaysInRange = () => {
    const days = [];
    let current = startDate;
    while (current <= endDate) {
      days.push(current);
      current = addDays(current, 1);
    }
    return days;
  };

  const getEntriesForDate = (dateStr) => {
    const d = new Date(dateStr + 'T00:00:00');

    for (const plan of plans) {
      const planStart = new Date(plan.weekStartDate + 'T00:00:00');
      const planEnd = new Date(plan.weekStartDate + 'T00:00:00');
      planEnd.setDate(planEnd.getDate() + 6);

      if (d >= planStart && d <= planEnd) {
        const diffDays = Math.round((d - planStart) / (1000 * 60 * 60 * 24));
        const dayOfWeek = DAYS[diffDays];

        if (!dayOfWeek) continue;

        const entries = (plan.entries || [])
          .filter((e) => e.dayOfWeek === dayOfWeek)
          .sort((a, b) => a.displayOrder - b.displayOrder);

        const notes = plan.dayNotes?.[dayOfWeek] || null;

        return { entries, notes, plan };
      }
    }

    return { entries: [], notes: null, plan: null };
  };

  const handlePrint = () => {
    window.print();
  };

  const daysInRange = getDaysInRange();

  if (loading) return <div className="loading">Loading summary...</div>;

  return (
    <div className="weekly-summary">
      <div className="page-header no-print">
        <h1>📋 Weekly Summary</h1>
        <button className="btn btn-primary" onClick={handlePrint}>
          🖨️ Print
        </button>
      </div>

      <div className="summary-controls no-print">
        <div className="date-range-inputs">
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>From</label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ marginBottom: 0 }}>
            <label>To</label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </div>
        </div>
        <div className="date-presets">
          <button className="btn btn-secondary btn-sm" onClick={() => {
            const sat = getSaturday(new Date());
            const today = new Date();
            if (sat < today) sat.setDate(sat.getDate() + 7);
            setStartDate(toISODate(sat));
          }}>Upcoming Week</button>
          <button className="btn btn-secondary btn-sm" onClick={() => {
            setStartDate(toISODate(getSaturday(new Date())));
          }}>This Week</button>
        </div>
      </div>

      <div className="summary-print-header">
        <h1>🍽️ Meal Plan</h1>
        <p>{formatDateShort(startDate)} – {formatDateShort(endDate)}</p>
      </div>

      <div className="summary-days">
        {daysInRange.map((dateStr) => {
          const { entries, notes } = getEntriesForDate(dateStr);
          const d = new Date(dateStr + 'T00:00:00');
          const dayName = d.toLocaleDateString('en-GB', { weekday: 'long' });
          const dateFormatted = d.toLocaleDateString('en-GB', { day: 'numeric', month: 'long' });

          return (
            <div key={dateStr} className="summary-day-card">
              <div className="summary-day-header">
                <span className="summary-day-name">{dayName}</span>
                <span className="summary-day-date">{dateFormatted}</span>
              </div>

              <div className="summary-day-body">
                {entries.length === 0 && !notes ? (
                  <p className="summary-no-plans">No meals planned</p>
                ) : (
                  <>
                    {entries.length > 0 && (
                      <div className="summary-meals">
                        {entries.map((entry) => (
                          <div key={entry.id} className="summary-meal-item">
                            <span className="summary-meal-title">{entry.meal.title}</span>
                            {entry.assignedCook && (
                              <span className="summary-meal-cook">
                                👨‍🍳 {entry.assignedCook.name}
                              </span>
                            )}
                            {entry.meal.effort && (
                              <span className={`badge badge-${entry.meal.effort.toLowerCase()}`}>
                                {entry.meal.effort.charAt(0) + entry.meal.effort.slice(1).toLowerCase()}
                              </span>
                            )}
                            {(entry.meal.prepTimeMinutes || entry.meal.cookTimeMinutes) && (
                              <span className="badge badge-time">
                                ⏱ {(entry.meal.prepTimeMinutes || 0) + (entry.meal.cookTimeMinutes || 0)} min
                              </span>
                            )}
                          </div>
                        ))}
                      </div>
                    )}

                    {notes && (
                      <div className="summary-notes">
                        📝 {notes}
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

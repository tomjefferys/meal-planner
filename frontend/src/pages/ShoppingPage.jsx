import { useState, useEffect, useCallback } from 'react';
import { mealPlansApi } from '../api';

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

function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
}

function addDays(dateStr, days) {
  const d = new Date(dateStr + 'T00:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().split('T')[0];
}

export default function ShoppingPage() {
  const [startDate, setStartDate] = useState(() => toISODate(getSaturday(new Date())));
  const [endDate, setEndDate] = useState(() => addDays(toISODate(getSaturday(new Date())), 6));
  const [items, setItems] = useState([]);
  const [checked, setChecked] = useState({});
  const [loading, setLoading] = useState(true);
  const [showExport, setShowExport] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const shoppingList = await mealPlansApi.getShoppingListByDateRange(startDate, endDate);
      setItems(shoppingList);
      setChecked({});
    } catch (err) {
      console.error('Failed to load shopping list:', err);
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        loadData();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [loadData]);

  const toggleChecked = (index) => {
    setChecked((c) => ({ ...c, [index]: !c[index] }));
  };

  const formatAmount = (item) => {
    if (!item.totalQuantity && !item.unit) return '';
    const qty = item.totalQuantity % 1 === 0 ? item.totalQuantity : item.totalQuantity.toFixed(1);
    return `${qty} ${item.unit}`.trim();
  };

  const exportText = items
    .map(
      (item, i) =>
        `${checked[i] ? '✓' : '☐'} ${item.name}${formatAmount(item) ? ` — ${formatAmount(item)}` : ''}`
    )
    .join('\n');

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(exportText);
      alert('Shopping list copied to clipboard!');
    } catch {
      // Fallback: select the textarea
      const el = document.querySelector('.shopping-export textarea');
      if (el) {
        el.select();
        document.execCommand('copy');
      }
    }
  };

  const handlePrint = () => {
    window.print();
  };

  if (loading) return <div className="loading">Loading shopping list...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>🛒 Shopping List</h1>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="btn btn-secondary btn-sm" onClick={loadData}>
            🔄 Refresh
          </button>
          <button className="btn btn-secondary btn-sm" onClick={() => setShowExport(!showExport)}>
            📋 Export
          </button>
          <button className="btn btn-secondary btn-sm" onClick={handlePrint}>
            🖨️ Print
          </button>
        </div>
      </div>

      <div className="shopping-date-controls">
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
            const sat = toISODate(getSaturday(new Date()));
            setStartDate(sat);
            setEndDate(addDays(sat, 6));
          }}>This Week</button>
          <button className="btn btn-secondary btn-sm" onClick={() => {
            const sat = toISODate(getSaturday(new Date()));
            setStartDate(sat);
            setEndDate(addDays(sat, 1));
          }}>Weekend</button>
          <button className="btn btn-secondary btn-sm" onClick={() => {
            const sat = toISODate(getSaturday(new Date()));
            setStartDate(addDays(sat, 2));
            setEndDate(addDays(sat, 6));
          }}>Weekdays</button>
        </div>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          {formatDate(startDate)} – {formatDate(endDate)}
          {items.length > 0 && <span> · {items.length} items</span>}
        </p>
      </div>

      {items.length === 0 ? (
        <div className="empty-state">
          <div className="icon">🛒</div>
          <p>
            No items on the shopping list yet.
            <br />
            Add meals with ingredients to the weekly plan first.
          </p>
        </div>
      ) : (
        <div className="shopping-list">
          {items.map((item, index) => (
            <div
              key={index}
              className={`shopping-item ${checked[index] ? 'checked' : ''}`}
            >
              <input
                type="checkbox"
                checked={!!checked[index]}
                onChange={() => toggleChecked(index)}
              />
              <span className="shopping-item-name">{item.name}</span>
              <span className="shopping-item-amount">{formatAmount(item)}</span>
            </div>
          ))}

          <p
            style={{
              textAlign: 'center',
              color: '#999',
              marginTop: '16px',
              fontSize: '0.85rem',
            }}
          >
            {Object.values(checked).filter(Boolean).length} of {items.length} items
            checked
          </p>
        </div>
      )}

      {showExport && items.length > 0 && (
        <div className="shopping-export">
          <h3 style={{ marginBottom: '8px' }}>Export Shopping List</h3>
          <textarea readOnly value={exportText} />
          <div style={{ display: 'flex', gap: '8px' }}>
            <button className="btn btn-primary btn-sm" onClick={handleCopy}>
              Copy to Clipboard
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

import { useState, useEffect, useCallback } from 'react';
import { mealPlansApi } from '../api';

function getMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
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
  const [weekStart] = useState(() => toISODate(getMonday(new Date())));
  const [plan, setPlan] = useState(null);
  const [items, setItems] = useState([]);
  const [checked, setChecked] = useState({});
  const [loading, setLoading] = useState(true);
  const [showExport, setShowExport] = useState(false);

  const loadData = useCallback(async () => {
    try {
      const planData = await mealPlansApi.getWeek(weekStart);
      setPlan(planData);

      if (planData?.id) {
        const shoppingList = await mealPlansApi.getShoppingList(planData.id);
        setItems(shoppingList);
      }
    } catch (err) {
      console.error('Failed to load shopping list:', err);
    } finally {
      setLoading(false);
    }
  }, [weekStart]);

  useEffect(() => {
    loadData();
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
          <button className="btn btn-secondary btn-sm" onClick={() => setShowExport(!showExport)}>
            📋 Export
          </button>
          <button className="btn btn-secondary btn-sm" onClick={handlePrint}>
            🖨️ Print
          </button>
        </div>
      </div>

      <p style={{ color: '#666', marginBottom: '16px' }}>
        Week of {formatDate(weekStart)} – {formatDate(addDays(weekStart, 6))}
        {plan?.entries?.length > 0 && (
          <span> · {plan.entries.length} meals planned</span>
        )}
      </p>

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

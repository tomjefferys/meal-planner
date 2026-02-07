export default function RatingStars({ value = 0, onChange, readonly = false }) {
  const handleClick = (star) => {
    if (!readonly && onChange) {
      onChange(star);
    }
  };

  return (
    <div className={`rating-stars ${readonly ? 'readonly' : ''}`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          className={`star ${star <= value ? 'active' : ''}`}
          onClick={() => handleClick(star)}
          disabled={readonly}
        >
          ★
        </button>
      ))}
    </div>
  );
}

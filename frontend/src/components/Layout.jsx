import { NavLink, Outlet } from 'react-router-dom';

export default function Layout() {
  const links = [
    { to: '/planner', icon: '📅', label: 'Weekly Plan' },
    { to: '/meals', icon: '🍲', label: 'Meals' },
    { to: '/people', icon: '👨‍👩‍👧‍👦', label: 'Family' },
    { to: '/shopping', icon: '🛒', label: 'Shopping' },
  ];

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-title">🍽️ Meal Planner</div>
        <nav>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to}>
              <span className="icon">{link.icon}</span>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="bottom-nav">
        <nav>
          {links.map((link) => (
            <NavLink key={link.to} to={link.to}>
              <span className="icon">{link.icon}</span>
              {link.label}
            </NavLink>
          ))}
        </nav>
      </div>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

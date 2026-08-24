import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import ProductList from './pages/ProductList';
import PlaceOrder from './pages/PlaceOrder';
import OrderStatus from './pages/OrderStatus';

function Navigation() {
  const location = useLocation();
  
  return (
    <nav>
      <Link to="/" className={location.pathname === '/' ? 'active' : ''}>Catalog</Link>
      <Link to="/order" className={location.pathname === '/order' ? 'active' : ''}>Place Order</Link>
      <Link to="/status" className={location.pathname === '/status' ? 'active' : ''}>Order Status</Link>
    </nav>
  );
}

function App() {
  return (
    <Router>
      <div className="container">
        <Navigation />
        <Routes>
          <Route path="/" element={<ProductList />} />
          <Route path="/order" element={<PlaceOrder />} />
          <Route path="/status" element={<OrderStatus />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;

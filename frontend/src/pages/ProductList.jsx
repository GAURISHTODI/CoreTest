import { useState, useEffect } from 'react';
import { getProducts } from '../api';
import { Link } from 'react-router-dom';

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await getProducts();
      setProducts(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '2rem' }}><div className="loader"></div></div>;
  }

  if (error) {
    return (
      <div className="card" style={{ borderColor: 'var(--danger-color)' }}>
        <p style={{ color: 'var(--danger-color)' }}>{error}</p>
        <button onClick={fetchProducts} style={{ marginTop: '1rem', background: 'var(--glass-bg)', color: 'var(--text-primary)' }}>Retry</button>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <h1>Product Catalog</h1>
      <div className="product-grid">
        {products.map(product => {
          let stockClass = 'success';
          if (product.stock === 0) stockClass = 'danger';
          else if (product.stock < 20) stockClass = 'warning';

          return (
            <div key={product.id} className="card">
              <h2>{product.name}</h2>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <span style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>${product.price}</span>
                <span className={`badge ${stockClass}`}>
                  {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
                </span>
              </div>
              <Link to={`/order?product=${product.id}`}>
                <button disabled={product.stock === 0}>
                  {product.stock > 0 ? 'Order Now' : 'Unavailable'}
                </button>
              </Link>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ProductList;

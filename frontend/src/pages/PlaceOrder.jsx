import { useState, useEffect } from 'react';
import { getProducts, placeOrder } from '../api';
import { useSearchParams, useNavigate } from 'react-router-dom';

function PlaceOrder() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const initialProductId = searchParams.get('product') || '';
  
  const [products, setProducts] = useState([]);
  const [productId, setProductId] = useState(initialProductId);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    getProducts().then(data => setProducts(data)).catch(err => console.error(err));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!productId || quantity < 1) {
      setError("Please select a product and valid quantity.");
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const order = await placeOrder(productId, parseInt(quantity));
      navigate(`/status?id=${order.id}`);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Failed to place order';
      setError(msg);
      setLoading(false);
    }
  };

  return (
    <div className="card animate-fade-in" style={{ maxWidth: '500px', margin: '0 auto' }}>
      <h1>Place Order</h1>
      
      {error && (
        <div className="badge danger" style={{ padding: '0.75rem', marginBottom: '1.5rem', display: 'block', borderRadius: '8px' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Product</label>
          <select 
            value={productId} 
            onChange={(e) => setProductId(e.target.value)}
            disabled={loading || products.length === 0}
          >
            <option value="">-- Select Product --</option>
            {products.map(p => (
              <option key={p.id} value={p.id} disabled={p.stock === 0}>
                {p.name} (${p.price}) {p.stock === 0 ? '- Out of stock' : ''}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Quantity</label>
          <input 
            type="number" 
            min="1" 
            value={quantity} 
            onChange={(e) => setQuantity(e.target.value)}
            disabled={loading}
          />
        </div>

        <button type="submit" disabled={loading || !productId}>
          {loading ? <span className="loader" style={{ width: '1rem', height: '1rem', borderWidth: '2px' }}></span> : 'Submit Order'}
        </button>
      </form>
    </div>
  );
}

export default PlaceOrder;

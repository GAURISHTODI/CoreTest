import { useState, useEffect } from 'react';
import { getOrder } from '../api';
import { useSearchParams } from 'react-router-dom';

function OrderStatus() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialOrderId = searchParams.get('id') || '';
  
  const [orderIdInput, setOrderIdInput] = useState(initialOrderId);
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchOrder = async (id) => {
    if (!id) return;
    try {
      setLoading(true);
      setError(null);
      const data = await getOrder(id);
      setOrder(data);
    } catch (err) {
      setOrder(null);
      setError(err.response?.status === 404 ? 'Order not found' : 'Failed to fetch order');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (initialOrderId) {
      fetchOrder(initialOrderId);
    }
  }, [initialOrderId]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (orderIdInput) {
      setSearchParams({ id: orderIdInput });
    }
  };

  return (
    <div className="card animate-fade-in" style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h1>Track Order</h1>
      
      <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
        <input 
          type="text" 
          placeholder="Enter Order ID" 
          value={orderIdInput} 
          onChange={(e) => setOrderIdInput(e.target.value)}
          style={{ flex: 1 }}
        />
        <button type="submit" style={{ width: 'auto' }} disabled={loading || !orderIdInput}>
          {loading ? '...' : 'Search'}
        </button>
      </form>

      {error && <p style={{ color: 'var(--danger-color)', textAlign: 'center' }}>{error}</p>}

      {order && (
        <div className="card" style={{ background: 'rgba(255,255,255,0.02)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
            <div>
              <p style={{ fontSize: '0.875rem' }}>Order ID</p>
              <h2 style={{ fontSize: '1.1rem', margin: 0, fontFamily: 'monospace' }}>{order.id}</h2>
            </div>
            <span className={`badge ${order.status === 'CONFIRMED' ? 'success' : order.status === 'FAILED' ? 'danger' : 'primary'}`}>
              {order.status}
            </span>
          </div>
          
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.5rem' }}>
            <div>
              <p style={{ fontSize: '0.875rem' }}>Product ID</p>
              <p style={{ fontFamily: 'monospace', color: 'var(--text-primary)' }}>{order.productId}</p>
            </div>
            <div>
              <p style={{ fontSize: '0.875rem' }}>Quantity</p>
              <p style={{ color: 'var(--text-primary)', fontWeight: 'bold' }}>{order.quantity}</p>
            </div>
            <div style={{ gridColumn: 'span 2' }}>
              <p style={{ fontSize: '0.875rem' }}>Created At</p>
              <p style={{ color: 'var(--text-primary)' }}>{new Date(order.createdAt).toLocaleString()}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default OrderStatus;

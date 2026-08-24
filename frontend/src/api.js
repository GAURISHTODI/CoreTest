import axios from 'axios';

const INVENTORY_API = 'http://localhost:8000/api';
const ORDER_API = 'http://localhost:8080/api';

export const getProducts = async () => {
  const response = await axios.get(`${INVENTORY_API}/products`);
  return response.data;
};

export const placeOrder = async (productId, quantity) => {
  const response = await axios.post(`${ORDER_API}/orders`, {
    productId,
    quantity
  });
  return response.data;
};

export const getOrder = async (orderId) => {
  const response = await axios.get(`${ORDER_API}/orders/${orderId}`);
  return response.data;
};

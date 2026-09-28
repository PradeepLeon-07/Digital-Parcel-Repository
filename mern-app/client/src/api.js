export function api(path, options = {}) {
  const token = localStorage.getItem('token');
  return fetch(`/api${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
    .then(async response => {
      const text = await response.text();
      let data = true;
      if (text) {
        try { data = JSON.parse(text); } catch { data = { message: text }; }
      }
      if (!response.ok) throw new Error(data.message || `Request failed (${response.status})`);
      return data;
    })
    .catch(error => {
      if (error instanceof TypeError) throw new Error('Unable to reach the API server. Start MongoDB and the Express server.');
      throw error;
    });
}
